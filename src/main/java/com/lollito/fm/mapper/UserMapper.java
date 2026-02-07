package com.lollito.fm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lollito.fm.model.Role;
import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.RoleDTO;
import com.lollito.fm.model.dto.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "country.name", target = "country")
    @Mapping(source = "club.id", target = "clubId")
    @Mapping(source = "server.id", target = "serverId")
    UserDTO toDto(User user);

    @Mapping(target = "country", ignore = true)
    @Mapping(source = "country", target = "countryString")
    User toEntity(UserDTO dto);

    default RoleDTO toRoleDto(Role role) {
        if (role == null) {
            return null;
        }
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }

    default Role toRole(RoleDTO roleDTO) {
        if (roleDTO == null) {
            return null;
        }
        Role role = new Role();
        role.setId(roleDTO.getId());
        role.setName(roleDTO.getName());
        return role;
    }
}
