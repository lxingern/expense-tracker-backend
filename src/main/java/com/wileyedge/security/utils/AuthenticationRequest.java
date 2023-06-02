package com.wileyedge.security.utils;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AuthenticationRequest {

	@Id
	private int id;
	
	private String email;
	private String password;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
