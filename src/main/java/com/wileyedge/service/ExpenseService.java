package com.wileyedge.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wileyedge.model.Expense;

public interface ExpenseService {

	public ObjectNode getExpensesWithTotal();
	public Expense createExpense(Expense newExpense);
	public Expense updateExpense(int expenseId, Expense updatedExpense);
	public void deleteExpense(int expenseId);
	
}
