package com.example.idea_match.chat.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private MessageType type;
    private String content;
    private String sender;
    private String sessionId;

}
