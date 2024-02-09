package com.electronics_store.entity;

import java.time.LocalDateTime;

import com.electronics_store.enums.UserRole;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Access_Token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessToken {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long tokenId;
	private String token;
	private boolean isBlocked;
	private LocalDateTime expiration;

}
