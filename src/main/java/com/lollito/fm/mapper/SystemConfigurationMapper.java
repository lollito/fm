package com.lollito.fm.mapper;

import org.mapstruct.Mapper;

import com.lollito.fm.model.SystemConfiguration;
import com.lollito.fm.model.dto.SystemConfigurationDTO;

@Mapper(componentModel = "spring")
public interface SystemConfigurationMapper {
    SystemConfigurationDTO toDto(SystemConfiguration config);
    SystemConfiguration toEntity(SystemConfigurationDTO dto);
}
