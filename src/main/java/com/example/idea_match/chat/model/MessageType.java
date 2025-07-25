package com.example.idea_match.chat.model;

public enum MessageType {
    // WebSocket message types
    JOIN,
    CHAT, 
    LEAVE,
    CONNECT,
    DISCONNECT,
    PRIVATE_MESSAGE,
    // Persistent message types
    TEXT,
    SYSTEM
}