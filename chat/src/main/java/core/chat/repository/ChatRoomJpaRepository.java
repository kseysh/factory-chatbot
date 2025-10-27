package core.chat.repository;

import core.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByUserIdAndId(String userId, Long roomId);
}