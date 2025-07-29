package com.teamtasker.exception;

public class InvalidLoginPasswordException extends RuntimeException {
    public InvalidLoginPasswordException(String message) {
        super(message);
    }
}