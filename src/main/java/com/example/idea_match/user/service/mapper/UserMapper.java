package com.example.idea_match.user.service.mapper;

import com.example.idea_match.user.command.AddUserCommand;
import com.example.idea_match.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User dtoToEntity(AddUserCommand addUserCommand);
}
