package core.chat.repository;

import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;

public interface ChatRepository {
    boolean existsByUserIdAndRoomId(String userId, Long roomId);

    void insertChatRoomWithoutSelect(ChatRoom chatRoom);
    void insertChatHistoryWithoutSelect(ChatHistory chatHistory);
}
