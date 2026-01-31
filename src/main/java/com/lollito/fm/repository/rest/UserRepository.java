package com.lollito.fm.repository.rest;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	public Boolean existsByUsername(String username);
	public Boolean existsByEmail(String email);
	
	Optional<User> findByUsername(String username);
	
	User findByUsernameAndActive(String username, boolean active);
}