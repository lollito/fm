package com.lollito.fm.repository.rest;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
	public Boolean existsByUsername(String username);
	public Boolean existsByEmail(String email);
	
	Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email);
	
	User findByUsernameAndActive(String username, boolean active);

	long countByIsActive(Boolean isActive);
	long countByIsVerified(Boolean isVerified);
	long countByIsBanned(Boolean isBanned);
	long countByCreatedDateAfter(java.time.LocalDateTime date);
	boolean existsByEmailAndIdNot(String email, Long id);
}