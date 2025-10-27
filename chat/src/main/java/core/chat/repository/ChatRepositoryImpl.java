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
    public List<Long> findAllRoomIdByUserId(String userId) {
        return chatRoomJpaRepository.findAllRoomIdByUserId(userId);
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
