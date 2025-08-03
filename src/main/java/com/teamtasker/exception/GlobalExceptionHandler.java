package com.teamtasker.exception;

import com.teamtasker.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException e, Model model) {
        model.addAttribute("user_error_message", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(UserValidationException.class)
    public String handleUserValidationException(UserValidationException e, Model model) {
        model.addAttribute("validationErrors", e.getErrorMessages());
        model.addAttribute("user", new User());
        return "auth/register";
    }

//    @ExceptionHandler(InvalidLoginException.class)
//    public String handleInvalidLoginException(InvalidChangePasswordException e, Model model) {
//        model.addAttribute("login_error_message", e.getMessage());
//        return "auth/login";
//    }

    @ExceptionHandler(InvalidChangePasswordException.class)
    public String handleInvalidChangePasswordException(InvalidChangePasswordException e, Model model) {
        model.addAttribute("change_password_error_message", e.getMessage());
        return "user/change_password";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, Model model) {
        model.addAttribute("generic_error_message", "An unexpected error occurred");
        e.printStackTrace();
        return "error/500";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("user_error_message", "You don't have permission to access this page.");
        return "error/403";
    }
}
