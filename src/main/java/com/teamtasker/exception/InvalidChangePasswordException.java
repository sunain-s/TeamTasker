package com.teamtasker.exception;

public class InvalidChangePasswordException extends RuntimeException {

    public InvalidChangePasswordException(String message) {
        super(message);
    }
}