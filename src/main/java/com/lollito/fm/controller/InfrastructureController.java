package com.lollito.fm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.FacilityType;
import com.lollito.fm.model.MaintenanceRecord;
import com.lollito.fm.model.dto.FacilityUpgradeDTO;
import com.lollito.fm.model.dto.InfrastructureOverviewDTO;
import com.lollito.fm.model.dto.ScheduleMaintenanceRequest;
import com.lollito.fm.model.dto.StartUpgradeRequest;
import com.lollito.fm.model.dto.UpgradeOptionDTO;
import com.lollito.fm.service.InfrastructureService;

@RestController
@RequestMapping("/api/infrastructure")
public class InfrastructureController {

    @Autowired
    private InfrastructureService infrastructureService;

    @GetMapping("/club/{clubId}/overview")
    public ResponseEntity<InfrastructureOverviewDTO> getInfrastructureOverview(@PathVariable Long clubId) {
        InfrastructureOverviewDTO overview = infrastructureService.getInfrastructureOverview(clubId);
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/club/{clubId}/upgrades/{facilityType}")
    public ResponseEntity<List<UpgradeOptionDTO>> getAvailableUpgrades(
            @PathVariable Long clubId,
            @PathVariable FacilityType facilityType) {
        List<UpgradeOptionDTO> upgrades = infrastructureService.getAvailableUpgrades(clubId, facilityType);
        return ResponseEntity.ok(upgrades);
    }

    @PostMapping("/club/{clubId}/upgrade/start")
    public ResponseEntity<FacilityUpgradeDTO> startUpgrade(
            @PathVariable Long clubId,
            @RequestBody StartUpgradeRequest request) {
        FacilityUpgradeDTO upgrade = infrastructureService.startUpgrade(clubId, request);
        return ResponseEntity.ok(upgrade);
    }

    @PostMapping("/upgrade/{upgradeId}/complete")
    public ResponseEntity<Void> completeUpgrade(@PathVariable Long upgradeId) {
        infrastructureService.completeUpgrade(upgradeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/club/{clubId}/maintenance/schedule")
    public ResponseEntity<List<MaintenanceRecord>> getMaintenanceSchedule(@PathVariable Long clubId) {
        List<MaintenanceRecord> maintenance = infrastructureService.getMaintenanceSchedule(clubId);
        return ResponseEntity.ok(maintenance);
    }

    @PostMapping("/club/{clubId}/maintenance/schedule")
    public ResponseEntity<MaintenanceRecord> scheduleMaintenance(
            @PathVariable Long clubId,
            @RequestBody ScheduleMaintenanceRequest request) {
        MaintenanceRecord maintenance = infrastructureService.scheduleMaintenance(clubId, request);
        return ResponseEntity.ok(maintenance);
    }
}
