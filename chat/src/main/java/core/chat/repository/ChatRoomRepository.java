package core.chat.repository;

import core.chat.entity.ChatRoom;
import core.chat.service.dto.ChatRoomDto;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository {
    Optional<ChatRoomDto> findByRoomId(Long roomId);

    List<ChatRoom> findAllByUserIdAfterRoomId(String userId, Long roomId, int size);
    List<ChatRoom> findAllByUserIdLatest(String userId, int size);

    void insertWithoutSelect(ChatRoom chatRoom);
    void deleteById(Long roomId);

    boolean existsByRoomIdAndUserId(Long roomId, String userId);
}
