package core.chat.service;

import core.chat.controller.response.ChatRoomResponse;
import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import core.chat.repository.ChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    @Transactional
    public void saveChatHistory(ChatHistory userChat, ChatHistory llmChat){
        chatRepository.insertChatHistoryWithoutSelect(userChat);
        chatRepository.insertChatHistoryWithoutSelect(llmChat);
    }

    @Transactional
    public void saveChatHistoryAndChatRoom(ChatRoom chatRoom, ChatHistory userChat, ChatHistory llmChat){
        chatRepository.insertChatRoomWithoutSelect(chatRoom);
        chatRepository.insertChatHistoryWithoutSelect(userChat);
        chatRepository.insertChatHistoryWithoutSelect(llmChat);
    }

    @Transactional(readOnly = true)
    public boolean checkIsValidRoomId(String userId, Long roomId) {
        return chatRepository.existsByUserIdAndRoomId(userId, roomId);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRoomsLatest(String userId, int limit) {
        return chatRepository.findAllByUserIdLatest(userId, limit).stream()
                .map(ChatRoomResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRoomsAfter(String userId, Long lastRoomId, int limit) {
        return chatRepository.findAllByUserIdAfterRoomId(userId, lastRoomId, limit).stream()
                .map(ChatRoomResponse::of)
                .toList();
    }
}
