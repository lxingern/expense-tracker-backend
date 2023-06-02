package com.wileyedge.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.wileyedge.exceptions.ExpenseNotFoundException;
import com.wileyedge.exceptions.InvalidInputException;
import com.wileyedge.exceptions.UserNotAuthorizedException;

@ControllerAdvice
@RestController
public class ExpenseControllerExceptionHandler {

	@ExceptionHandler({InvalidInputException.class, ExpenseNotFoundException.class, UserNotAuthorizedException.class})
    public final ResponseEntity<Map<String, String>> handleInvalidInputException(Exception ex, WebRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getLocalizedMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

//	@ExceptionHandler(ExpenseNotFoundException.class)
//    public final ResponseEntity<Map<String, String>> handleExpenseNotFoundException(ExpenseNotFoundException ex, WebRequest request) {
//        Map<String, String> response = new HashMap<>();
//        response.put("error", "Could not find expense with that ID.");
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//	
}
