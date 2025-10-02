package core.chat.controller;

import core.chat.controller.request.ChatRequest;
import core.chat.controller.response.ChatResponse;
import core.chat.service.ChatService;
import core.mcpclient.service.LLMHealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final LLMHealthCheckService llmHealthCheckService;
    private final ChatService chatService;

    @PostMapping("/v1/chat")
    public ResponseEntity<ChatResponse> read(@RequestBody ChatRequest chatRequest) {
        return ResponseEntity.ok(chatService.chat(chatRequest));
    }

    @GetMapping("/v1/mcp/health")
    public ResponseEntity<String> llmHealthCheck() {
        return ResponseEntity.ok(llmHealthCheckService.checkLLMConnection());
    }

    @GetMapping("/health")
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }
}
