package core.chat.repository;

import core.chat.entity.ChatHistory;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatHistoryRepositoryImpl implements ChatHistoryRepository {

    private final ChatHistoryJpaRepository chatHistoryJpaRepository;
    private final EntityManager em;

    @Override
    public void insertChatHistoryWithoutSelect(ChatHistory chatHistory){
        em.persist(chatHistory);
    }

    @Override
    public void deleteChatHistoryByRoomId(Long roomId) {
        chatHistoryJpaRepository.deleteByRoomId(roomId);
    }

    @Override
    public List<ChatHistory> findAllByRoomIdLatest(Long roomId, Integer limit) {
        return chatHistoryJpaRepository.findAllByRoomIdLatest(roomId, limit);
    }

    @Override
    public List<ChatHistory> findAllByRoomIdAfterChatId(Long roomId, Long lastChatId, Integer limit) {
        return chatHistoryJpaRepository.findAllByRoomIdAfterChatId(roomId, lastChatId, limit);
    }
}
