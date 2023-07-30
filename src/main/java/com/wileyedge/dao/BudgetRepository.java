package com.wileyedge.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wileyedge.model.Budget;
import com.wileyedge.model.User;

public interface BudgetRepository extends JpaRepository<Budget, Integer>{
	List<Budget> findByUser(User user);
}
