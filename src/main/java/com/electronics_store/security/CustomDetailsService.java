package com.electronics_store.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.electronics_store.exception.UserNotFound;
import com.electronics_store.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CustomDetailsService implements UserDetailsService {
	
	private UserRepository userRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	  return userRepo.findByUserName(username).map(user -> new CustomDetails(user) )
			  .orElseThrow( ()-> new UserNotFound("User Not Found!!") );
	
	}
	
	

}
