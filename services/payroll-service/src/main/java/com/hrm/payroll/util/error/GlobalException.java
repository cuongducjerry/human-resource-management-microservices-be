package com.hrm.payroll.util.error;

import com.hrm.payroll.entity.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<RestResponse<Object>> handleBadRequest(
            BadRequestException ex
    ) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // HANDLE VALIDATION
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

    @ExceptionHandler({
            IdInvalidException.class
    })
    public ResponseEntity<RestResponse<Object>> handleNotFound(Exception ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<RestResponse<Object>> handleForbidden(Exception ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

}
