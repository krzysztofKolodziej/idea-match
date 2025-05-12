package com.example.idea_match.chat.broker;

import com.example.idea_match.chat.model.Message;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class Sender {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    public void send(String topic, Message message) {
        kafkaTemplate.send(topic, message);
    }
}