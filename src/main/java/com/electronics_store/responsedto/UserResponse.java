package com.electronics_store.responsedto;

import com.electronics_store.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserResponse {
	
	private int userId;
	private String userName;
	private String userEmail;
	private UserRole userRole;
	private boolean isEmailVerified;
	private boolean isDeleted;


}
