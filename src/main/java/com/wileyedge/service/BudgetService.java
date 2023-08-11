package com.wileyedge.service;

import java.util.List;

import com.wileyedge.model.Budget;
import com.wileyedge.model.BudgetInstance;
import com.wileyedge.model.User;

public interface BudgetService {

	public Budget createBudget(Budget newBudget, User user);
	public List<BudgetInstance> getCurrentBudgetsWithUtilisation(User user);
	public Budget updateBudget(int budgetId, Budget updatedBudget, User user);
	public void deleteBudget(int budgetId, User user);
	
}
