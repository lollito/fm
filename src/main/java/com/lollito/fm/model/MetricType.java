package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum MetricType {
    DATABASE_QUERY_TIME("Database Query Time"),
    MEMORY_USAGE("Memory Usage"),
    CPU_USAGE("CPU Usage"),
    RESPONSE_TIME("Response Time"),
    THROUGHPUT("Throughput"),
    ERROR_RATE("Error Rate"),
    ACTIVE_SESSIONS("Active Sessions"),
    MATCH_SIMULATION_TIME("Match Simulation Time");

    private final String displayName;

    MetricType(String displayName) {
        this.displayName = displayName;
    }
}
