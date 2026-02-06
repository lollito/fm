package com.lollito.fm.mapper;

import com.lollito.fm.model.News;
import com.lollito.fm.model.dto.NewsDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NewsMapper {
    NewsDTO toDto(News news);
    News toEntity(NewsDTO dto);
}
