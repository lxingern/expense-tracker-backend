package com.wileyedge.service;

import java.util.List;

import com.wileyedge.model.Budget;
import com.wileyedge.model.User;

public interface BudgetService {

	public Budget createBudget(Budget newBudget, User user);
	public List<Budget> getBudgets(User user);
	
}
