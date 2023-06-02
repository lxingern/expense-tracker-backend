package com.wileyedge.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wileyedge.model.Expense;
import com.wileyedge.model.User;
import com.wileyedge.service.ExpenseService;
import com.wileyedge.service.UserService;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {
	
	@Autowired
	ExpenseService expenseService;
	
	@Autowired
	UserService userService;

	@GetMapping("")
	public ObjectNode getExpenses(Principal principal) {
		User user = getUserFromPrincipal(principal);
		return expenseService.getExpensesWithTotal(user);
	}
	
	@PostMapping("")
	public ResponseEntity<Expense> addExpense(Principal principal, @RequestBody Expense newExpense) {
		User user = getUserFromPrincipal(principal);
		expenseService.createExpense(newExpense, user);
		return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	public Expense editExpense(Principal principal, @PathVariable("id") int id, @RequestBody Expense newExpense) {
		User user = getUserFromPrincipal(principal);
		return expenseService.updateExpense(id, newExpense, user);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteExpense(Principal principal, @PathVariable("id") int id) {
		User user = getUserFromPrincipal(principal);
		expenseService.deleteExpense(id, user);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	private User getUserFromPrincipal(Principal principal) {
		String userEmail = principal.getName();
		return userService.findUserByEmail(userEmail);
	}
	
}
