package com.lollito.fm.mapper;

import com.lollito.fm.model.Module;
import com.lollito.fm.model.dto.ModuleDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ModuleMapper {
    ModuleDTO toDto(Module module);
    Module toEntity(ModuleDTO dto);
}
