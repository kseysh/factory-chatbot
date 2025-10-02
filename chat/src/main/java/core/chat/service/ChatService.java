package core.chat.service;

import core.chat.controller.request.ChatRequest;
import core.chat.controller.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    public ChatResponse chat(ChatRequest chatRequest) {
        return ChatResponse.of("your request is "+ chatRequest.getMessage());
    }
}
