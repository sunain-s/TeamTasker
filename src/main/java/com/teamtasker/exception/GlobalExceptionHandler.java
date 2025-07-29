package com.teamtasker.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException e, Model model) {
        model.addAttribute("user_error_message", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public String handleDuplicateUsernameException(DuplicateUsernameException e, Model model) {
        model.addAttribute("username_error_message", e.getMessage());
        return "auth/register";
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public String handleDuplicateEmailException(DuplicateEmailException e, Model model) {
        model.addAttribute("email_error_message", e.getMessage());
        return "auth/register";
    }

    @ExceptionHandler(InvalidLoginPasswordException.class)
    public String handleInvalidLoginPasswordException(InvalidChangePasswordException e, Model model) {
        model.addAttribute("password_login_error_message", e.getMessage());
        return "auth/login";
    }

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
}
