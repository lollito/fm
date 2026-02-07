package com.lollito.fm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.dto.ClubDTO;

@Mapper(componentModel = "spring")
public interface ClubMapper {
    @Mapping(source = "league.id", target = "leagueId")
    @Mapping(source = "team.id", target = "teamId")
    @Mapping(target = "isHuman", expression = "java(club.getUser() != null)")
    @Mapping(source = "stadium.id", target = "stadiumId")
    @Mapping(source = "trainingFacility.id", target = "trainingFacilityId")
    @Mapping(source = "medicalCenter.id", target = "medicalCenterId")
    @Mapping(source = "youthAcademy.id", target = "youthAcademyId")
    @Mapping(source = "finance.id", target = "financeId")
    ClubDTO toDto(Club club);
    Club toEntity(ClubDTO dto);
}
