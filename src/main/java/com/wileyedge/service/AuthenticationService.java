package com.wileyedge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wileyedge.dao.UserRepository;
import com.wileyedge.exceptions.InvalidCredentialsException;
import com.wileyedge.model.User;
import com.wileyedge.security.utils.AuthenticationRequest;
import com.wileyedge.security.utils.AuthenticationResponse;
import com.wileyedge.security.utils.JwtService;
import com.wileyedge.security.utils.RegisterRequest;

@Service
public class AuthenticationService {
	
	@Autowired
	UserRepository userRepo;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	JwtService jwtService;
	@Autowired
	AuthenticationManager authManager;

	public AuthenticationResponse register(RegisterRequest request) {
		if (request.getName() == null || request.getName().trim().length() == 0 ||
			request.getEmail() == null || request.getEmail().trim().length() == 0 ||
			request.getPassword() == null || request.getPassword().trim().length() == 0) {
			throw new InvalidCredentialsException("Name, email and password cannot be blank.");
		}
		
		User user = new User(
				request.getName(),
				request.getEmail(),
				passwordEncoder.encode(request.getPassword())
				);
				
		userRepo.save(user);
		
		String jwtToken = jwtService.generateToken(user);
		
		return new AuthenticationResponse(jwtToken);
	}

	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		authManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(), 
						request.getPassword()
				)
		);
		
		User user = userRepo.findByEmail(request.getEmail()).get();

		String jwtToken = jwtService.generateToken(user);
		
		return new AuthenticationResponse(jwtToken);
	}

}
