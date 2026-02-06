package com.lollito.fm.mapper;

import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.dto.RankingDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ClubMapper.class})
public interface RankingMapper {
    RankingDTO toDto(Ranking ranking);
    Ranking toEntity(RankingDTO dto);
}
