package com.ilu.system.chatbot.controller;

import com.ilu.system.chatbot.dto.ChatRequestDto;
import com.ilu.system.chatbot.dto.ChatResponseDto;
import com.ilu.system.chatbot.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> chat(@RequestBody ChatRequestDto request) {
        Map<String, Object> result = chatbotService.chat(request.getMessage(), request.getSessionId());
        String response = (String) result.get("response");
        String sessionId = (String) result.get("sessionId");
        return ResponseEntity.ok(new ChatResponseDto(response, sessionId));
    }
}
