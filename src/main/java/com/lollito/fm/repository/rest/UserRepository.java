package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.User;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
	public Boolean existsByUsername(String username);
	public Boolean existsByEmail(String email);
	
	User findByUsername(String username);
}