package com.electronics_store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.electronics_store.requestdto.AuthRequest;
import com.electronics_store.requestdto.OtpModel;
import com.electronics_store.requestdto.UserRequest;
import com.electronics_store.responsedto.AuthResponse;
import com.electronics_store.responsedto.UserResponse;
import com.electronics_store.service.AuthService;
import com.electronics_store.util.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
public class AuthController {
	
	@Autowired
	private AuthService authService;
	
	@PostMapping("/register")
	public ResponseEntity<ResponseStructure<UserResponse>> register(@RequestBody @Valid UserRequest userRequest) throws MessagingException
	{
		return authService.register(userRequest);
	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(@RequestBody OtpModel otpModel)
	{
		return authService.verifyOTP(otpModel);
	}

	@PostMapping("/login")
	public ResponseEntity<ResponseStructure<AuthResponse>> login(@RequestBody AuthRequest authRequest,HttpServletResponse response)
	{
		return authService.login(authRequest,response);
	}
}
