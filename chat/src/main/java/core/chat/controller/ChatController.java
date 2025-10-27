package core.chat.controller;

import core.global.auth.UserId;
import core.chat.controller.request.ChatRequest;
import core.chat.controller.request.CreateChatRoomRequest;
import core.chat.controller.response.ChatResponse;
import core.chat.controller.response.CreateChatRoomResponse;
import core.chat.service.ChatFacade;
import core.mcpclient.service.LLMHealthCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final LLMHealthCheckService llmHealthCheckService;
    private final ChatFacade chatFacade;

    @PostMapping("/v1/chat")
    public ResponseEntity<ChatResponse> chat(
            @UserId String userId,
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatFacade.chat(userId, request));
    }

    @PostMapping("/v1/chat/room/create")
    public ResponseEntity<CreateChatRoomResponse> createChatRoom(
            @UserId String userId,
            @Valid @RequestBody CreateChatRoomRequest request) {
        return ResponseEntity.ok(chatFacade.startNewChat(userId, request));
    }

    @GetMapping("/v1/mcp/health")
    public ResponseEntity<String> llmHealthCheck() {
        if(llmHealthCheckService.isLLMConnected()) return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @GetMapping("/health")
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }
}
