package com.luismiguel.java.url_shortener.infrastructure.exception;

import com.luismiguel.java.url_shortener.domain.exception.category.InvalidInputException;
import com.luismiguel.java.url_shortener.domain.exception.category.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Slf4j
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


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException e, HttpServletRequest request) {
        log.debug("{} on {}: {}", HttpStatus.NOT_FOUND, request.getRequestURI(), e.getMessage());
        HashMap<String, Object> body = errorBuilder(
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.name(),
                e.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Object> handleInvalidInput(InvalidInputException e, HttpServletRequest request) {
        log.debug("{} on {}: {}", HttpStatus.UNPROCESSABLE_ENTITY, request.getRequestURI(), e.getMessage());
        HashMap<String, Object> body = errorBuilder(
                HttpStatus.UNPROCESSABLE_ENTITY,
                HttpStatus.UNPROCESSABLE_ENTITY.name(),
                e.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception exception, HttpServletRequest request){
        log.error("Unexpected error on {}", request.getRequestURI(), exception);
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


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.debug("{} on {}: {}", HttpStatus.BAD_REQUEST, request.getRequestURI(), message);
        HashMap<String, Object> body = errorBuilder(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.name(),
                message,
                request.getRequestURI()
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleMessageNotReadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        HashMap<String, Object> body = errorBuilder(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.name(),
                "Malformed or missing request body",
                request.getRequestURI()
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException exception, HttpServletRequest request) {
        String message = exception.getContentType() == null
                ? "Content type is missing"
                : "Content type " + exception.getContentType() + " is not supported";

        HashMap<String, Object> body = errorBuilder(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.name(),
                message,
                request.getRequestURI()
        );

        return new ResponseEntity<>(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFound(NoResourceFoundException exception, HttpServletRequest request) {
        HashMap<String, Object> body = errorBuilder(
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.name(),
                "Resource not found",
                request.getRequestURI()
        );

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}