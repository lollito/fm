package com.lollito.fm.mapper;

import com.lollito.fm.model.Server;
import com.lollito.fm.model.dto.ServerDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServerMapper {
    ServerDTO toDto(Server server);
    Server toEntity(ServerDTO dto);
}
