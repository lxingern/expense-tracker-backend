package com.wileyedge.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.wileyedge.model.Expense;
import com.wileyedge.model.User;

@SpringBootTest
class ExpenseRepositoryTest {

	@Autowired
	ExpenseRepository expenseRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@BeforeEach
	void setUp() throws Exception {
		User user1 = new User("John Doe", "john@email.com", "Password123");
		user1 = userRepo.save(user1);
		User user2 = new User("Jill Smith", "jill@email.com", "Password123");
		user2 = userRepo.save(user2);
		
		Expense ex1 = new Expense(LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", user1);
		expenseRepo.save(ex1);
		Expense ex2 = new Expense(LocalDate.of(2023, 6, 12), new BigDecimal("4.23"), "Food and Drink", "Mixed rice", user1);
		expenseRepo.save(ex2);
		Expense ex3 = new Expense(LocalDate.of(2023, 6, 12), new BigDecimal("65.70"), "Utilities and Bills", "Electricity bill", user1);
		expenseRepo.save(ex3);
		Expense ex4 = new Expense(LocalDate.of(2023, 6, 14), new BigDecimal("15.30"), "Food and Drink", "Brunch", user1);
		expenseRepo.save(ex4);
		Expense ex5 = new Expense(LocalDate.of(2023, 6, 11), new BigDecimal("12.30"), "Transport", "Grab", user2);
		expenseRepo.save(ex5);
	}

	@Test
	void findFilteredByUserIdCorrectlyReturnsFilteredRecords() {
		User user = new User(1, "John Doe", "john@email.com", "Password123");
		List<String> categoriesToFilterBy = new ArrayList<>(Arrays.asList("Leisure", "Food and Drink"));
		Expense ex2 = new Expense(2, LocalDate.of(2023, 6, 12), new BigDecimal("4.23"), "Food and Drink", "Mixed rice", user);
		Expense ex1 = new Expense(1, LocalDate.of(2023, 6, 10), new BigDecimal("18.00"), "Leisure", "Movie ticket", user);
		List<Expense> expectedExpenses = new ArrayList<>(Arrays.asList(ex2, ex1));
		
		List<Expense> actualExpenses = expenseRepo.findFilteredByUserId(user, LocalDate.of(2023, 6, 10), LocalDate.of(2023, 6, 12), categoriesToFilterBy);
		
		assertEquals(expectedExpenses.size(), actualExpenses.size());
		assertEquals(expectedExpenses, actualExpenses);
	}

}
