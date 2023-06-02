package com.wileyedge.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Expense {

	private static List<String> categories = Arrays.asList("Food and Drink", "Utilities and Bills", "Transport", "Leisure");
	
	@Id
	@GeneratedValue
	private int id;
	
	@Column
	private LocalDate date;
	
	@Column
	private BigDecimal amount;
	
	@Column
	private String category;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public static List<String> getCategories() {
		return categories;
	}

	@Override
	public String toString() {
		return "Expense [id=" + id + ", date=" + date + ", amount=" + amount + ", category=" + category + "]";
	}
	
}
