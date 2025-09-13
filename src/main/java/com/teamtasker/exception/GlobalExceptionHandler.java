package com.teamtasker.exception;

import com.teamtasker.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    //------------------------------------------------------------------------------------------------------------------
    // User Exceptions

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

    @ExceptionHandler(InvalidChangePasswordException.class)
    public String handleInvalidChangePasswordException(InvalidChangePasswordException e, Model model) {
        model.addAttribute("change_password_error_message", e.getMessage()); // may need to change to 'user_error_message'
        return "user/change_password";
    }

    //------------------------------------------------------------------------------------------------------------------
    // Team Exceptions

    @ExceptionHandler(TeamNotFoundException.class)
    public String handleTeamNotFoundException(TeamNotFoundException e, Model model) {
        model.addAttribute("user_error_message", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(TeamAccessException.class)
    public String handleTeamAccessException(TeamAccessException e, Model model) {
        model.addAttribute("user_error_message", e.getMessage());
        return "error/403";
    }

    @ExceptionHandler(TeamAlreadyExistsException.class)
    public String handleTeamAlreadyExistsException(TeamAlreadyExistsException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("user_error_message", e.getMessage());
        redirectAttributes.addFlashAttribute("conflictingTeamName", e.getTeamName());
        return "redirect:/teams/create"; // example redirect
    }

    @ExceptionHandler(UserNotInTeamException.class)
    public String handleUserNotInTeamException(UserNotInTeamException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("user_error_message", e.getMessage());
        if (e.getTeamId() != null) {
            return "redirect:/teams/" + e.getTeamId();
        }
        return "redirect:/teams";
    }

    //------------------------------------------------------------------------------------------------------------------
    // Generic Exceptions - commented out to avoid conflicts, uncomment if necessary

//    @ExceptionHandler(NoHandlerFoundException.class)
//    public String handleNoHandlerFound(NoHandlerFoundException e, Model model) {
//        model.addAttribute("user_error_message", "Page not found.");
//        return "error/404";
//    }
//
//    @ExceptionHandler(Exception.class)
//    public String handleGenericException(Exception e, Model model) {
//        model.addAttribute("generic_error_message", "An unexpected error occurred");
//        e.printStackTrace();
//        return "error/500";
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public String handleAccessDenied(AccessDeniedException ex, Model model) {
//        model.addAttribute("user_error_message", "You don't have permission to access this page.");
//        return "error/403";
//    }
}
