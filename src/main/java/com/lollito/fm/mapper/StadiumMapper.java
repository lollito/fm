package com.lollito.fm.mapper;

import org.mapstruct.Mapper;

import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.dto.StadiumDTO;

@Mapper(componentModel = "spring")
public interface StadiumMapper {
    StadiumDTO toDto(Stadium stadium);
    Stadium toEntity(StadiumDTO dto);
}
