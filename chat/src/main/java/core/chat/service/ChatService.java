package core.chat.service;

import core.chat.controller.response.ChatRoomResponse;
import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import core.chat.repository.ChatRepository;
import core.chat.service.dto.ChatRoomDto;
import java.util.List;
import java.util.Optional;
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
        Optional<ChatRoomDto> optionalChatRoomDto = chatRepository.findChatRoomByRoomId(roomId);
        return optionalChatRoomDto.map(
                chatRoomDto -> chatRoomDto.getUserId().equals(userId)
        ).orElse(true);
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

    @Transactional
    public void deleteRoom(Long roomId) {
        chatRepository.deleteChatHistoryByRoomId(roomId);
        chatRepository.deleteChatRoomById(roomId);
    }
}
