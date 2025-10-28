package core.chat.repository;

import core.chat.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByUserIdAndId(String userId, Long roomId);

    @Query("SELECT c FROM ChatRoom c WHERE c.userId = :userId AND c.id < :lastRoomId ORDER BY c.id DESC LIMIT :limit")
    List<ChatRoom> findAllByUserIdAfterRoomId(String userId, Long lastRoomId, int limit);

    @Query("SELECT c FROM ChatRoom c WHERE c.userId = :userId ORDER BY c.id DESC LIMIT :limit")
    List<ChatRoom> findAllByUserIdLatest(String userId, int limit);
}