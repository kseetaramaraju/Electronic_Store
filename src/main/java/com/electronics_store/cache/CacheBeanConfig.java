package com.electronics_store.cache;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.electronics_store.entity.User;

@Configuration
public class CacheBeanConfig {

	@Bean
	 CacheStore<User> userCacheStore()
	{
		return new CacheStore<User>(Duration.ofMinutes(5));
	}
	
	@Bean
	 CacheStore<String> otpCacheStore()
	{
		return new CacheStore<String>(Duration.ofMinutes(1));
	}
	
	
}
