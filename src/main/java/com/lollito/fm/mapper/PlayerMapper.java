package com.lollito.fm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lollito.fm.dto.PlayerDTO;
import com.lollito.fm.model.Player;

@Mapper(componentModel = "spring")
public interface PlayerMapper {
    @Mapping(target = "clubId", expression = "java(player.getTeam() != null ? player.getTeam().getOwnerClubId() : null)")
    @Mapping(target = "price", expression = "java(player.getMarketValue())")
    PlayerDTO toDto(Player player);
    Player toEntity(PlayerDTO dto);
}
