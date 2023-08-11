package com.wileyedge.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wileyedge.dao.BudgetRepository;
import com.wileyedge.dao.ExpenseRepository;
import com.wileyedge.exceptions.BudgetAlreadyExistsException;
import com.wileyedge.exceptions.BudgetNotFoundException;
import com.wileyedge.exceptions.InvalidInputException;
import com.wileyedge.exceptions.UserNotAuthorizedException;
import com.wileyedge.model.Budget;
import com.wileyedge.model.BudgetInstance;
import com.wileyedge.model.Expense;
import com.wileyedge.model.Timeframe;
import com.wileyedge.model.User;

@Service
public class BudgetServiceImpl implements BudgetService {
	
	@Autowired
	BudgetRepository budgetRepo;
	@Autowired
	ExpenseRepository expenseRepo;
	
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
	public List<BudgetInstance> getCurrentBudgetsWithUtilisation(User user) {
		List<Budget> budgets = budgetRepo.findByUser(user);
		LocalDate today = LocalDate.now();
		List<Expense> expenses = expenseRepo.findFilteredByUserId(user, LocalDate.of(today.getYear(), 1, 1), LocalDate.of(today.getYear(), 12, 31), Expense.getCategories());
		List<BudgetInstance> budgetInstances = new ArrayList<>();
			
		Timeframe timeframe;
		for (Budget budget : budgets) {
			timeframe = budget.getTimeframe();
			
			switch (timeframe) {
				case DAILY:
					createDailyBudgetInstance(expenses, budgetInstances, budget, budget.getType(), budget.getCategory());
					break;
				case WEEKLY:
					createWeeklyBudgetInstance(expenses, budgetInstances, budget, budget.getType(), budget.getCategory());
					break;
				case MONTHLY:
					createMonthlyBudgetInstance(expenses, budgetInstances, budget, budget.getType(), budget.getCategory());
					break;
				case QUARTERLY:
					createQuarterlyBudgetInstance(expenses, budgetInstances, budget, budget.getType(), budget.getCategory());
					break;
				case YEARLY:
					createYearlyBudgetInstance(expenses, budgetInstances, budget, budget.getType(), budget.getCategory());
					break;
			}			
		}
		
		return budgetInstances;
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

	@Override
	public void deleteBudget(int budgetId, User user) {
		Budget budgetToDelete = getBudgetIfExists(budgetId);
		
		checkIfUserIsAuthorized(budgetToDelete, user);
		
		budgetRepo.deleteById(budgetId);
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

	private Budget getBudgetIfExists(int budgetId) {
		return budgetRepo.findById(budgetId).orElseThrow(() -> new BudgetNotFoundException("Could not find budget with that ID."));
	}

	private void checkIfUserIsAuthorized(Budget budget, User user) {
		if (!budget.getUser().equals(user)) {
			throw new UserNotAuthorizedException("You are not authorized to perform this transaction.");
		}
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
	
	private void checkIfDuplicate(Budget newBudget, User user) {
		int duplicateBudgetId = getDuplicateBudgetId(newBudget, user);
		
		if (duplicateBudgetId != 0) throw new BudgetAlreadyExistsException("That budget already exists.");
	}

	private void checkIfDuplicateAfterUpdate(int budgetId, Budget updatedBudget, User user) {
		int duplicateBudgetId = getDuplicateBudgetId(updatedBudget, user);
		
		if (duplicateBudgetId != budgetId) throw new BudgetAlreadyExistsException("That budget already exists.");
	}
	
	private BigDecimal getTotalFromFilteredExpenses(List<Expense> allExpenses, Predicate<Expense> filterCondition) {
		BigDecimal totalExpenses = new BigDecimal(0);
		List<Expense> filteredExpenses = allExpenses.stream()
			.filter(filterCondition)
			.collect(Collectors.toList());
		for (Expense e : filteredExpenses) totalExpenses = totalExpenses.add(e.getAmount());
		return totalExpenses;
	}
	
	private void createBudgetInstance(Budget budget, List<BudgetInstance> budgetInstances, LocalDate start, LocalDate end, BigDecimal totalExpenses) {
		BigDecimal utilisation = totalExpenses.divide(budget.getAmount(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
		budgetInstances.add(new BudgetInstance(budget, start, end, totalExpenses, utilisation));
	}

	private void createDailyBudgetInstance(List<Expense> expenses, List<BudgetInstance> budgetInstances, Budget budget, String type, String category) {
		LocalDate today = LocalDate.now();
		BigDecimal totalExpenses;
		
		if (type.equals("Overall")) {
			totalExpenses = getTotalFromFilteredExpenses(expenses, e -> e.getDate().isEqual(today));			
		} else {
			totalExpenses = getTotalFromFilteredExpenses(expenses, e -> e.getCategory().equals(category) && e.getDate().isEqual(today));
		}
		
		createBudgetInstance(budget, budgetInstances, today, today, totalExpenses);
	}

	private void createWeeklyBudgetInstance(List<Expense> expenses,	List<BudgetInstance> budgetInstances, Budget budget, String type, String category) {
		LocalDate today = LocalDate.now();
		BigDecimal totalExpenses;
		LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		
		if (type.equals("Overall")) {
			totalExpenses = getTotalFromFilteredExpenses(expenses, e -> e.getDate().compareTo(startOfWeek) >= 0 && e.getDate().compareTo(endOfWeek) <= 0);			
		} else {
			totalExpenses = getTotalFromFilteredExpenses(expenses, e -> e.getCategory().equals(category) && e.getDate().compareTo(startOfWeek) >= 0 && e.getDate().compareTo(endOfWeek) <= 0);
		}
			
		createBudgetInstance(budget, budgetInstances, startOfWeek, endOfWeek, totalExpenses);
	}

	private void createMonthlyBudgetInstance(List<Expense> expenses, List<BudgetInstance> budgetInstances, Budget budget, String type, String category) {
		LocalDate today = LocalDate.now();
		BigDecimal totalExpenses;
		LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
		
		if (type.equals("Overall")) {
			totalExpenses = getTotalFromFilteredExpenses(expenses, e -> e.getDate().compareTo(startOfMonth) >= 0 && e.getDate().compareTo(endOfMonth) <= 0);		
		} else {
			totalExpenses = getTotalFromFilteredExpenses(expenses, e -> e.getCategory().equals(category) && e.getDate().compareTo(startOfMonth) >= 0 && e.getDate().compareTo(endOfMonth) <= 0);
		}
		
		createBudgetInstance(budget, budgetInstances, startOfMonth, endOfMonth, totalExpenses);
	}

	private void createQuarterlyBudgetInstance(List<Expense> expenses, List<BudgetInstance> budgetInstances, Budget budget, String type, String category) {
		LocalDate today = LocalDate.now();
		BigDecimal totalExpenses;
		int currMonth = today.getMonthValue();
		LocalDate startOfQuarter;
		LocalDate endOfQuarter;
		if (currMonth >= 1 && currMonth <= 3) {
			startOfQuarter = LocalDate.of(today.getYear(), 1, 1);
			endOfQuarter = LocalDate.of(today.getYear(), 3, 31);
		} else if (currMonth >= 4 && currMonth <= 6) {
			startOfQuarter = LocalDate.of(today.getYear(), 4, 1);
			endOfQuarter = LocalDate.of(today.getYear(), 6, 30);
		} else if (currMonth >= 7 && currMonth <= 9) {
			startOfQuarter = LocalDate.of(today.getYear(), 7, 1);
			endOfQuarter = LocalDate.of(today.getYear(), 9, 30);
		} else {
			startOfQuarter = LocalDate.of(today.getYear(), 10, 1);
			endOfQuarter = LocalDate.of(today.getYear(), 12, 31);
		}
		
		if (type.equals("Overall")) {
			totalExpenses = getTotalFromFilteredExpenses(expenses, e -> e.getDate().compareTo(startOfQuarter) >= 0 && e.getDate().compareTo(endOfQuarter) <= 0);		
		} else {
			totalExpenses = getTotalFromFilteredExpenses(expenses, e -> e.getCategory().equals(category) && e.getDate().compareTo(startOfQuarter) >= 0 && e.getDate().compareTo(endOfQuarter) <= 0);
		}
		
		createBudgetInstance(budget, budgetInstances, startOfQuarter, endOfQuarter, totalExpenses);
	}

	private void createYearlyBudgetInstance(List<Expense> expenses, List<BudgetInstance> budgetInstances, Budget budget, String type, String category) {
		LocalDate today = LocalDate.now();
		BigDecimal totalExpenses;
		LocalDate startOfYear = LocalDate.of(today.getYear(), 1, 1);
		LocalDate endOfYear = LocalDate.of(today.getYear(), 12, 31);
		
		if (type.equals("Overall")) {
			totalExpenses = getTotalFromFilteredExpenses(expenses, e -> e.getDate().compareTo(startOfYear) >= 0 && e.getDate().compareTo(endOfYear) <= 0);		
		} else {
			totalExpenses = getTotalFromFilteredExpenses(expenses, e -> e.getCategory().equals(category) && e.getDate().compareTo(startOfYear) >= 0 && e.getDate().compareTo(endOfYear) <= 0);
		}

		createBudgetInstance(budget, budgetInstances, startOfYear, endOfYear, totalExpenses);
	}
}
