package com.wileyedge.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wileyedge.model.Budget;
import com.wileyedge.model.User;
import com.wileyedge.service.BudgetService;
import com.wileyedge.service.UserService;

@RestController
@RequestMapping("/budgets")
public class BudgetController {
	
	@Autowired
	BudgetService budgetService;
	
	@Autowired
	UserService userService;
	
	@PostMapping("")
	public ResponseEntity<Budget> addExpense(Principal principal, @RequestBody Budget newBudget) {
		User user = getUserFromPrincipal(principal);
		budgetService.createBudget(newBudget, user);
		return new ResponseEntity<>(newBudget, HttpStatus.CREATED);
	}
	
	private User getUserFromPrincipal(Principal principal) {
		String userEmail = principal.getName();
		return userService.findUserByEmail(userEmail);
	}
}
