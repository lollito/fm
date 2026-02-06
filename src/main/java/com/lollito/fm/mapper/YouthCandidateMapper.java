package com.lollito.fm.mapper;

import com.lollito.fm.model.YouthCandidate;
import com.lollito.fm.model.dto.YouthCandidateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface YouthCandidateMapper {
    @Mapping(target = "nationality", expression = "java(entity.getNationality() != null ? entity.getNationality().getName() : null)")
    YouthCandidateDTO toDto(YouthCandidate entity);
}
