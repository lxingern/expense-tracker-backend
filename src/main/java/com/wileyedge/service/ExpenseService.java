package com.wileyedge.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wileyedge.model.Expense;
import com.wileyedge.model.User;

public interface ExpenseService {

	public ObjectNode getExpensesWithTotal(User user);
	public Expense createExpense(Expense newExpense, User user);
	public Expense updateExpense(int expenseId, Expense updatedExpense, User user);
	public void deleteExpense(int expenseId, User user);
	
}
