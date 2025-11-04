package core.chat.repository;

import core.chat.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByUserIdAndId(String userId, Long roomId);

    @Query("SELECT c FROM ChatRoom c WHERE c.userId = :userId AND c.id < :lastRoomId ORDER BY c.id DESC LIMIT :limit")
    List<ChatRoom> findAllByUserIdAfterRoomId(
            @Param("userId") String userId,
            @Param("lastRoomId") Long lastRoomId,
            @Param("limit") int limit
    );

    @Query("SELECT c FROM ChatRoom c WHERE c.userId = :userId ORDER BY c.id DESC LIMIT :limit")
    List<ChatRoom> findAllByUserIdLatest(@Param("userId") String userId, @Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM ChatRoom cr WHERE cr.id = :id")
    void deleteById(@Param("id") Long id);

}