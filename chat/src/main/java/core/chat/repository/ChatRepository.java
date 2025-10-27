package core.chat.repository;

import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import java.util.List;

public interface ChatRepository {
    List<Long> findAllRoomIdByUserId(String userId);

    void insertChatRoomWithoutSelect(ChatRoom chatRoom);
    void insertChatHistoryWithoutSelect(ChatHistory chatHistory);
}
