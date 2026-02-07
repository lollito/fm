package com.lollito.fm.mapper;

import org.mapstruct.Mapper;

import com.lollito.fm.model.News;
import com.lollito.fm.model.dto.NewsDTO;

@Mapper(componentModel = "spring")
public interface NewsMapper {
    NewsDTO toDto(News news);
    News toEntity(NewsDTO dto);
}
