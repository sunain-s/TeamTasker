package com.teamtasker.auth;

import com.teamtasker.entity.Role;
import com.teamtasker.entity.User;
import com.teamtasker.exception.UserNotFoundException;
import com.teamtasker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (userService.isUsernameTaken(user.getUsername())) {
            result.rejectValue("username", null, "Username already exists");
        }

        if (userService.isEmailTaken(user.getEmail())) {
            result.rejectValue("email", null, "Email already exists");
        }

        // Validation errors + form data cleared on page reload, PostRedirectGet pattern
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", result);
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/auth/register";
        }

        user.setRole(Role.USER);
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

    @GetMapping("/test-403")
    @PreAuthorize("hasRole('ADMIN')")
    public String test403(Authentication authentication) {
        System.out.println("User: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());
        return "dashboard";
    }

    @GetMapping("/test-404")
    public String test404() {
        throw new UserNotFoundException("Test user not found.");
    }

    @GetMapping("/test-500")
    public String test500() {
        throw new RuntimeException("Deliberate server error.");
    }
}


