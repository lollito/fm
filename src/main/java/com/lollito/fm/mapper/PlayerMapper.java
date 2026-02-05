package com.lollito.fm.mapper;

import com.lollito.fm.model.Player;
import com.lollito.fm.dto.PlayerDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayerMapper {
    PlayerDTO toDto(Player player);
    Player toEntity(PlayerDTO dto);
}
