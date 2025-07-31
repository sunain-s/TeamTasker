package com.teamtasker.exception;

import java.util.Map;

public class UserValidationException extends RuntimeException {

    private final Map<String, String> errorMessages;

    public UserValidationException(Map<String, String> errorMessages) {
        super("User validation failed");
        this.errorMessages = errorMessages;
    }

    public Map<String, String> getErrorMessages() {
        return errorMessages;
    }
}
