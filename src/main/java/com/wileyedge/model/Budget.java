package com.wileyedge.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Budget {

	@Id
	@GeneratedValue
	private int id;
	
	@Column
	private String type;
	
	@Column
	private String category;
	
	@Column
	private BigDecimal amount;
	
	@Column
	private Timeframe timeframe;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	public Budget(int id, String type, String category, BigDecimal amount, Timeframe timeframe, User user) {
		this.id = id;
		this.type = type;
		this.category = category;
		this.amount = amount;
		this.timeframe = timeframe;
		this.user = user;
	}
	
}
