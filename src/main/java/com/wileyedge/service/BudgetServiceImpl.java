package com.wileyedge.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wileyedge.dao.BudgetRepository;
import com.wileyedge.exceptions.BudgetAlreadyExistsException;
import com.wileyedge.exceptions.BudgetNotFoundException;
import com.wileyedge.exceptions.InvalidInputException;
import com.wileyedge.exceptions.UserNotAuthorizedException;
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
		validateBudget(newBudget);
		checkIfDuplicate(newBudget, user);
		newBudget.setUser(user);
		
		return budgetRepo.save(newBudget);
	}
	
	@Override
	public List<Budget> getBudgets(User user) {
		List<Budget> budgets = budgetRepo.findByUser(user);
		return budgets;
	}
	
	@Override
	public Budget updateBudget(int budgetId, Budget updatedBudget, User user) {
		if (updatedBudget.getId() != 0 && budgetId != updatedBudget.getId()) {
			throw new InvalidInputException("Expense IDs in path and request body do not match.");
		}
		
		Budget currBudget = getBudgetIfExists(budgetId);
		
		checkIfUserIsAuthorized(currBudget, user);
		
		validateBudget(updatedBudget);
		checkIfDuplicateAfterUpdate(budgetId, updatedBudget, user);
		
		updatedBudget.setId(budgetId);
		updatedBudget.setUser(user);
		return budgetRepo.save(updatedBudget);
	}

	private int getDuplicateBudgetId(Budget updatedBudget, User user) {
		List<Budget> budgets = budgetRepo.findByUser(user);
		String newBudgetType = updatedBudget.getType();
		String newBudgetCategory = updatedBudget.getCategory();
		Timeframe newBudgetTimeframe = updatedBudget.getTimeframe();
		
		Budget duplicateBudget;
		
		if (newBudgetType.equals("Overall")) {
			duplicateBudget = budgets.stream()
					.filter(budget -> budget.getType().equals("Overall") && budget.getTimeframe().equals(newBudgetTimeframe))
					.findFirst()
					.orElse(null);
		} else {
			duplicateBudget = budgets.stream()
					.filter(budget -> budget.getType().equals("Category") && budget.getCategory().equals(newBudgetCategory) && budget.getTimeframe().equals(newBudgetTimeframe))
					.findFirst()
					.orElse(null);
		}
		
		if (duplicateBudget == null) {
			return 0;
		} else {
			return duplicateBudget.getId();
		}
	}
	
	private void checkIfDuplicateAfterUpdate(int budgetId, Budget updatedBudget, User user) {
		int duplicateBudgetId = getDuplicateBudgetId(updatedBudget, user);
		
		if (duplicateBudgetId != budgetId) throw new BudgetAlreadyExistsException("That budget already exists.");
	}

	private void checkIfUserIsAuthorized(Budget budget, User user) {
		if (!budget.getUser().equals(user)) {
			throw new UserNotAuthorizedException("You are not authorized to perform this transaction.");
		}
	}

	private Budget getBudgetIfExists(int budgetId) {
		return budgetRepo.findById(budgetId).orElseThrow(() -> new BudgetNotFoundException("Could not find budget with that ID."));
	}

	private void validateBudget(Budget newBudget) {
		if (!(newBudget.getType().equals("Overall") || newBudget.getType().equals("Category"))) {
			throw new InvalidInputException("Type must be either 'Overall' or 'Category'.");
		}
		
		if (newBudget.getType().equals("Category") && !Expense.getCategories().contains(newBudget.getCategory())) {
			throw new InvalidInputException("Category must be one of the following: 'Food and Drink', 'Utilities and Bills', 'Transport', 'Leisure'.");
		}
		
		if (newBudget.getAmount().compareTo(new BigDecimal("0")) == -1) {
			throw new InvalidInputException("Amount cannot be negative.");
		}		
	}

	private void checkIfDuplicate(Budget newBudget, User user) {
		int duplicateBudgetId = getDuplicateBudgetId(newBudget, user);
		
		if (duplicateBudgetId != 0) throw new BudgetAlreadyExistsException("That budget already exists.");
	}
	
}
