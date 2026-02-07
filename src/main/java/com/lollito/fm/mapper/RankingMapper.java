package com.lollito.fm.mapper;

import org.mapstruct.Mapper;

import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.dto.RankingDTO;

@Mapper(componentModel = "spring", uses = {ClubMapper.class})
public interface RankingMapper {
    RankingDTO toDto(Ranking ranking);
    Ranking toEntity(RankingDTO dto);
}
