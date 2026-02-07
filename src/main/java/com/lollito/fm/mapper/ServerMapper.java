package com.lollito.fm.mapper;

import org.mapstruct.Mapper;

import com.lollito.fm.model.Server;
import com.lollito.fm.model.dto.ServerDTO;

@Mapper(componentModel = "spring")
public interface ServerMapper {
    ServerDTO toDto(Server server);
    Server toEntity(ServerDTO dto);
}
