package com.wileyedge.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wileyedge.dao.ExpenseRepository;
import com.wileyedge.exceptions.ExpenseNotFoundException;
import com.wileyedge.exceptions.InvalidInputException;
import com.wileyedge.exceptions.UserNotAuthorizedException;
import com.wileyedge.model.Expense;
import com.wileyedge.model.User;

@Service
public class ExpenseServiceImpl implements ExpenseService {

	@Autowired
	ExpenseRepository expenseRepo;
	
	@Autowired
	ObjectMapper mapper;
	
	public ExpenseServiceImpl(ExpenseRepository expenseRepo, ObjectMapper mapper) {
		this.expenseRepo = expenseRepo;
		this.mapper = mapper;
	}

	@Override
	public ObjectNode getExpensesWithTotal(User user) {
		List<Expense> expenses = expenseRepo.findAllByUserId(user.getId());
		Collections.sort(expenses, (e1, e2) -> {
			return e2.getDate().compareTo(e1.getDate());
		});
		JsonNode expensesNode = mapper.valueToTree(expenses);
		BigDecimal total = expenses.stream()
							.map((exp) -> exp.getAmount())
							.reduce((a1, a2) -> a1.add(a2))
							.orElse(new BigDecimal("0"));
		
		ObjectNode baseNode = mapper.createObjectNode();
		baseNode.put("totalAmount", total);
		baseNode.set("expenses", expensesNode);
		
		return baseNode;
	}

	@Override
	public Expense createExpense(Expense newExpense, User user) {
		validateExpense(newExpense);
		newExpense.setUser(user);

		return expenseRepo.save(newExpense);
	}

	@Override
	public Expense updateExpense(int expenseId, Expense updatedExpense, User user) {
		if (updatedExpense.getId() != 0 && expenseId != updatedExpense.getId()) {
			throw new InvalidInputException("Expense IDs in path and request body do not match.");
		}
		
		Expense currExpense = getExpenseIfExists(expenseId);
		
		checkIfUserIsAuthorized(currExpense, user);
		
		validateExpense(updatedExpense);
		
		updatedExpense.setId(expenseId);
		updatedExpense.setUser(user);
		return expenseRepo.save(updatedExpense);
	}

	@Override
	public void deleteExpense(int expenseId, User user) {
		Expense expense = getExpenseIfExists(expenseId);
		
		checkIfUserIsAuthorized(expense, user);
		
		expenseRepo.deleteById(expenseId);
	}
	
	private void validateExpense(Expense expense) {
		if (expense.getAmount().compareTo(new BigDecimal("0")) == -1) {
			throw new InvalidInputException("Amount cannot be negative.");
		}
		
		if (!Expense.getCategories().contains(expense.getCategory())) {
			throw new InvalidInputException("Category must be one of the following: 'Food and Drink', 'Utilities and Bills', 'Transport', 'Leisure'.");
		}
	}
	
	private Expense getExpenseIfExists(int expenseId) {
		return expenseRepo.findById(expenseId).orElseThrow(() -> new ExpenseNotFoundException("Could not find expense with that ID."));
	}
	
	private void checkIfUserIsAuthorized(Expense expense, User user) {
		if (!expense.getUser().equals(user)) {
			throw new UserNotAuthorizedException("You are not authorized to perform this transaction.");
		}
	}
	
}
