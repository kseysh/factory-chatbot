package core.chat.service;

import core.chat.controller.request.ChatRequest;
import core.chat.controller.request.ChatRoomListRequest;
import core.chat.controller.request.CreateChatRoomRequest;
import core.chat.controller.response.ChatResponse;
import core.chat.controller.response.ChatRoomListResponse;
import core.chat.controller.response.CreateChatRoomResponse;
import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import core.common.snowflake.Snowflake;
import core.mcpclient.service.dto.NewChatRoomInfo;
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
        if (!chatService.checkIsValidRoomId(userId, roomId)) {
            throw new IllegalArgumentException("Invalid room ID: " + roomId + " for user: " + userId);
        }
        String question = chatRequest.getQuestion();
        String answer = llmService.chat(roomId, question);

        ChatHistory userChat = ChatHistory.createUserChatHistory(roomId, question);
        ChatHistory llmChat = ChatHistory.createLLMChatHistory(roomId, answer);
        chatService.saveChatHistory(userChat, llmChat);

        return ChatResponse.of(llmChat);
    }

    public CreateChatRoomResponse startNewChat(String userId, CreateChatRoomRequest request) {
        Long roomId = Snowflake.getInstance().nextId();
        String question = request.getQuestion();

        NewChatRoomInfo newChatRoomInfo = llmService.startNewChat(roomId, question);

        ChatRoom chatRoom = ChatRoom.createChatRoom(roomId, userId, newChatRoomInfo.roomName());
        ChatHistory userChat = ChatHistory.createUserChatHistory(roomId, question);
        ChatHistory llmChat = ChatHistory.createLLMChatHistory(roomId, newChatRoomInfo.answer());
        chatService.saveChatHistoryAndChatRoom(chatRoom, userChat, llmChat);

        return CreateChatRoomResponse.of(chatRoom.getName(), llmChat);
    }

    public ChatRoomListResponse getChatRooms(String userId, ChatRoomListRequest request) {
        return request.getLastRoomId().map(
                roomId -> ChatRoomListResponse.of(chatService.getChatRoomsAfter(userId, roomId, request.getSize())))
                .orElseGet(() -> ChatRoomListResponse.of(chatService.getChatRoomsLatest(userId, request.getSize())));
    }
}
