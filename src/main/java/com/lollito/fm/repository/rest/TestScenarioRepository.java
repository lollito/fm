package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.TestScenario;

@Repository
public interface TestScenarioRepository extends JpaRepository<TestScenario, Long> {
    List<TestScenario> findByIsActiveTrue();
}
