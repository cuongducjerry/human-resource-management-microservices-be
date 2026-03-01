package com.hrm.auth.util.error;

import com.hrm.auth.entity.RestResponse;
import jakarta.ws.rs.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {

    private ResponseEntity<RestResponse<Object>> build(
            HttpStatus status, String message, Object data
    ) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(status.value());
        res.setError(status.getReasonPhrase());
        res.setMessage(message);
        res.setData(data);
        return ResponseEntity.status(status).body(res);
    }

    private ResponseEntity<RestResponse<Object>> build(
            HttpStatus status, String message
    ) {
        return build(status, message, null);
    }

    // VALIDATION
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Invalid request");

        return build(HttpStatus.BAD_REQUEST, message);
    }

    // CUSTOM AUTH EXCEPTION
    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<RestResponse<Object>> handleAuth(
            InvalidLoginException ex
    ) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // BAD REQUEST
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<RestResponse<Object>> handleBadRequest(
            BadRequestException ex
    ) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // RESPONSE STATUS
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<RestResponse<Object>> handleResponseStatus(
            ResponseStatusException ex
    ) {
        return build(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getReason()
        );
    }

    // FALLBACK
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleException(
            Exception ex
    ) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
        );
    }
}
