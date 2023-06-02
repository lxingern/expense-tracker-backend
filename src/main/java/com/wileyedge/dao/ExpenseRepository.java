package com.wileyedge.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wileyedge.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

	List<Expense> findAllByUserId(int userId);
	
}
