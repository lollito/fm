package com.lollito.fm.mapper;

import org.mapstruct.Mapper;

import com.lollito.fm.model.Module;
import com.lollito.fm.model.dto.ModuleDTO;

@Mapper(componentModel = "spring")
public interface ModuleMapper {
    ModuleDTO toDto(Module module);
    Module toEntity(ModuleDTO dto);
}
