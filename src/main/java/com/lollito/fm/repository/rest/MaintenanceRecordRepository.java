package com.lollito.fm.repository.rest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.MaintenanceRecord;
import com.lollito.fm.model.MaintenanceStatus;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    List<MaintenanceRecord> findByClubAndStatusAndScheduledDateAfter(Club club, MaintenanceStatus status, LocalDate date);
}
