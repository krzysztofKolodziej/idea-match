package com.example.idea_match.chat.repository;

import com.example.idea_match.chat.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    @Query("{ 'recipientId': ?0, 'status': { $ne: 'READ' }, 'deleted': false }")
    List<ChatMessage> findUnreadMessages(String userId);
}