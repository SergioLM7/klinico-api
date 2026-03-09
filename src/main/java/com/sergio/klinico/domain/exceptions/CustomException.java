package com.sergio.klinico.domain.exceptions;

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}
