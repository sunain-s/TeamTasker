package com.teamtasker.auth;

import com.teamtasker.entity.User;
import com.teamtasker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.registerUser(user);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error_message", e.getMessage());
            return "auth/register";
        }
        return "redirect:/auth/login?registered";
    }

    @GetMapping("/login")
    public String loginForm(Model model,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered",  required = false) String registered) {
        if (error != null) {
            model.addAttribute("error_message", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("logout_message", "You have been logged out");
        }
        if (registered != null) {
            model.addAttribute("register_message", "Registration successful! Please log in");
        }
        return "auth/login";
    }
}


