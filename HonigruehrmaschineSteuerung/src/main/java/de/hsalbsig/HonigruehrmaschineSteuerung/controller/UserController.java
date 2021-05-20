package de.hsalbsig.HonigruehrmaschineSteuerung.controller;
import de.hsalbsig.HonigruehrmaschineSteuerung.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class UserController {

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

    @GetMapping("/dashboard")
    public String dashboard(){
        return "dashboard";
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


}
