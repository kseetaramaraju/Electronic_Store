package com.electronics_store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.electronics_store.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

	boolean existsByUserEmail(String userEmail);

	Optional<User>  findByUserName(String name);

	List<User> findByIsDeleted(boolean b);
	
}
