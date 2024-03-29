package com.electronics_store.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	
	@Value("${myapp.secret}")
	private String secret;
	
	@Value("${myapp.access.expiry}")
	private long acessExpirationInSeconds;
	
	@Value("${myapp.refresh.expiry}")
	private long refreshExpirationInSeconds;
	
	public String generateAccessToken(String username)
	{
		return generateJWT(new HashMap<String,Object>(), username,acessExpirationInSeconds*1000l);
	}
	
	public String generateRefreshToken(String username)
	{
		return generateJWT(new HashMap<String,Object>(), username,refreshExpirationInSeconds*1000l);
	}

	
	
	private String generateJWT(Map<String,Object> claims ,String userName,Long expiry)
	{
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(userName)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date( System.currentTimeMillis()+expiry))
				.signWith(getSignature(),SignatureAlgorithm.HS512) // Signing the JWT with key
				.compact();
	}

	private Key getSignature()
	{
		byte[] secretBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(secretBytes);
	}
	
}
