package com.lollito.fm.repository.rest;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.MetricType;
import com.lollito.fm.model.PerformanceMetric;

@Repository
public interface PerformanceMetricRepository extends JpaRepository<PerformanceMetric, Long> {
    List<PerformanceMetric> findByRecordedAtBetweenAndMetricTypeIn(LocalDateTime start, LocalDateTime end, Collection<MetricType> metricTypes);
}
