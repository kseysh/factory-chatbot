package core.chat.controller;


import core.chat.controller.request.ChatRequest;
import core.chat.controller.response.ChatResponse;
import core.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/v1/chat")
    public ResponseEntity<ChatResponse> read(@RequestBody ChatRequest chatRequest) {
        return ResponseEntity.ok(chatService.chat(chatRequest));
    }
}
