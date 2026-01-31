package com.lollito.fm.repository.rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Formation;

@Repository
public interface FormationRepository extends JpaRepository<Formation, Long> {

}