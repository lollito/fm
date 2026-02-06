package com.lollito.fm.mapper;

import com.lollito.fm.model.FacilityType;
import com.lollito.fm.model.MaintenanceRecord;
import com.lollito.fm.model.MaintenanceStatus;
import com.lollito.fm.model.MaintenanceType;
import com.lollito.fm.model.dto.MaintenanceRecordDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MaintenanceMapperTest {

    @Autowired
    private MaintenanceMapper maintenanceMapper;

    @Test
    public void testToDto() {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setId(1L);
        record.setFacilityType(FacilityType.STADIUM);
        record.setFacilityId(10L);
        record.setMaintenanceType(MaintenanceType.CORRECTIVE);
        record.setDescription("Fix seats");
        record.setCost(BigDecimal.valueOf(1000));
        record.setStatus(MaintenanceStatus.SCHEDULED);
        record.setScheduledDate(LocalDate.now());
        record.setCompletedDate(LocalDate.now().plusDays(1));
        record.setQualityRestored(5);
        record.setIssuesFound("Broken seats");
        record.setWorkPerformed("Replaced seats");
        record.setContractorName("BuildCorp");
        record.setIsEmergencyMaintenance(true);

        MaintenanceRecordDTO dto = maintenanceMapper.toDto(record);

        assertNotNull(dto);
        assertEquals(record.getId(), dto.getId());
        assertEquals(record.getFacilityType(), dto.getFacilityType());
        assertEquals(record.getFacilityId(), dto.getFacilityId());
        assertEquals(record.getMaintenanceType(), dto.getMaintenanceType());
        assertEquals(record.getDescription(), dto.getDescription());
        assertEquals(record.getCost(), dto.getCost());
        assertEquals(record.getStatus(), dto.getStatus());
        assertEquals(record.getScheduledDate(), dto.getScheduledDate());
        assertEquals(record.getCompletedDate(), dto.getCompletedDate());
        assertEquals(record.getQualityRestored(), dto.getQualityRestored());
        assertEquals(record.getIssuesFound(), dto.getIssuesFound());
        assertEquals(record.getWorkPerformed(), dto.getWorkPerformed());
        assertEquals(record.getContractorName(), dto.getContractorName());
        assertEquals(record.getIsEmergencyMaintenance(), dto.getIsEmergencyMaintenance());
    }

    @Test
    public void testToDtoList() {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setId(1L);

        List<MaintenanceRecordDTO> dtos = maintenanceMapper.toDtoList(Collections.singletonList(record));

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(record.getId(), dtos.get(0).getId());
    }
}
