package core.chat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;


@Table(
    name = "SPRING_AI_CHAT_MEMORY",
    indexes = @Index(
        name = "SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX",
        columnList = "conversation_id, timestamp"
    )
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpringAiChatMemory {
    @Id
    @Column(name = "conversation_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private String conversationId;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(10)")
    private MessageType type;

    @NotNull
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime timestamp;
}
