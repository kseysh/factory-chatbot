package core.chat.service;

import core.chat.controller.request.*;
import core.chat.controller.response.*;
import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import core.chat.service.dto.ChatRoomDto;
import core.common.snowflake.Snowflake;
import core.mcpclient.service.dto.NewChatRoomInfo;
import core.mcpclient.service.LLMService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return ChatResponse.of(llmChat);
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

        return CreateChatRoomResponse.of(chatRoom.getName(), llmChat);
    }

    @Transactional(readOnly = true)
    public ChatHistoriesResponse getChatHistories(String userId, ChatHistoryRequest request) {
        if (!chatRoomService.canUserAccessRoom(request.getRoomId(), userId)) {
            throw new IllegalArgumentException("Invalid room ID: " + request.getRoomId() + " for user: " + userId);
        }

        if(request.getLastChatId() == null){
            return ChatHistoriesResponse.of(
                    request.getRoomId(),
                    chatHistoryService.getChatHistoriesLatest(request.getRoomId(), request.getSize())
            );
        }
        return ChatHistoriesResponse.of(
                request.getRoomId(),
                chatHistoryService.getChatHistoriesAfter(request.getRoomId(), request.getLastChatId(), request.getSize())
        );
    }

    @Transactional(readOnly = true)
    public ChatRoomListResponse getChatRooms(String userId, ChatRoomListRequest request) {
        if(request.getLastRoomId() == null){
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
        if(checkUserCanDeleteRoom(userId, roomId)){
            chatRoomService.deleteRoom(roomId);
            chatHistoryService.deleteChatHistoryByRoomId(roomId);
        }else{
            throw new IllegalArgumentException("Room Id(" + roomId + ")를 삭제할 권한이 " + userId + "에게 존재하지 않습니다.");
        }
    }

    private boolean checkUserCanDeleteRoom(String userId, Long roomId) {
        return chatRoomService.findChatRoomByRoomId(roomId).map(
                chatRoomDto -> chatRoomDto.getUserId().equals(userId)
        ).orElse(true);
    }
}
