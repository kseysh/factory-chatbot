package core.chat.controller;

import core.chat.controller.request.ChatHistoryRequest;
import core.chat.controller.request.ChatRoomListRequest;
import core.chat.controller.response.ChatHistoriesResponse;
import core.chat.controller.response.ChatResponseDeprecated;
import core.chat.controller.response.ChatRoomListResponse;
import core.chat.controller.response.CreateChatRoomResponseDeprecated;
import core.global.auth.UserId;
import core.chat.controller.request.ChatRequest;
import core.chat.controller.request.CreateChatRoomRequest;
import core.chat.controller.response.ChatResponse;
import core.chat.controller.response.CreateChatRoomResponse;
import core.chat.service.ChatFacade;
import core.mcpclient.service.LLMHealthCheckService;
import core.mcpclient.service.McpToolService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final LLMHealthCheckService llmHealthCheckService;
    private final McpToolService mcpToolService;
    private final ChatFacade chatFacade;

    @PostMapping("/v2/chat")
    public ResponseEntity<ChatResponse> chat(
            @UserId String userId,
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatFacade.chat(userId, request));
    }

    @PostMapping("/v2/chat/room/create")
    public ResponseEntity<CreateChatRoomResponse> createChatRoom(
            @UserId String userId,
            @Valid @RequestBody CreateChatRoomRequest request
    ) {
        return ResponseEntity.ok(chatFacade.startNewChat(userId, request));
    }

    @PostMapping("/v1/chat/stream")
    public SseEmitter chatStream(
            @UserId String userId,
            @Valid @RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(60 * 1000L);

        Disposable disposable = chatFacade.chatStream(userId, request).subscribe(
                data -> {
                    try {
                        emitter.send(data);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },// onNext: 데이터 전송
                emitter::completeWithError, // onError: 에러 전파
                emitter::complete // onComplete: 종료
        );

        emitter.onCompletion(disposable::dispose);
        emitter.onTimeout(() -> {
            emitter.complete();
            disposable.dispose();
        });

        return emitter;
    }

    @PostMapping("/v1/chat/room/create/stream")
    public SseEmitter createChatRoomStream(
            @UserId String userId,
            @Valid @RequestBody CreateChatRoomRequest request
    ) {
        SseEmitter emitter = new SseEmitter(60 * 1000L);

        Disposable disposable = chatFacade.startNewChatStream(userId, request).subscribe(
                data -> {
                    try {
                        emitter.send(data);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },// onNext: 데이터 전송
                emitter::completeWithError, // onError: 에러 전파
                emitter::complete // onComplete: 종료
        );

        emitter.onCompletion(disposable::dispose);
        emitter.onTimeout(() -> {
            emitter.complete();
            disposable.dispose();
        });

        return emitter;
    }

    @GetMapping("/v1/chat")
    public ResponseEntity<ChatHistoriesResponse> getChatHistory(
            @UserId String userId,
            @Valid @ModelAttribute ChatHistoryRequest request) {
        return ResponseEntity.ok(chatFacade.getChatHistories(userId, request));
    }

    @GetMapping("/v1/chat/room/list")
    public ResponseEntity<ChatRoomListResponse> getChatRooms(
            @UserId String userId,
            @Valid @ModelAttribute ChatRoomListRequest request) {

        return ResponseEntity.ok(chatFacade.getChatRooms(userId, request));
    }

    @DeleteMapping("/v1/chat/room")
    public ResponseEntity<Void> deleteChatRoom(
            @UserId String userId,
            @NotNull @RequestParam Long roomId
    ) {
        chatFacade.deleteRoom(userId, roomId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/v1/mcp/tools/refresh")
    public ResponseEntity<Void> refreshTools() {
        mcpToolService.refreshTools();
        return ResponseEntity.noContent().build();
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

    @PostMapping("/v1/chat")
    public ResponseEntity<ChatResponseDeprecated> chatDeprecated(
            @UserId String userId,
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatFacade.chatDeprecated(userId, request));
    }

    @PostMapping("/v1/chat/room/create")
    public ResponseEntity<CreateChatRoomResponseDeprecated> createChatRoomDeprecated(
            @UserId String userId,
            @Valid @RequestBody CreateChatRoomRequest request
    ) {
        return ResponseEntity.ok(chatFacade.startNewChatDeprecated(userId, request));
    }
}
