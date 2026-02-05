package com.lollito.fm.mapper;

import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "country.name", target = "country")
    UserDTO toDto(User user);

    @Mapping(target = "country", ignore = true)
    @Mapping(source = "country", target = "countryString")
    User toEntity(UserDTO dto);
}
