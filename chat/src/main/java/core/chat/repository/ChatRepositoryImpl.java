package core.chat.repository;

import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import core.chat.service.dto.ChatRoomDto;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepository {
    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final ChatHistoryJpaRepository chatHistoryJpaRepository;
    private final EntityManager em;

    @Override
    public Optional<ChatRoomDto> findChatRoomByRoomId(Long roomId){
        Optional<ChatRoom> optionalChatRoom = chatRoomJpaRepository.findById(roomId);
        return optionalChatRoom.map(ChatRoomDto::of);
    }

    @Override
    public List<ChatRoom> findAllByUserIdAfterRoomId(String userId, Long roomId, int limit) {
        return chatRoomJpaRepository.findAllByUserIdAfterRoomId(userId, roomId, limit);
    }

    @Override
    public List<ChatRoom> findAllByUserIdLatest(String userId, int limit) {
        return chatRoomJpaRepository.findAllByUserIdLatest(userId, limit);
    }

    @Override
    public void insertChatRoomWithoutSelect(ChatRoom chatRoom) {
        em.persist(chatRoom);
    }

    @Override
    public void insertChatHistoryWithoutSelect(ChatHistory chatHistory){
        em.persist(chatHistory);
    }

    @Override
    public void deleteChatHistoryByRoomId(Long roomId) {
        chatHistoryJpaRepository.deleteByRoomId(roomId);
    }

    @Override
    public void deleteChatRoomById(Long roomId) {
        chatRoomJpaRepository.deleteById(roomId);
    }

}
