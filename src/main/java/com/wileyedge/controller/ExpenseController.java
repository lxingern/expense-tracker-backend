package com.wileyedge.controller;

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
import com.wileyedge.service.ExpenseService;

@RestController
@RequestMapping(value = "/expenses")
public class ExpenseController {
	
	@Autowired
	ExpenseService expenseService;

	@GetMapping("")
	public ObjectNode getExpenses() {
		return expenseService.getExpensesWithTotal();
	}
	
	@PostMapping("")
	public ResponseEntity<Expense> addExpense(@RequestBody Expense newExpense) {
		expenseService.createExpense(newExpense);
		return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
	}
	
	@PutMapping("/{id}")
	public Expense editExpense(@PathVariable("id") int id, @RequestBody Expense newExpense) {
		return expenseService.updateExpense(id, newExpense);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteExpense(@PathVariable("id") int id) {
		expenseService.deleteExpense(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
}
