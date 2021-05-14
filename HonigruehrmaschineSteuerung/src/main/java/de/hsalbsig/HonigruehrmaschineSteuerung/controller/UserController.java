package de.hsalbsig.HonigruehrmaschineSteuerung.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class UserController {

    @GetMapping("/login")
    public String login(){

        return "login.html";
    }
    @GetMapping("/signup")
    public String signup(){

        return "signup.html";
    }

}
