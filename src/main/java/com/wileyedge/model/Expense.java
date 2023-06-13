package com.wileyedge.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
	
	@Column
	private String description;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	
	public Expense() {
	}

	public Expense(int id, LocalDate date, BigDecimal amount, String category, String description) {
		this.id = id;
		this.date = date;
		this.amount = amount;
		this.category = category;
		this.description = description;
	}

	public Expense(LocalDate date, BigDecimal amount, String category, String description) {
		this.date = date;
		this.amount = amount;
		this.category = category;
		this.description = description;
	}

	public Expense(LocalDate date, BigDecimal amount, String category, String description, User user) {
		this.date = date;
		this.amount = amount;
		this.category = category;
		this.description = description;
		this.user = user;
	}

	public Expense(int id, LocalDate date, BigDecimal amount, String category, String description, User user) {
		this.id = id;
		this.date = date;
		this.amount = amount;
		this.category = category;
		this.description = description;
		this.user = user;
	}

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
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Expense [id=" + id + ", date=" + date + ", amount=" + amount + ", category=" + category + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, category, date, description, id, user);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Expense other = (Expense) obj;
		return Objects.equals(amount, other.amount) && Objects.equals(category, other.category)
				&& Objects.equals(date, other.date) && Objects.equals(description, other.description) && id == other.id
				&& Objects.equals(user, other.user);
	}
}
