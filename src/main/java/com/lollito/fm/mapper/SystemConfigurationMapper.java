package com.lollito.fm.mapper;

import com.lollito.fm.model.SystemConfiguration;
import com.lollito.fm.model.dto.SystemConfigurationDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemConfigurationMapper {
    SystemConfigurationDTO toDto(SystemConfiguration config);
    SystemConfiguration toEntity(SystemConfigurationDTO dto);
}
