package com.wileyedge.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wileyedge.model.Expense;
import com.wileyedge.model.User;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

	@Query(value = "SELECT e FROM Expense e WHERE user = ?1 AND e.date BETWEEN ?2 AND ?3 AND e.category IN (?4) ORDER BY e.date DESC")
	List<Expense> findFilteredByUserId(User user, LocalDate startDate, LocalDate endDate, List<String> categories);
	
}
