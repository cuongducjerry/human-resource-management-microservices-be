package com.hrm.employee.util.error;

public class RequestException extends RuntimeException {
    public RequestException(String message) {
        super(message);
    }
}
