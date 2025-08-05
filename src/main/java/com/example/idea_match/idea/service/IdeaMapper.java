package com.example.idea_match.idea.service;


import com.example.idea_match.idea.dto.IdeaDto;
import com.example.idea_match.idea.model.Idea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IdeaMapper {

    @Mapping(source = "owner.username", target = "username")
    IdeaDto toDto(Idea idea);
}
