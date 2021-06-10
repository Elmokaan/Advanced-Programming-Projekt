package de.hsalbsig.HonigruehrmaschineSteuerung.controller;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hsalbsig.HonigruehrmaschineSteuerung.dao.LoggingRepository;
import de.hsalbsig.HonigruehrmaschineSteuerung.model.Logging;
import de.hsalbsig.HonigruehrmaschineSteuerung.service.CustomUserDetailsService;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Controller
public class UserController {


    MqttClient mqttClient ;

    List<String> labels = new ArrayList<>();
    List<Double> data = new ArrayList<>();


    MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            System.out.println(topic + " - "+ message.getPayload());

            JsonObject messageJson = new JsonParser().parse(message.getPayload().toString()).getAsJsonObject();
            Double temperaturValue = messageJson.get("Temperatur").getAsDouble();

            labels.add(LocalDateTime.now().toString());
            data.add(temperaturValue);

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    };

    HashMap<String,String> payload = new HashMap<>();


    public MqttClient getMqttClient() throws MqttException {
        if(mqttClient == null){
            mqttClient = new MqttClient("tcp://broker.hivemq.com:1883"
                    , MqttClient.generateClientId()
                    , new MemoryPersistence());

            mqttClient.setCallback(mqttCallback);
        }
        try {
            if(!mqttClient.isConnected()){
                mqttClient.connect();
                // topic subscription
                mqttClient.subscribe("honigsensors",1);
            }
        }catch (Exception e){
            System.out.println("Connection refused");
            e.printStackTrace();
        }
        return mqttClient;
    }

    @Autowired
    LoggingRepository loggingRepository;

    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private String mixingTime;
    private boolean isInProgress = false;
    private boolean isTimed = false;

    private LocalDateTime timedStart;
    private LocalDateTime timedStop;

    Timer timedStartTimer;
    Timer timedStopTimer;
    private boolean isTimerStopRunning = false;
    private boolean isTimerStartRunning = false;

    private long timeToReload;
    private boolean shouldReload = false;


    private Model model;


    @Autowired
    CustomUserDetailsService customUserDetailsService;


    @GetMapping("/login")
    public String login(){
        return "login.html";
    }

    @GetMapping("/signup")
    public String signup(){
        return "signup.html";
    }

    @GetMapping("/")
    public String toDashboard(){
        return "redirect:/dashboard";
    }


    @RequestMapping(method = RequestMethod.POST, value="/signup")
    public String signUp(@RequestParam( name="username", defaultValue= "") String username ,
                         @RequestParam( name="password", defaultValue= "") String password,
                         @RequestParam( name="token", defaultValue= "") String token
    ) {
        if(token != null && token.equals("14F5S8EFVR2BDS35S")){
            System.out.println(username + " " + password + " " +token);
            if (username == null || username.isBlank()
                    || username.isEmpty()){
                return "redirect:/user/signup?error";
            }
            if (password == null || password.isBlank()
                    || password.isEmpty()){
                return "redirect:/user/signup?error";
            }
            customUserDetailsService.saveUser(username,password);
            return "redirect:/login";
        }

        return "redirect:/signup?error";
    }


    @GetMapping("/logs")
    public String log(Model model){
        model.addAttribute("logs",loggingRepository.findAll());
        return "log.html";
    }


    @GetMapping("/dashboard")
    public String dashboard(Model model){
        this.model = model;
        model.addAttribute("start",String.valueOf(startTime==null?"":startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))));
        model.addAttribute("stop",String.valueOf(stopTime==null?"":stopTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))));
        model.addAttribute("mixingTime",mixingTime);
        model.addAttribute("isInProgress",isInProgress);
        model.addAttribute("isTimed",isTimed);
        model.addAttribute("timedStart",String.valueOf(timedStart==null?"":timedStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))));
        model.addAttribute("timedStop",String.valueOf(timedStop==null?"":timedStop.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))));


        model.addAttribute("labels",labels);
        model.addAttribute("data",data);

        model.addAttribute("timeToReload",timeToReload);
        model.addAttribute("shouldReload",shouldReload);

        return "dashboard";
    }


    @RequestMapping(value="/start")
    public String startCommand() throws MqttException {

        if(getMqttClient().isConnected()) {
            this.stopTime = null;
            this.mixingTime = "";

            if(!isTimed)
                this.startTime = LocalDateTime.now();
            else
                this.startTime = this.timedStart;

            isInProgress = true;


//        getMqttClient().publish(
//                "honigcontrol",
//                "{\"Ruehrer\":\"on\"}".getBytes(StandardCharsets.UTF_8),
//                2,
//                false
//        );

            loggingRepository.save(new Logging(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")), "Start"));
        }else {reset();};
        return "redirect:/dashboard";
    }
    @RequestMapping(value="/stop")
    public String stopCommand() throws MqttException {


        if (isInProgress && getMqttClient().isConnected()) {
            this.stopTime = LocalDateTime.now();
            if (isTimed && isTimerStopRunning) {
                this.timedStopTimer.cancel();
                this.isTimerStopRunning = false;
            }
            this.mixingTime = String.valueOf(Duration.between(startTime, stopTime)).substring(2);
            isInProgress = false;
            if (isTimed)
                isTimed = false;
            shouldReload = false;

//        getMqttClient().publish(
//                "honigcontrol",
//                "{\"Ruehrer\":\"off\"}".getBytes(StandardCharsets.UTF_8),
//                2,
//                false
//        );

            loggingRepository.save(new Logging(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")), "Stop"));
        }else {reset();};

        return "redirect:/dashboard";
    }

    @RequestMapping(value="/autostart")
    public String autoStart(@RequestParam( name="timedStart", defaultValue= "") String timedStart ,
                            @RequestParam( name="timedStop", defaultValue= "") String timedStop){

        if(timedStart != null
                && timedStart.matches("^([0-2][0-9]|[3][0-1])\\.([0][1-9]|[1][0-2])\\.([1][0-9][0-9][0-9]|[2][0][0-9][0-9]) ([0-1][0-9]|[2][0-4]):[0-5][0-9]:[0-5][0-9]$")
                && timedStop != null
                && timedStop.matches("^([0-2][0-9]|[3][0-1])\\.([0][1-9]|[1][0-2])\\.([1][0-9][0-9][0-9]|[2][0][0-9][0-9]) ([0-1][0-9]|[2][0-4]):[0-5][0-9]:[0-5][0-9]$")
        ){
            this.timedStart = LocalDateTime.parse(timedStart.toString(),DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) ;
            this.timedStop = LocalDateTime.parse(timedStop.toString(),DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) ;

            if(this.timedStart.isAfter(this.timedStop)
            ){
                LocalDateTime tempValue = this.timedStart;
                this.timedStart = this.timedStop;
                this.timedStop = tempValue;
            }
            if(this.timedStart.isAfter(LocalDateTime.now())
                    && this.timedStop.isAfter(LocalDateTime.now())
            ){
                this.isTimed = true;
                timer();
            }
        }
        return "redirect:/dashboard";
    }
    @RequestMapping(value="/abortautostart")
    public String abortAutoStart(){

        if(isTimed){
            this.startTime = null;
            this.stopTime = null;
            this.timedStart = null;
            this.timedStop = null;
            isTimed = false;
            if(isTimerStartRunning)
                timedStartTimer.cancel();
            isTimerStartRunning = false;
            shouldReload = false;
        }
        return "redirect:/dashboard";
    }



    public void timer(){

        timedStopTimer = new Timer();
        timedStartTimer = new Timer();

        this.timeToReload = ChronoUnit.MILLIS.between(LocalDateTime.now(),timedStart);
        this.timedStartTimer.schedule(getTimedStartTask(),this.timeToReload);
        this.shouldReload = true;
    }

    public TimerTask getTimedStopTask() {
        return new TimerTask() {
            @Override
            public void run() {
                isTimerStopRunning = true;
                try {
                    stopCommand();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                shouldReload = false;
            }
        };
    }


    public TimerTask getTimedStartTask() {
        return new TimerTask() {
            @Override
            public void run() {
                isTimerStartRunning = true;

                timeToReload = ChronoUnit.MILLIS.between(timedStart,timedStop);
                timedStopTimer.schedule(getTimedStopTask(),timeToReload);

                shouldReload = true;
                try {
                    startCommand();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        };
    }


    public void reset(){
        startTime = null;
        stopTime = null;
        mixingTime= "";
        isInProgress = false;
        isTimed = false;

        timedStart= null;
        timedStop= null;

        isTimerStopRunning = false;
        isTimerStartRunning = false;

        shouldReload = false;
    }




}
