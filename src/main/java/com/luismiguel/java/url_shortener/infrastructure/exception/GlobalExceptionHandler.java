package com.luismiguel.java.url_shortener.infrastructure.exception;

import com.luismiguel.java.url_shortener.infrastructure.exception.business.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static HashMap<String, Object> errorBuilder(HttpStatus status, String errorName, String exceptionMessage, String path){
        HashMap<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error",  errorName);
        body.put("message", exceptionMessage);
        body.put("path", path);
        return body;
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException e, HttpServletRequest request) {
        HashMap<String, Object> body = errorBuilder(
                e.getStatus(),
                e.getStatus().name(),
                e.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, e.getStatus());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception exception, HttpServletRequest request){
        HashMap<String, Object> body = errorBuilder(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "An unexpected error occurred.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        String message = "HTTP method " + exception.getMethod() + " is not supported for this endpoint";

        HashMap<String, Object> body = errorBuilder(
                HttpStatus.METHOD_NOT_ALLOWED,
                HttpStatus.METHOD_NOT_ALLOWED.name(),
                message,
                request.getRequestURI()
        );

        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }
}