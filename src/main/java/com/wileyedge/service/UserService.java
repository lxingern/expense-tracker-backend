package com.wileyedge.service;

import com.wileyedge.model.User;

public interface UserService {

	User findUserByEmail(String email);
	
}
