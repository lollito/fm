package com.lollito.fm.mapper;

import org.mapstruct.Mapper;

import com.lollito.fm.model.MedicalCenter;
import com.lollito.fm.model.TrainingFacility;
import com.lollito.fm.model.YouthAcademy;
import com.lollito.fm.model.dto.MedicalCenterDTO;
import com.lollito.fm.model.dto.TrainingFacilityDTO;
import com.lollito.fm.model.dto.YouthAcademyDTO;

@Mapper(componentModel = "spring")
public interface FacilityMapper {
    TrainingFacilityDTO toDto(TrainingFacility facility);
    MedicalCenterDTO toDto(MedicalCenter facility);
    YouthAcademyDTO toDto(YouthAcademy facility);
}
