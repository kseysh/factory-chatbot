package core.chat.repository;

import core.chat.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr.id FROM ChatRoom cr WHERE cr.userId = :userId")
    List<Long> findAllRoomIdByUserId(String userId);
}