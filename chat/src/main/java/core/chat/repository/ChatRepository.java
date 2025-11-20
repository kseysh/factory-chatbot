package core.chat.repository;

import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import core.chat.service.dto.ChatRoomDto;
import java.util.List;
import java.util.Optional;

public interface ChatRepository {
    Optional<ChatRoomDto> findChatRoomByRoomId(Long roomId);

    List<ChatRoom> findAllByUserIdAfterRoomId(String userId, Long roomId, int size);
    List<ChatRoom> findAllByUserIdLatest(String userId, int size);

    void insertChatRoomWithoutSelect(ChatRoom chatRoom);
    void insertChatHistoryWithoutSelect(ChatHistory chatHistory);

    void deleteChatHistoryByRoomId(Long roomId);
    void deleteChatRoomById(Long roomId);
}
