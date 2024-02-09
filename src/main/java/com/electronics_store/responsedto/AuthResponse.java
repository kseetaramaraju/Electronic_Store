package com.electronics_store.responsedto;

import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

	private int userId;
	private String userName;
	private String userEmail;
	private String userRole;
	private boolean isAthenticated;
    private LocalDateTime accessExpiration;
    private LocalDateTime refreshExpiration;
}
