package com.wileyedge.security.utils;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AuthenticationResponse {
	
	@Id
	private int id;
	
	private String token;

	public AuthenticationResponse(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
}
