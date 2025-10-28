package core.chat.repository;

import core.chat.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatHistoryJpaRepository extends JpaRepository<ChatHistory, Long> {

    @Modifying
    @Query("DELETE FROM ChatHistory ch WHERE ch.roomId = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);
}
