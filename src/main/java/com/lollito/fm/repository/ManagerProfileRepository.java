package com.lollito.fm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.ManagerProfile;

@Repository
public interface ManagerProfileRepository extends JpaRepository<ManagerProfile, Long> {

    Optional<ManagerProfile> findByUserId(Long userId);

}
