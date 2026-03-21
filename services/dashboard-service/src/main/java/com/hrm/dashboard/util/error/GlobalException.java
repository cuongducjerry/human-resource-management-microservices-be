package com.hrm.dashboard.util.error;

import com.hrm.dashboard.entity.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

}
