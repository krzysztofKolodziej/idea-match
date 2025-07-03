package com.example.idea_match.user.service.mapper;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", constant = "USER") // Default role set to USER
    @Mapping(target = "lastName", source = "lastName")
    User dtoToEntity(AddUserCommand addUserCommand);
}
