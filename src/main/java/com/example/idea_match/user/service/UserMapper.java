package com.example.idea_match.user.service;

import com.example.idea_match.user.command.RegisterUserCommand;
import com.example.idea_match.user.command.UpdateUserProfileCommand;
import com.example.idea_match.user.dto.UserResponse;
import com.example.idea_match.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "USER") // Default role set to USER
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "profilePictureUrl", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "tokenExpirationTime", ignore = true)
    @Mapping(target = "passwordResetToken", ignore = true)
    @Mapping(target = "passwordResetTokenExpiry", ignore = true)
    @Mapping(target = "enabled", constant = "false")
    User commandToEntity(RegisterUserCommand registerUserCommand);

    @Mapping(target = "userId", source = "id")
    UserResponse entityToDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "tokenExpirationTime", ignore = true)
    @Mapping(target = "passwordResetToken", ignore = true)
    @Mapping(target = "passwordResetTokenExpiry", ignore = true)
    void updateUserFromCommand(UpdateUserProfileCommand command, @MappingTarget User user);
}
