package com.lollito.fm.mapper;

import com.lollito.fm.model.Staff;
import com.lollito.fm.model.StaffContract;
import com.lollito.fm.dto.StaffDTO;
import com.lollito.fm.dto.StaffContractDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StaffMapper {
    @Mapping(source = "club.id", target = "clubId")
    @Mapping(source = "nationality.name", target = "nationalityName")
    StaffDTO toDto(Staff staff);

    @Mapping(source = "staff.id", target = "staffId")
    @Mapping(source = "club.id", target = "clubId")
    StaffContractDTO toDto(StaffContract contract);
}
