package core.chat.repository;

import core.chat.entity.ChatHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatHistoryJpaRepository extends JpaRepository<ChatHistory, Long> {

    @Query("SELECT c FROM ChatHistory c WHERE c.roomId = :roomId AND c.id < :lastChatId ORDER BY c.id DESC LIMIT :limit")
    List<ChatHistory> findAllByRoomIdAfterChatId(
            @Param("roomId") Long roomId,
            @Param("lastChatId") Long lastChatId,
            @Param("limit") int limit
    );

    @Query("SELECT c FROM ChatHistory c WHERE c.roomId = :roomId ORDER BY c.id DESC LIMIT :limit")
    List<ChatHistory> findAllByRoomIdLatest(@Param("roomId") Long roomId, @Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM ChatHistory ch WHERE ch.roomId = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);
}
