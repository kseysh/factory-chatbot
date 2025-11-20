package core.chat.repository;

import core.chat.entity.ChatRoom;
import core.chat.service.dto.ChatRoomDto;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository {

    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final EntityManager em;

    @Override
    public void insertWithoutSelect(ChatRoom chatRoom) {
        em.persist(chatRoom);
    }

    @Override
    public Optional<ChatRoomDto> findByRoomId(Long roomId){
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
    public void deleteById(Long roomId) {
        chatRoomJpaRepository.deleteById(roomId);
    }

    @Override
    public boolean existsByRoomIdAndUserId(Long roomId, String userId) {
        return chatRoomJpaRepository.existsByIdAndUserId(roomId, userId);
    }
}
