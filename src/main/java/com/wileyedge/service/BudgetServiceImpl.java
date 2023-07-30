package com.wileyedge.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wileyedge.dao.BudgetRepository;
import com.wileyedge.exceptions.BudgetAlreadyExistsException;
import com.wileyedge.exceptions.InvalidInputException;
import com.wileyedge.model.Budget;
import com.wileyedge.model.Expense;
import com.wileyedge.model.Timeframe;
import com.wileyedge.model.User;

@Service
public class BudgetServiceImpl implements BudgetService {
	
	@Autowired
	BudgetRepository budgetRepo;
	
	@Autowired
	ObjectMapper mapper;

	@Override
	public Budget createBudget(Budget newBudget, User user) {
		validateBudget(newBudget, user);
		newBudget.setUser(user);
		
		return budgetRepo.save(newBudget);
	}
	
	@Override
	public List<Budget> getBudgets(User user) {
		List<Budget> budgets = budgetRepo.findByUser(user);
		return budgets;
	}

	private void validateBudget(Budget newBudget, User user) {
		if (!(newBudget.getType().equals("Overall") || newBudget.getType().equals("Category"))) {
			throw new InvalidInputException("Type must be either 'Overall' or 'Category'.");
		}
		
		if (newBudget.getType().equals("Category") && !Expense.getCategories().contains(newBudget.getCategory())) {
			throw new InvalidInputException("Category must be one of the following: 'Food and Drink', 'Utilities and Bills', 'Transport', 'Leisure'.");
		}
		
		if (newBudget.getAmount().compareTo(new BigDecimal("0")) == -1) {
			throw new InvalidInputException("Amount cannot be negative.");
		}
		
		List<Budget> budgets = budgetRepo.findByUser(user);
		String newBudgetType = newBudget.getType();
		String newBudgetCategory = newBudget.getCategory();
		Timeframe newBudgetTimeframe = newBudget.getTimeframe();
		boolean isDuplicateBudget;
		if (newBudgetType.equals("Overall")) {
			isDuplicateBudget = budgets.stream()
									.anyMatch(budget -> budget.getType().equals("Overall") && budget.getTimeframe().equals(newBudgetTimeframe));
		} else {
			isDuplicateBudget = budgets.stream()
									.anyMatch(budget -> budget.getType().equals("Category") && budget.getCategory().equals(newBudgetCategory) && budget.getTimeframe().equals(newBudgetTimeframe));
		}
		if (isDuplicateBudget) throw new BudgetAlreadyExistsException("That budget already exists.");
	}

}
