package core.chat.service;

import core.chat.controller.request.*;
import core.chat.controller.response.*;
import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import core.chat.entity.MessageType;
import core.chat.service.dto.ChatAnswerStreamResponse;
import core.chat.service.dto.ChatMetaDataStreamResponse;
import core.chat.service.dto.ChatRoomNameStreamResponse;
import core.common.snowflake.Snowflake;
import core.mcpclient.service.dto.NewChatRoomInfo;
import core.mcpclient.service.LLMService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ChatFacade {

    private final LLMService llmService;
    private final ChatRoomService chatRoomService;
    private final ChatHistoryService chatHistoryService;

    @Transactional
    public ChatResponse chat(String userId, ChatRequest chatRequest) {
        Long roomId = chatRequest.getRoomId();
        if (!chatRoomService.canUserAccessRoom(roomId, userId)) {
            throw new IllegalArgumentException("Invalid room ID: " + roomId + " for user: " + userId);
        }
        String question = chatRequest.getQuestion();
        String answer = llmService.chat(roomId, question);

        ChatHistory userChat = ChatHistory.createUserChatHistory(roomId, question);
        ChatHistory llmChat = ChatHistory.createLLMChatHistory(roomId, answer);
        chatHistoryService.saveChatHistory(userChat, llmChat);

        return ChatResponse.builder()
                .roomId(roomId)
                .answer(answer)
                .llmChatId(llmChat.getId())
                .userChatId(userChat.getId())
                .build();
    }

    public Flux<Object> chatStream(String userId, ChatRequest chatRequest) {
        Long roomId = chatRequest.getRoomId();
        String question = chatRequest.getQuestion();

        return Mono.fromCallable(() -> chatRoomService.canUserAccessRoom(roomId, userId))
                .subscribeOn(Schedulers.boundedElastic()) // 알바생 스레드에게 위임
                .flatMapMany(isAllowed -> {
                    if (Boolean.FALSE.equals(isAllowed)) {
                        return Flux.error(new IllegalArgumentException(
                                "Invalid room ID: " + roomId + " for user: " + userId));
                    }

                    Long userChatId = Snowflake.getInstance().nextId();
                    Long llmChatId = Snowflake.getInstance().nextId();
                    StringBuilder answerBuilder = new StringBuilder();

                    Mono<Object> metaPacket = Mono.just(
                            new ChatMetaDataStreamResponse(roomId, userChatId, llmChatId)
                    );

                    Flux<Object> contentStream = llmService.chatStream(roomId, question)
                            .doOnNext(answerBuilder::append)
                            .map(ChatAnswerStreamResponse::new);
                    return Flux.concat(metaPacket, contentStream)
                            .doOnComplete(() -> {
                                ChatHistory userChat = ChatHistory.builder()
                                        .id(userChatId)
                                        .roomId(roomId)
                                        .content(question)
                                        .type(MessageType.USER)
                                        .build();
                                ChatHistory llmChat = ChatHistory.builder()
                                        .id(llmChatId)
                                        .roomId(roomId)
                                        .content(answerBuilder.toString())
                                        .type(MessageType.ASSISTANT)
                                        .build();
                                Mono.fromRunnable(() -> this.chatHistoryService.saveChatHistory(userChat, llmChat))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .subscribe();
                            });
                });
    }

    @Transactional
    public CreateChatRoomResponse startNewChat(String userId, CreateChatRoomRequest request) {
        Long roomId = Snowflake.getInstance().nextId();
        String question = request.getQuestion();

        NewChatRoomInfo newChatRoomInfo = llmService.startNewChat(roomId, question);

        ChatRoom chatRoom = ChatRoom.createChatRoom(roomId, userId, newChatRoomInfo.roomName());
        ChatHistory userChat = ChatHistory.createUserChatHistory(roomId, question);
        ChatHistory llmChat = ChatHistory.createLLMChatHistory(roomId, newChatRoomInfo.answer());
        chatRoomService.saveChatRoom(chatRoom);
        chatHistoryService.saveChatHistory(userChat, llmChat);

        return CreateChatRoomResponse.builder()
                .roomId(roomId)
                .roomName(chatRoom.getName())
                .answer(newChatRoomInfo.answer())
                .userChatId(userChat.getId())
                .llmChatId(llmChat.getId())
                .build();
    }

    public Flux<Object> startNewChatStream(String userId, CreateChatRoomRequest request) {
        Long roomId = Snowflake.getInstance().nextId();
        Long userChatId = Snowflake.getInstance().nextId();
        Long llmChatId = Snowflake.getInstance().nextId();

        StringBuilder titleBuilder = new StringBuilder();
        StringBuilder answerBuilder = new StringBuilder();

        Mono<Object> metaPacket = Mono.just(
                new ChatMetaDataStreamResponse(roomId, userChatId, llmChatId)
        );

        Flux<Object> contentStream = llmService.startNewChatStream(roomId, request.getQuestion())
                .doOnNext(chunk -> {
                    if (chunk.roomName() != null) {
                        titleBuilder.append(chunk.roomName());
                    }
                    if (chunk.answer() != null) {
                        answerBuilder.append(chunk.answer());
                    }
                })
                .flatMap(chunk -> {
                    List<Object> packets = new ArrayList<>();

                    if (chunk.roomName() != null && !chunk.roomName().isEmpty()) {
                        packets.add(new ChatRoomNameStreamResponse(chunk.roomName()));
                    }
                    if (chunk.answer() != null && !chunk.answer().isEmpty()) {
                        packets.add(new ChatAnswerStreamResponse(chunk.answer()));
                    }

                    return Flux.fromIterable(packets);
                });

        return Flux.concat(metaPacket, contentStream)
                .doOnComplete(() -> {
                    ChatRoom chatRoom = ChatRoom.createChatRoom(roomId, userId, titleBuilder.toString());
                    ChatHistory userChat = ChatHistory.builder()
                            .id(userChatId)
                            .roomId(roomId)
                            .content(request.getQuestion())
                            .type(MessageType.USER)
                            .build();
                    ChatHistory llmChat = ChatHistory.builder()
                            .id(llmChatId)
                            .roomId(roomId)
                            .content(answerBuilder.toString())
                            .type(MessageType.ASSISTANT)
                            .build();
                    Mono.fromRunnable(() -> {
                                chatRoomService.saveChatRoom(chatRoom);
                                chatHistoryService.saveChatHistory(userChat, llmChat);
                            }).subscribeOn(Schedulers.boundedElastic())
                            .subscribe();
                });
    }

    @Transactional(readOnly = true)
    public ChatHistoriesResponse getChatHistories(String userId, ChatHistoryRequest request) {
        if (!chatRoomService.canUserAccessRoom(request.getRoomId(), userId)) {
            throw new IllegalArgumentException("Invalid room ID: " + request.getRoomId() + " for user: " + userId);
        }

        if (request.getLastChatId() == null) {
            return ChatHistoriesResponse.of(
                    request.getRoomId(),
                    chatHistoryService.getChatHistoriesLatest(request.getRoomId(), request.getSize())
            );
        }
        return ChatHistoriesResponse.of(
                request.getRoomId(),
                chatHistoryService.getChatHistoriesAfter(request.getRoomId(), request.getLastChatId(),
                        request.getSize())
        );
    }

    @Transactional(readOnly = true)
    public ChatRoomListResponse getChatRooms(String userId, ChatRoomListRequest request) {
        if (request.getLastRoomId() == null) {
            return ChatRoomListResponse.of(
                    chatRoomService.findChatRoomsLatest(userId, request.getSize())
            );
        }
        return ChatRoomListResponse.of(
                chatRoomService.findChatRoomsAfter(userId, request.getLastRoomId(), request.getSize())
        );
    }

    @Transactional
    public void deleteRoom(String userId, Long roomId) {
        if (checkUserCanDeleteRoom(userId, roomId)) {
            chatRoomService.deleteRoom(roomId);
            chatHistoryService.deleteChatHistoryByRoomId(roomId);
        } else {
            throw new IllegalArgumentException("Room Id(" + roomId + ")를 삭제할 권한이 " + userId + "에게 존재하지 않습니다.");
        }
    }

    private boolean checkUserCanDeleteRoom(String userId, Long roomId) {
        return chatRoomService.findChatRoomByRoomId(roomId).map(
                chatRoomDto -> chatRoomDto.getUserId().equals(userId)
        ).orElse(true);
    }

    @Transactional
    public ChatResponseDeprecated chatDeprecated(String userId, ChatRequest chatRequest) {
        Long roomId = chatRequest.getRoomId();
        if (!chatRoomService.canUserAccessRoom(roomId, userId)) {
            throw new IllegalArgumentException("Invalid room ID: " + roomId + " for user: " + userId);
        }
        String question = chatRequest.getQuestion();
        String answer = llmService.chat(roomId, question);

        ChatHistory userChat = ChatHistory.createUserChatHistory(roomId, question);
        ChatHistory llmChat = ChatHistory.createLLMChatHistory(roomId, answer);
        chatHistoryService.saveChatHistory(userChat, llmChat);

        return ChatResponseDeprecated.of(llmChat);
    }

    @Transactional
    public CreateChatRoomResponseDeprecated startNewChatDeprecated(String userId, CreateChatRoomRequest request) {
        Long roomId = Snowflake.getInstance().nextId();
        String question = request.getQuestion();

        NewChatRoomInfo newChatRoomInfo = llmService.startNewChat(roomId, question);

        ChatRoom chatRoom = ChatRoom.createChatRoom(roomId, userId, newChatRoomInfo.roomName());
        ChatHistory userChat = ChatHistory.createUserChatHistory(roomId, question);
        ChatHistory llmChat = ChatHistory.createLLMChatHistory(roomId, newChatRoomInfo.answer());
        chatRoomService.saveChatRoom(chatRoom);
        chatHistoryService.saveChatHistory(userChat, llmChat);

        return CreateChatRoomResponseDeprecated.of(chatRoom.getName(), llmChat);
    }
}
