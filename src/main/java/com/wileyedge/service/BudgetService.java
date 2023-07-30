package com.wileyedge.service;

import com.wileyedge.model.Budget;
import com.wileyedge.model.User;

public interface BudgetService {

	public Budget createBudget(Budget newBudget, User user);
	
}
