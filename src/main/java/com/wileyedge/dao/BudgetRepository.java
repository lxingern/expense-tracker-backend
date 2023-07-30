package com.wileyedge.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wileyedge.model.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Integer>{

}
