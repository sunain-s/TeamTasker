package com.teamtasker.exception;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode != null) {
            int status = Integer.parseInt(statusCode.toString());

            return switch (status) {
                case 403 -> {
                    model.addAttribute("user_error_message", "You don't have permission to access this page.");
                    yield "error/403";
                }
                case 404 -> {
                    model.addAttribute("user_error_message", "Page not found.");
                    yield "error/404";
                }
                case 500 -> {
                    model.addAttribute("generic_error_message", "An internal server error occurred.");
                    yield "error/500";
                }
                default -> {
                    model.addAttribute("generic_error_message", "Unexpected error occurred.");
                    yield "error/500";
                }
            };
        }
        model.addAttribute("generic_error_message", "Unexpected error occurred.");
        return "error/500";
    }
}
