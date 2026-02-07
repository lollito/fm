package com.lollito.fm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lollito.fm.model.YouthCandidate;
import com.lollito.fm.model.dto.YouthCandidateDTO;

@Mapper(componentModel = "spring")
public interface YouthCandidateMapper {
    @Mapping(target = "nationality", expression = "java(entity.getNationality() != null ? entity.getNationality().getName() : null)")
    YouthCandidateDTO toDto(YouthCandidate entity);
}
