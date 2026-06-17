package com.payment.service;



import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.payment.entity.User;
import com.payment.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService{
	
	 private final UserRepository repo;

	    public CustomUserDetailsService(
	            UserRepository repo) {

	        this.repo = repo;
	    }

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = repo.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("User not found"));
		
		return org.springframework.security.core.userdetails
                .User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
	}

}
