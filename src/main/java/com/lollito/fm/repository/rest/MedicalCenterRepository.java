package com.lollito.fm.repository.rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.MedicalCenter;

@Repository
public interface MedicalCenterRepository extends JpaRepository<MedicalCenter, Long> {
}
