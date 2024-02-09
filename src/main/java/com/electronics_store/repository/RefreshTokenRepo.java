package com.electronics_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.electronics_store.entity.RefreshToken;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken,Long>{

}
