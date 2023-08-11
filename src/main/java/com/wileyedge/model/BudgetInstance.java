package com.wileyedge.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetInstance {
	private Budget budget;
	private LocalDate periodStart;
	private LocalDate periodEnd;
	private BigDecimal expenditure;
	private BigDecimal utilization;
	
	public BudgetInstance(Budget budget, LocalDate periodStart, LocalDate periodEnd, BigDecimal expenditure,
			BigDecimal utilization) {
		this.budget = budget;
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.expenditure = expenditure;
		this.utilization = utilization;
	}

	public Budget getBudget() {
		return budget;
	}

	public void setBudget(Budget budget) {
		this.budget = budget;
	}

	public LocalDate getPeriodStart() {
		return periodStart;
	}

	public void setPeriodStart(LocalDate periodStart) {
		this.periodStart = periodStart;
	}

	public LocalDate getPeriodEnd() {
		return periodEnd;
	}

	public void setPeriodEnd(LocalDate periodEnd) {
		this.periodEnd = periodEnd;
	}

	public BigDecimal getExpenditure() {
		return expenditure;
	}

	public void setExpenditure(BigDecimal expenditure) {
		this.expenditure = expenditure;
	}

	public BigDecimal getUtilization() {
		return utilization;
	}

	public void setUtilization(BigDecimal utilization) {
		this.utilization = utilization;
	}
	
}
