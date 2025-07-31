package com.teamtasker.auth;

import com.teamtasker.entity.User;
import com.teamtasker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        userService.registerUser(user);
        redirectAttributes.addFlashAttribute("register_message", "Registration successful! Please log in");
        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String loginForm(Model model, HttpServletRequest request) {
        String errorMessage = (String) request.getSession().getAttribute("error_message");
        if (errorMessage != null) {
            model.addAttribute("error_message", errorMessage);
            request.getSession().removeAttribute("error_message");
        }

        String logoutMessage = (String) request.getSession().getAttribute("logout_message");
        if (logoutMessage != null) {
            model.addAttribute("logout_message", logoutMessage);
            request.getSession().removeAttribute("logout_message");
        }
        return "auth/login";
    }
}


