package core.chat.entity;

import core.common.snowflake.Snowflake;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ChatRoom {
    @Id
    private Long id;

    @NotNull
    private String userId;

    @NotNull
    private String name;

    @NotNull
    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    public static ChatRoom createChatRoom(String userId, String roomName) {
        return ChatRoom.builder()
                .id(Snowflake.getInstance().nextId())
                .userId(userId)
                .name(roomName)
                .build();
    }
}
