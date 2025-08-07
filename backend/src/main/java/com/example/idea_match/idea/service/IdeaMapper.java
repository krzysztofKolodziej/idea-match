package com.example.idea_match.idea.service;


import com.example.idea_match.idea.command.AddIdeaCommand;
import com.example.idea_match.idea.command.UpdateIdeaCommand;
import com.example.idea_match.idea.dto.IdeaDetailsDto;
import com.example.idea_match.idea.dto.IdeaDto;
import com.example.idea_match.idea.model.Idea;
import com.example.idea_match.user.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IdeaMapper {

    @Mapping(source = "owner.username", target = "username")
    IdeaDto toDto(Idea idea);

    @Mapping(source = "owner.username", target = "username")
    IdeaDetailsDto toDtoWithDetails(Idea idea);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", source = "command.location")
    @Mapping(target = "collaborators", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(source = "owner", target = "owner")
    Idea toEntity(AddIdeaCommand command, User owner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "collaborators", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateIdeaCommand command, @MappingTarget Idea idea);

}
