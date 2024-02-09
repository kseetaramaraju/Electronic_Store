package com.electronics_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.electronics_store.entity.AccessToken;

public interface AccessTokenRepo extends JpaRepository<AccessToken,Long> {

}
