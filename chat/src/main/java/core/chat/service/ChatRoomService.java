package core.chat.service;

import core.chat.controller.response.ChatRoomResponse;
import core.chat.entity.ChatRoom;
import core.chat.repository.ChatRoomRepository;
import core.chat.service.dto.ChatRoomDto;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional(readOnly = true)
    public boolean checkIsValidRoomId(String userId, Long roomId) {
        Optional<ChatRoomDto> optionalChatRoomDto = chatRoomRepository.findChatRoomByRoomId(roomId);
        return optionalChatRoomDto.map(
                chatRoomDto -> chatRoomDto.getUserId().equals(userId)
        ).orElse(true);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRoomsLatest(String userId, int limit) {
        return chatRoomRepository.findAllByUserIdLatest(userId, limit).stream()
                .map(ChatRoomResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRoomsAfter(String userId, Long lastRoomId, int limit) {
        return chatRoomRepository.findAllByUserIdAfterRoomId(userId, lastRoomId, limit).stream()
                .map(ChatRoomResponse::of)
                .toList();
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        chatRoomRepository.deleteChatRoomById(roomId);
    }

    @Transactional
    public void saveChatRoom(ChatRoom chatRoom) {
        chatRoomRepository.insertChatRoomWithoutSelect(chatRoom);
    }
}
