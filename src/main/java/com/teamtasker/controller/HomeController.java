package com.teamtasker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home() {
        System.out.println("✅ Home route hit");
        return "home"; // renders home.html from templates
    }
}
