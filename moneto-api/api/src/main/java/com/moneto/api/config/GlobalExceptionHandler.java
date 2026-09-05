package com.moneto.api.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "An error occurred";
        HttpStatus status = resolveStatus(message);

        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }

    // Përcakton kodin e statusit sipas mesazhit
    private HttpStatus resolveStatus(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("not found")) {
            return HttpStatus.NOT_FOUND; // 404
        }
        if (lower.contains("access denied") || lower.contains("only")) {
            return HttpStatus.FORBIDDEN; // 403
        }
        if (lower.contains("already") || lower.contains("invalid")
                || lower.contains("incorrect") || lower.contains("verify")
                || lower.contains("must") || lower.contains("required")) {
            return HttpStatus.BAD_REQUEST; // 400
        }
        return HttpStatus.BAD_REQUEST; // default 400 (jo 500, sepse shumica janë gabime përdoruesi)
    }
}