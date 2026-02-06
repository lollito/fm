package com.lollito.fm.mapper;

import com.lollito.fm.model.Player;
import com.lollito.fm.dto.PlayerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerMapper {
    @Mapping(target = "clubId", expression = "java(player.getTeam() != null ? player.getTeam().getOwnerClubId() : null)")
    PlayerDTO toDto(Player player);
    Player toEntity(PlayerDTO dto);
}
