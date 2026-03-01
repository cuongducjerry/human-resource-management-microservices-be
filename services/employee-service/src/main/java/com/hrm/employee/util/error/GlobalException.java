package com.hrm.employee.util.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.employee.entity.RestResponse;
import feign.FeignException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<RestResponse<Object>> handleBadRequest(
            BadRequestException ex
    ) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RequestException.class)
    public ResponseEntity<RestResponse<Object>> handleRequest(
            RequestException ex
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RestResponse<Object>> handleAccessDenied(
            AccessDeniedException ex
    ) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission");
    }

    @ExceptionHandler({
            UsernameNotFoundException.class,
            IdInvalidException.class
    })
    public ResponseEntity<RestResponse<Object>> handleNotFound(Exception ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<RestResponse<Object>> handleForbidden(Exception ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler({
            ImageException.class
    })
    public ResponseEntity<RestResponse<Object>> handleImage(Exception ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<RestResponse<Object>> handleFeign(FeignException ex) {

        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String message = extractMessageFromFeign(ex);

        return build(status, message);
    }

    private String extractMessageFromFeign(FeignException ex) {
        try {
            String body = ex.contentUTF8();

            if (body == null || body.isBlank()) {
                return "Service error";
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);

            if (node.has("message")) {
                return node.get("message").asText();
            }

        } catch (Exception ignored) {}

        return "Service error";
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<RestResponse<Object>> handleInternal(
            InternalServerException ex
    ) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

}
