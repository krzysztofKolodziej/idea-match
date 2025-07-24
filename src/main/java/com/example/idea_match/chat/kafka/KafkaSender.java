package com.example.idea_match.chat.kafka;

import com.example.idea_match.chat.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class KafkaSender {

    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;

    public void send(String topic, ChatMessage message) {
        kafkaTemplate.send(topic, message);
    }

}