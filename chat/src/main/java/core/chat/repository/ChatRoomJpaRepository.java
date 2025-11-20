package core.chat.repository;

import core.chat.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT c FROM ChatRoom c WHERE c.userId = :userId AND c.id < :lastRoomId ORDER BY c.id DESC LIMIT :limit")
    List<ChatRoom> findAllByUserIdAfterRoomId(
            @Param("userId") String userId,
            @Param("lastRoomId") Long lastRoomId,
            @Param("limit") int limit
    );

    @Query("SELECT c FROM ChatRoom c WHERE c.userId = :userId ORDER BY c.id DESC LIMIT :limit")
    List<ChatRoom> findAllByUserIdLatest(@Param("userId") String userId, @Param("limit") int limit);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ChatRoom c WHERE c.id = :id")
    void deleteById(@Param("id") Long id);

    boolean existsByIdAndUserId(Long id, String userId);
}