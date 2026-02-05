package com.lollito.fm.mapper;

import com.lollito.fm.model.Game;
import com.lollito.fm.model.dto.GameDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GameMapper {
    GameDTO toDto(Game game);
    Game toEntity(GameDTO dto);
}
