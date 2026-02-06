package com.lollito.fm.mapper;

import com.lollito.fm.model.MaintenanceRecord;
import com.lollito.fm.model.dto.MaintenanceRecordDTO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MaintenanceMapper {
    MaintenanceRecordDTO toDto(MaintenanceRecord record);
    List<MaintenanceRecordDTO> toDtoList(List<MaintenanceRecord> records);
}
