package com.wileyedge.controller;

import java.security.Principal;
import java.util.List;

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
	
	@GetMapping("")
	public ResponseEntity<List<Budget>> getExpenses(Principal principal) {
		User user = getUserFromPrincipal(principal);
		List<Budget> budgets = budgetService.getBudgets(user);
		return new ResponseEntity<>(budgets, HttpStatus.OK);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Budget> editBudget(Principal principal, @PathVariable("id") int id, @RequestBody Budget newBudget) {
		User user = getUserFromPrincipal(principal);
		Budget updatedBudget = budgetService.updateBudget(id, newBudget, user);
		return new ResponseEntity<>(updatedBudget, HttpStatus.OK);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteBudget(Principal principal, @PathVariable("id") int id) {
		User user = getUserFromPrincipal(principal);
		budgetService.deleteBudget(id, user);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	private User getUserFromPrincipal(Principal principal) {
		String userEmail = principal.getName();
		return userService.findUserByEmail(userEmail);
	}
}
