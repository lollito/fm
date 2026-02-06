package com.lollito.fm.mapper;

import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.dto.StadiumDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StadiumMapper {
    StadiumDTO toDto(Stadium stadium);
    Stadium toEntity(StadiumDTO dto);
}
