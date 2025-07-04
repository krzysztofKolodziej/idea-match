package com.example.idea_match.user.service;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "USER") // Default role set to USER
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "profilePictureUrl", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "tokenExpirationTime", ignore = true)
    @Mapping(target = "enabled", constant = "false")
    User dtoToEntity(AddUserCommand addUserCommand);
}
