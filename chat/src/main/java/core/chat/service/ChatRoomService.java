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

    @Transactional
    public void saveChatRoom(ChatRoom chatRoom) {
        chatRoomRepository.insertWithoutSelect(chatRoom);
    }

    @Transactional(readOnly = true)
    public Optional<ChatRoomDto> findChatRoomByRoomId(Long roomId) {
        return chatRoomRepository.findByRoomId(roomId);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> findChatRoomsLatest(String userId, int limit) {
        return chatRoomRepository.findAllByUserIdLatest(userId, limit).stream()
                .map(ChatRoomResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> findChatRoomsAfter(String userId, Long lastRoomId, int limit) {
        return chatRoomRepository.findAllByUserIdAfterRoomId(userId, lastRoomId, limit).stream()
                .map(ChatRoomResponse::of)
                .toList();
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        chatRoomRepository.deleteById(roomId);
    }

    @Transactional(readOnly = true)
    public boolean canUserAccessRoom(Long roomId, String userId) {
        return chatRoomRepository.existsByRoomIdAndUserId(roomId, userId);
    }
}
