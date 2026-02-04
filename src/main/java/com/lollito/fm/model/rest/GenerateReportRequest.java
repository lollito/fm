package com.lollito.fm.model.rest;

import java.time.LocalDate;

import com.lollito.fm.model.ReportType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenerateReportRequest {
    private ReportType reportType;
    private LocalDate startDate;
    private LocalDate endDate;
}
