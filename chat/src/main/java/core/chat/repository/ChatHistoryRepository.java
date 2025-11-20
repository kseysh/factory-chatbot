package core.chat.repository;

import core.chat.entity.ChatHistory;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatHistoryRepository {
    void insertChatHistoryWithoutSelect(ChatHistory chatHistory);

    void deleteChatHistoryByRoomId(Long roomId);

    List<ChatHistory> findAllByRoomIdLatest(Long roomId, Integer limit);

    List<ChatHistory> findAllByRoomIdAfterChatId(Long roomId, Long lastChatId, Integer limit);
}
