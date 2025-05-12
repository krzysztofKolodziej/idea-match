package com.example.idea_match.chat.controller;

import com.example.idea_match.chat.model.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

// controller only needed to test by postman

@RestController
@AllArgsConstructor
@Slf4j
public class CommandController {

    private final KafkaTemplate<String, Message> kafkaTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    @RequestMapping(method = RequestMethod.POST, path = "/send")
    public void send(@RequestBody Message message) {
        kafkaTemplate.send("messaging", message);
        log.info(message.getContent());
        messagingTemplate.convertAndSend("topic/public", message);
    }

}
