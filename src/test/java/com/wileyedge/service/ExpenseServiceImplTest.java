package com.wileyedge.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wileyedge.dao.ExpenseRepository;
import com.wileyedge.exceptions.ExpenseNotFoundException;
import com.wileyedge.exceptions.InvalidInputException;
import com.wileyedge.exceptions.UserNotAuthorizedException;
import com.wileyedge.model.Expense;
import com.wileyedge.model.User;

class ExpenseServiceImplTest {
	
	ExpenseRepository repo;
	ObjectMapper mapper;
	ExpenseService service;

	@BeforeEach
	void setUp() throws Exception {
		repo = mock(ExpenseRepository.class);
		mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		service = new ExpenseServiceImpl(repo, mapper);
	}

	@Test
	void getExpensesWithTotalReturnsCorrectSortedExpensesAndTotalForUser() {
		// Prepare inputs
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense ex1 = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", user);
		Expense ex2 = new Expense(2, LocalDate.of(2023, 6, 12), new BigDecimal("4.23"), "Food and Drink", "Mixed rice", user);
		List<Expense> expenses = new ArrayList<>();
		expenses.add(ex1);
		expenses.add(ex2);
		
		// Prepare expected output
		when(repo.findAllByUserId(user.getId())).thenReturn(expenses);
		ObjectNode expectedResult = mapper.createObjectNode();
		List<Expense> sortedExpenses = new ArrayList<>();
		sortedExpenses.add(ex2);
		sortedExpenses.add(ex1);
		JsonNode expensesNode = mapper.valueToTree(sortedExpenses);
		expectedResult.put("totalAmount", new BigDecimal("22.23"));
		expectedResult.set("expenses", expensesNode);

		ObjectNode actualResult = service.getExpensesWithTotal(user);
		
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	void getExpensesWithTotalReturnsEmptyExpensesListAndZeroTotalIfNoExpensesFound() {
		// Prepare inputs
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		
		// Prepare expected output
		when(repo.findAllByUserId(user.getId())).thenReturn(new ArrayList<>());
		ObjectNode expectedResult = mapper.createObjectNode();
		List<Expense> expenses = new ArrayList<>();
		JsonNode expensesNode = mapper.valueToTree(expenses);
		expectedResult.put("totalAmount", new BigDecimal("0"));
		expectedResult.set("expenses", expensesNode);

		ObjectNode actualResult = service.getExpensesWithTotal(user);
		
		assertEquals(expectedResult, actualResult);
	}

	@Test
	void createExpenseSuccessfullyCreatesExpense() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense newExpense = new Expense(LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket");
		Expense expectedSavedExpense = new Expense(LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", user);
		
		service.createExpense(newExpense, user);
		
		verify(repo).save(expectedSavedExpense);
	}
	
	@Test
	void createExpenseThrowsInvalidInputExceptionIfAmountIsNegative() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense newExpense = new Expense(LocalDate.of(2023, 6, 10), new BigDecimal("-5.00"), "Leisure", "Movie ticket");
		
		assertThrows(InvalidInputException.class, () -> service.createExpense(newExpense, user));
	}
	
	@Test
	void createExpenseThrowsInvalidInputExceptionIfCategoryNotInList() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense newExpense = new Expense(LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Fun", "Movie ticket");
		
		assertThrows(InvalidInputException.class, () -> service.createExpense(newExpense, user));
	}
	
	@Test
	void updateExpenseSuccessfullyUpdatesExpense() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense expenseToUpdate = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", user);
		Expense updatedExpenseFromUser = new Expense(LocalDate.of(2023, 6, 10), new BigDecimal("12.00"), "Leisure", "Movie ticket");
		when(repo.findById(1)).thenReturn(Optional.of(expenseToUpdate));
		Expense expectedSavedExpense = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("12.00"), "Leisure", "Movie ticket", user);
		
		service.updateExpense(1, updatedExpenseFromUser, user);
		
		verify(repo).save(expectedSavedExpense);
	}
	
	@Test
	void updateExpenseThrowsInvalidInputExceptionIfExpenseIdsInPathAndRequestBodyDoNotMatch() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense updatedExpenseFromUser = new Expense(2, LocalDate.of(2023, 6, 10), new BigDecimal("12.00"), "Leisure", "Movie ticket");
		
		assertThrows(InvalidInputException.class, () -> service.updateExpense(1, updatedExpenseFromUser, user));
	}
	
	@Test
	void updateExpenseThrowsExpenseNotFoundExceptionIfNoExpenseWithMatchingId() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense updatedExpenseFromUser = new Expense(LocalDate.of(2023, 6, 10), new BigDecimal("12.00"), "Leisure", "Movie ticket");
		when(repo.findById(1)).thenReturn(Optional.empty());
		
		assertThrows(ExpenseNotFoundException.class, () -> service.updateExpense(1, updatedExpenseFromUser, user));
	}
	
	@Test
	void updateExpenseThrowsUserNotAuthorizedExceptionIfUserIsNotOwnerOfExpense() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		User ownerOfExpense = new User(2, "Amy Smith", "amy@email.com", "Password123");
		Expense updatedExpenseFromUser = new Expense(LocalDate.of(2023, 6, 10), new BigDecimal("12.00"), "Leisure", "Movie ticket");
		Expense expenseToUpdate = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", ownerOfExpense);
		when(repo.findById(1)).thenReturn(Optional.of(expenseToUpdate));
		
		assertThrows(UserNotAuthorizedException.class, () -> service.updateExpense(1, updatedExpenseFromUser, user));
	}
	
	@Test
	void updateExpenseThrowsInvalidInputExceptionIfUpdatedAmountIsNegative() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense updatedExpenseFromUser = new Expense(LocalDate.of(2023, 6, 10), new BigDecimal("-12.00"), "Leisure", "Movie ticket");
		Expense expenseToUpdate = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", user);
		when(repo.findById(1)).thenReturn(Optional.of(expenseToUpdate));
		
		assertThrows(InvalidInputException.class, () -> service.updateExpense(1, updatedExpenseFromUser, user));
	}
	
	@Test
	void updateExpenseThrowsInvalidInputExceptionIfCategoryNotInList() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense updatedExpenseFromUser = new Expense(LocalDate.of(2023, 6, 10), new BigDecimal("12.00"), "Fun", "Movie ticket");
		Expense expenseToUpdate = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", user);
		when(repo.findById(1)).thenReturn(Optional.of(expenseToUpdate));
		
		assertThrows(InvalidInputException.class, () -> service.updateExpense(1, updatedExpenseFromUser, user));
	}
	
	@Test
	void deleteExpenseSuccessfullyDeletesExpense() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense expenseToDelete = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", user);
		when(repo.findById(1)).thenReturn(Optional.of(expenseToDelete));
		
		service.deleteExpense(1, user);
		
		verify(repo).deleteById(1);
	}
	
	@Test
	void deleteExpenseThrowsExpenseNotFoundExceptionIfNoExpenseWithMatchingId() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		when(repo.findById(1)).thenReturn(Optional.empty());
		
		assertThrows(ExpenseNotFoundException.class, () -> service.deleteExpense(1, user));
	}
	
	@Test
	void deleteExpenseThrowsUserNotAuthorizedExceptionIfUserIsNotOwnerOfExpense() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		User ownerOfExpense = new User(2, "Amy Smith", "amy@email.com", "Password123");
		Expense expenseToDelete = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", ownerOfExpense);
		when(repo.findById(1)).thenReturn(Optional.of(expenseToDelete));
		
		assertThrows(UserNotAuthorizedException.class, () -> service.deleteExpense(1, user));
	}
}
