package core.chat.repository;

import core.chat.entity.ChatHistory;
import core.chat.service.dto.ChatHistoryDto;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatHistoryRepository {
    void insertChatHistoryWithoutSelect(ChatHistory chatHistory);

    void deleteChatHistoryByRoomId(Long roomId);

    List<ChatHistoryDto> findAllByRoomIdLatest(Long roomId, Integer limit);

    List<ChatHistoryDto> findAllByRoomIdAfterChatId(Long roomId, Long lastChatId, Integer limit);
}
