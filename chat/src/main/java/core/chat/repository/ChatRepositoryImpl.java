package core.chat.repository;

import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepository {
    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final EntityManager em;

    @Override
    public boolean existsByUserIdAndRoomId(String userId, Long roomId) {
        return chatRoomJpaRepository.existsByUserIdAndId(userId, roomId);
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

}
