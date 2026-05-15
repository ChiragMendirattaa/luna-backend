package com.lunacycle.controller;

import com.lunacycle.dto.ChatDto;
import com.lunacycle.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<ChatDto.ChatResponse> chat(
            Authentication auth,
            @RequestBody ChatDto.ChatRequest request) {
        UUID userId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(chatService.chat(userId, request));
    }
}