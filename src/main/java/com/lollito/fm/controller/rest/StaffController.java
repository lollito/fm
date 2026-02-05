package com.lollito.fm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.dto.StaffBonusesDTO;
import com.lollito.fm.dto.StaffContractDTO;
import com.lollito.fm.dto.StaffDTO;
import com.lollito.fm.dto.request.FireStaffRequest;
import com.lollito.fm.dto.request.HireStaffRequest;
import com.lollito.fm.dto.request.RenewContractRequest;
import com.lollito.fm.model.Staff;
import com.lollito.fm.model.StaffContract;
import com.lollito.fm.model.StaffRole;
import com.lollito.fm.service.StaffService;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<StaffDTO>> getClubStaff(@PathVariable Long clubId) {
        List<Staff> staff = staffService.getClubStaff(clubId);
        return ResponseEntity.ok(staff.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/available")
    public ResponseEntity<List<StaffDTO>> getAvailableStaff(
            @RequestParam(required = false) StaffRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Staff> staff = staffService.getAvailableStaff(role, PageRequest.of(page, size));
        return ResponseEntity.ok(staff.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @PostMapping("/hire")
    public ResponseEntity<StaffContractDTO> hireStaff(@RequestBody HireStaffRequest request) {
        StaffContract contract = staffService.hireStaff(
            request.getClubId(),
            request.getStaffId(),
            request
        );
        return ResponseEntity.ok(convertToContractDTO(contract));
    }

    @PostMapping("/{staffId}/fire")
    public ResponseEntity<Void> fireStaff(
            @PathVariable Long staffId,
            @RequestBody FireStaffRequest request) {
        staffService.fireStaff(staffId, request.getReason());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{staffId}/renew")
    public ResponseEntity<StaffContractDTO> renewContract(
            @PathVariable Long staffId,
            @RequestBody RenewContractRequest request) {
        StaffContract contract = staffService.renewContract(staffId, request);
        return ResponseEntity.ok(convertToContractDTO(contract));
    }

    @GetMapping("/club/{clubId}/bonuses")
    public ResponseEntity<StaffBonusesDTO> getClubStaffBonuses(@PathVariable Long clubId) {
        StaffBonusesDTO bonuses = staffService.calculateClubStaffBonuses(clubId);
        return ResponseEntity.ok(bonuses);
    }

    @PostMapping("/generate/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffDTO>> generateStaff(
            @PathVariable StaffRole role,
            @RequestParam(defaultValue = "10") int count) {
        List<Staff> staff = staffService.generateAvailableStaff(role, count);
        return ResponseEntity.ok(staff.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    private StaffDTO convertToDTO(Staff staff) {
        StaffDTO dto = new StaffDTO();
        dto.setId(staff.getId());
        dto.setName(staff.getName());
        dto.setSurname(staff.getSurname());
        dto.setBirth(staff.getBirth());
        dto.setAge(staff.getAge());
        dto.setClubId(staff.getClub() != null ? staff.getClub().getId() : null);
        dto.setRole(staff.getRole());
        dto.setSpecialization(staff.getSpecialization());
        dto.setAbility(staff.getAbility());
        dto.setReputation(staff.getReputation());
        dto.setMonthlySalary(staff.getMonthlySalary());
        dto.setContractStart(staff.getContractStart());
        dto.setContractEnd(staff.getContractEnd());
        dto.setStatus(staff.getStatus());
        dto.setMotivationBonus(staff.getMotivationBonus());
        dto.setTrainingBonus(staff.getTrainingBonus());
        dto.setInjuryPreventionBonus(staff.getInjuryPreventionBonus());
        dto.setRecoveryBonus(staff.getRecoveryBonus());
        dto.setScoutingBonus(staff.getScoutingBonus());
        dto.setNationalityName(staff.getNationality() != null ? staff.getNationality().getName() : null);
        dto.setDescription(staff.getDescription());
        dto.setExperience(staff.getExperience());
        return dto;
    }

    private StaffContractDTO convertToContractDTO(StaffContract contract) {
        StaffContractDTO dto = new StaffContractDTO();
        dto.setId(contract.getId());
        dto.setStaffId(contract.getStaff().getId());
        dto.setClubId(contract.getClub().getId());
        dto.setMonthlySalary(contract.getMonthlySalary());
        dto.setSigningBonus(contract.getSigningBonus());
        dto.setPerformanceBonus(contract.getPerformanceBonus());
        dto.setStartDate(contract.getStartDate());
        dto.setEndDate(contract.getEndDate());
        dto.setStatus(contract.getStatus());
        dto.setTerminationClause(contract.getTerminationClause());
        dto.setTerminationFee(contract.getTerminationFee());
        return dto;
    }
}
