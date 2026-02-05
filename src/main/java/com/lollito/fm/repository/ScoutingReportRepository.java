package com.lollito.fm.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.RecommendationLevel;
import com.lollito.fm.model.ScoutingReport;

@Repository
public interface ScoutingReportRepository extends JpaRepository<ScoutingReport, Long> {

    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.scout.club = :club AND sr.reportDate >= :date AND sr.recommendation IN ('RECOMMEND', 'PRIORITY') ORDER BY sr.reportDate DESC")
    List<ScoutingReport> findRecentRecommendedReports(@Param("club") Club club, @Param("date") LocalDate date);

    Page<ScoutingReport> findByScout_Club_Id(Long clubId, Pageable pageable);

    Page<ScoutingReport> findByScout_Club_IdAndRecommendation(Long clubId, RecommendationLevel recommendation, Pageable pageable);
}
