package core.chat.repository;

import core.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByUserIdAndId(String userId, Long roomId);

    @Modifying
    @Query("DELETE FROM ChatRoom cr WHERE cr.id = :id")
    void deleteById(@Param("id") Long id);
}