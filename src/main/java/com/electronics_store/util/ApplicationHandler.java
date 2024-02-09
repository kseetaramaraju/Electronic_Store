package com.electronics_store.util;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.electronics_store.exception.EmailAlreadyExist;
import com.electronics_store.exception.IllegelUserRoleException;

@RestControllerAdvice
public class ApplicationHandler {
	
	public ResponseEntity<Object> structure(HttpStatus status,String message,Object rootcause)
	{
		return new ResponseEntity<Object>(Map.of("Status",status.value(),"Message",message,"RootCause",rootcause),status);
	}
	
	@ExceptionHandler
	public ResponseEntity<Object> handleUserAlreadyExist(EmailAlreadyExist e)
	{
	   return structure(HttpStatus.BAD_REQUEST,e.getMessage(),"User with that email Already Exist");
	}

	@ExceptionHandler
	public ResponseEntity<Object> handleIllegelUserRoleException(IllegelUserRoleException e)
	{
	   return structure(HttpStatus.NOT_FOUND,e.getMessage(),"UserRole which is given not Exist");
	}

	@ExceptionHandler
	public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException e)
	{
	   return structure(HttpStatus.NOT_FOUND,e.getMessage(),"Username And Password are ");
	}

	
	

}
