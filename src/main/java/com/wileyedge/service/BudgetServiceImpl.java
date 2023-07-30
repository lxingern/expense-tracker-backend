package com.wileyedge.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wileyedge.dao.BudgetRepository;
import com.wileyedge.exceptions.InvalidInputException;
import com.wileyedge.model.Budget;
import com.wileyedge.model.Expense;
import com.wileyedge.model.User;

@Service
public class BudgetServiceImpl implements BudgetService {
	
	@Autowired
	BudgetRepository budgetRepo;

	@Override
	public Budget createBudget(Budget newBudget, User user) {
		validateBudget(newBudget);
		newBudget.setUser(user);
		
		return budgetRepo.save(newBudget);
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

}
