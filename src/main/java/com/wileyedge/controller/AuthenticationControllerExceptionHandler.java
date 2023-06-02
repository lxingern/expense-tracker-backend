package com.wileyedge.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.wileyedge.exceptions.InvalidCredentialsException;

@ControllerAdvice
@RestController
public class AuthenticationControllerExceptionHandler {

	@ExceptionHandler(DataIntegrityViolationException.class)
    public final ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "An account already exists for that email.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(InvalidCredentialsException.class)
    public final ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(InvalidCredentialsException ex, WebRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getLocalizedMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
	
}