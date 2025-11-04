package core.chat.repository;

import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import java.util.List;

public interface ChatRepository {
    boolean existsByUserIdAndRoomId(String userId, Long roomId);

    List<ChatRoom> findAllByUserIdAfterRoomId(String userId, Long roomId, int size);
    List<ChatRoom> findAllByUserIdLatest(String userId, int size);

    void insertChatRoomWithoutSelect(ChatRoom chatRoom);
    void insertChatHistoryWithoutSelect(ChatHistory chatHistory);

    void deleteChatHistoryByRoomId(Long roomId);
    void deleteChatRoomById(Long roomId);
}
