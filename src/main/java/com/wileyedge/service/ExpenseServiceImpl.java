package com.wileyedge.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wileyedge.dao.ExpenseRepository;
import com.wileyedge.exceptions.ExpenseNotFoundException;
import com.wileyedge.exceptions.InvalidInputException;
import com.wileyedge.model.Expense;

@Service
public class ExpenseServiceImpl implements ExpenseService {

	@Autowired
	ExpenseRepository expenseRepo;
	
	@Autowired
	ObjectMapper mapper;

	@Override
	public ObjectNode getExpensesWithTotal() {
		List<Expense> expenses = expenseRepo.findAll();
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
	public Expense createExpense(Expense newExpense) {
		validateExpense(newExpense);

		return expenseRepo.save(newExpense);
	}

	@Override
	public Expense updateExpense(int expenseId, Expense updatedExpense) {
		if (updatedExpense.getId() != 0 && expenseId != updatedExpense.getId()) {
			throw new InvalidInputException("Expense IDs in path and request body do not match.");
		}
		
		checkIfExpenseExists(expenseId);
		
		validateExpense(updatedExpense);
		
		updatedExpense.setId(expenseId);		
		return expenseRepo.save(updatedExpense);
	}

	@Override
	public void deleteExpense(int expenseId) {
		checkIfExpenseExists(expenseId);
		
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
	
	private void checkIfExpenseExists(int expenseId) {
		boolean expenseExists = expenseRepo.existsById(expenseId);
		if (!expenseExists) throw new ExpenseNotFoundException();
	}
}
