package com.hrm.auth.util.error;

public class InvalidLoginException extends RuntimeException {
    public InvalidLoginException(String message){
        super(message);
    }
}
