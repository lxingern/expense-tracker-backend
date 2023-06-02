package com.wileyedge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wileyedge.dao.UserRepository;
import com.wileyedge.model.User;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepo;
	
	@Override
	public User findUserByEmail(String email) {
		return userRepo.findByEmail(email).get();
	}

}
