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
public class ChatHistory {

    @Id
    private Long id;

    @NotNull
    @Column(name = "room_id")
    private Long roomId;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    public static ChatHistory createUserChatHistory(Long roomId, String content) {
        return ChatHistory.builder()
                .id(Snowflake.getInstance().nextId())
                .roomId(roomId)
                .type(MessageType.USER)
                .content(content)
                .build();
    }

    public static ChatHistory createLLMChatHistory(Long roomId, String content) {
        return ChatHistory.builder()
                .id(Snowflake.getInstance().nextId())
                .roomId(roomId)
                .type(MessageType.LLM)
                .content(content)
                .build();
    }
}
