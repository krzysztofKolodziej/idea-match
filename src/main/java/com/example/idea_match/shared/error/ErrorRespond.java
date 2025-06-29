package com.example.idea_match.shared.error;

import org.springframework.http.HttpStatusCode;

public record ErrorRespond(HttpStatusCode status, String message) {
}
