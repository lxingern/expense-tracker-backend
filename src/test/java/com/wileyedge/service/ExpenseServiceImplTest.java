package com.wileyedge.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
	void getExpensesWithTotalThrowsInvalidInputExceptionIfStartDateXOrEndDateNotProvided() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		
		assertThrows(InvalidInputException.class, () -> service.getExpensesWithTotal(user, "2023-06-10", null, null));
		assertThrows(InvalidInputException.class, () -> service.getExpensesWithTotal(user, null, "2023-06-12", null));
	}
	
	@Test
	void getExpensesWithTotalQueriesForCurrentMonthAndAllCategoriesExpensesIfNoFiltersProvided() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Calendar cal = mock(Calendar.class);
		when(cal.get(Calendar.MONTH)).thenReturn(5);
		when(cal.get(Calendar.YEAR)).thenReturn(2023);
		when(cal.getActualMaximum(Calendar.DATE)).thenReturn(30);
		List<String> categories = Expense.getCategories();
		
		service.getExpensesWithTotal(user, null, null, null);
		
		verify(repo).findFilteredByUserId(user, LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30), categories);	
	}
	
	@Test
	void getExpensesWithTotalCorrectlyQueriesExpensesWithFiltersProvided() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		ArrayList<String> categories = new ArrayList<String>(Arrays.asList("Leisure", "Food and Drink"));
		
		service.getExpensesWithTotal(user, "2023-06-10", "2023-06-12", categories);
		
		verify(repo).findFilteredByUserId(user, LocalDate.of(2023, 6, 10), LocalDate.of(2023, 6, 12), categories);
	}
	
	@Test
	void getExpensesWithTotalCalculatesTotalCorrectly() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense ex1 = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", user);
		Expense ex2 = new Expense(2, LocalDate.of(2023, 6, 12), new BigDecimal("4.23"), "Food and Drink", "Mixed rice", user);
		Expense ex3 = new Expense(3, LocalDate.of(2023, 6, 12), new BigDecimal("65.70"), "Utilities and Bills", "Electricity bill", user);
		List<Expense> expenses = new ArrayList<>(Arrays.asList(ex1, ex2, ex3));
		List<String> categories = Expense.getCategories();
		when(repo.findFilteredByUserId(user, LocalDate.of(2023, 6, 10),	LocalDate.of(2023, 6, 12), categories)).thenReturn(expenses);

		ObjectNode actualResult = service.getExpensesWithTotal(user, "2023-06-10", "2023-06-12", null);
		
		assertEquals(new BigDecimal("87.93"), actualResult.get("totalAmount").decimalValue());
	}
	
	@Test
	void getExpensesWithTotalReturnsCorrectDetailsWithFiltersProvided() {
		// Prepare inputs
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense ex1 = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", user);
		Expense ex2 = new Expense(2, LocalDate.of(2023, 6, 12), new BigDecimal("4.23"), "Food and Drink", "Mixed rice", user);
		Expense ex3 = new Expense(3, LocalDate.of(2023, 6, 12), new BigDecimal("65.70"), "Utilities and Bills", "Electricity bill", user);
		List<Expense> expenses = new ArrayList<>(Arrays.asList(ex1, ex2, ex3));
		List<String> categories = new ArrayList<>(Arrays.asList("Leisure", "Food and Drink"));
		when(repo.findFilteredByUserId(user, LocalDate.of(2023, 6, 10),	LocalDate.of(2023, 6, 12), categories)).thenReturn(expenses);
		
		// Prepare expected output
		ObjectNode expectedResult = mapper.createObjectNode();
		JsonNode expensesNode = mapper.valueToTree(expenses);
		expectedResult.put("totalAmount", new BigDecimal("87.93"));
		expectedResult.set("expenses", expensesNode);
		expectedResult.put("startDate", "2023-06-10");
		expectedResult.put("endDate", "2023-06-12");
		JsonNode categoriesNode = mapper.valueToTree(categories);
		expectedResult.set("categories", categoriesNode);
		
		ObjectNode actualResult = service.getExpensesWithTotal(user, "2023-06-10", "2023-06-12", categories);
		
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	void getExpensesWithTotalReturnsCorrectDetailsWithNoFiltersProvided() {
		// Prepare inputs
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Expense ex1 = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", user);
		Expense ex2 = new Expense(2, LocalDate.of(2023, 6, 12), new BigDecimal("4.23"), "Food and Drink", "Mixed rice", user);
		Expense ex3 = new Expense(3, LocalDate.of(2023, 6, 12), new BigDecimal("65.70"), "Utilities and Bills", "Electricity bill", user);
		List<Expense> expenses = new ArrayList<>(Arrays.asList(ex1, ex2, ex3));
		Calendar cal = mock(Calendar.class);
		when(cal.get(Calendar.MONTH)).thenReturn(5);
		when(cal.get(Calendar.YEAR)).thenReturn(2023);
		when(cal.getActualMaximum(Calendar.DATE)).thenReturn(30);
		List<String> categories = Expense.getCategories();
		when(repo.findFilteredByUserId(user, LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30), categories)).thenReturn(expenses);
		
		// Prepare expected output
		ObjectNode expectedResult = mapper.createObjectNode();
		JsonNode expensesNode = mapper.valueToTree(expenses);
		expectedResult.put("totalAmount", new BigDecimal("87.93"));
		expectedResult.set("expenses", expensesNode);
		expectedResult.put("startDate", "2023-06-01");
		expectedResult.put("endDate", "2023-06-30");
		JsonNode categoriesNode = mapper.valueToTree(categories);
		expectedResult.set("categories", categoriesNode);
		
		ObjectNode actualResult = service.getExpensesWithTotal(user, null, null, null);
		
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	void getExpensesWithTotalReturnsEmptyExpensesListAndZeroTotalIfNoExpensesFound() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		Calendar cal = mock(Calendar.class);
		when(cal.get(Calendar.MONTH)).thenReturn(5);
		when(cal.get(Calendar.YEAR)).thenReturn(2023);
		when(cal.getActualMaximum(Calendar.DATE)).thenReturn(30);
		List<String> categories = Expense.getCategories();
		when(repo.findFilteredByUserId(user, LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30), categories)).thenReturn(new ArrayList<>());

		ObjectNode actualResult = service.getExpensesWithTotal(user, null, null, null);
		
		assertEquals(new BigDecimal("0"), actualResult.get("totalAmount").decimalValue());
		assertTrue(actualResult.get("expenses").isEmpty());
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
