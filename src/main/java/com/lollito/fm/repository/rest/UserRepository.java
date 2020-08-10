package com.lollito.fm.repository.rest;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.User;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
	public Boolean existsByUsername(String username);
	public Boolean existsByEmail(String email);
	
	Optional<User> findByUsername(String username);
	
	User findByUsernameAndActive(String username, boolean active);
}