package com.lollito.fm.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.lollito.fm.model.MaintenanceRecord;
import com.lollito.fm.model.dto.MaintenanceRecordDTO;

@Mapper(componentModel = "spring")
public interface MaintenanceMapper {
    MaintenanceRecordDTO toDto(MaintenanceRecord record);
    List<MaintenanceRecordDTO> toDtoList(List<MaintenanceRecord> records);
}
