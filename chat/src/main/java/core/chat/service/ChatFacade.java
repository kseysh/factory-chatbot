package core.chat.service;

import core.chat.controller.request.ChatRequest;
import core.chat.controller.request.CreateChatRoomRequest;
import core.chat.controller.response.ChatResponse;
import core.chat.controller.response.CreateChatRoomResponse;
import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import core.common.snowflake.Snowflake;
import core.mcpclient.service.LLMCreatedRoom;
import core.mcpclient.service.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatFacade {

    private final LLMService llmService;
    private final ChatService chatService;

    public ChatResponse chat(String userId, ChatRequest chatRequest) {
        Long roomId = chatRequest.getRoomId();
        String question = chatRequest.getQuestion();
        if (!chatService.checkIsValidRoomId(userId, roomId)) {
            throw new IllegalArgumentException();
        }

        String answer = llmService.chat(roomId, question);

        ChatHistory userChat = ChatHistory.createUserChatHistory(roomId, question);
        ChatHistory llmChat = ChatHistory.createSystemChatHistory(roomId,answer);
        chatService.saveChatHistory(userChat, llmChat);

        return ChatResponse.of(llmChat);
    }

    public CreateChatRoomResponse createChatRoom(String userId, CreateChatRoomRequest request) {
        Long roomId = Snowflake.getInstance().nextId();
        String question = request.getQuestion();

        LLMCreatedRoom llmCreatedRoom = llmService.startNewChat(roomId, question);

        ChatRoom chatRoom = ChatRoom.createChatRoom(userId, llmCreatedRoom.getRoomName());
        ChatHistory userChat = ChatHistory.createUserChatHistory(roomId, question);
        ChatHistory llmChat = ChatHistory.createSystemChatHistory(roomId, llmCreatedRoom.getAnswer());
        chatService.saveChatHistoryAndChatRoom(chatRoom, userChat, llmChat);

        return CreateChatRoomResponse.of(chatRoom.getName(), userChat);
    }
}
