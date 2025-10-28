package core.chat.repository;

import core.chat.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatHistoryJpaRepository extends JpaRepository<ChatHistory, Long> {

    void deleteByRoomId(Long roomId);
}
