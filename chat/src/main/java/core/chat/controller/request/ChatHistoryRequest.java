package core.chat.controller.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ChatHistoryRequest {
    private static final int DEFAULT_CHAT_SIZE = 20;
    private static final int MIN_CHATS_PAGE_SIZE = 1;
    private static final int MAX_CHATS_PAGE_SIZE = 100;

    @NotNull(message = "채팅방 ID는 필수 입력 값입니다.")
    private final Long roomId;

    @Nullable
    private final Long lastChatId;

    @Min(MIN_CHATS_PAGE_SIZE)
    @Max(MAX_CHATS_PAGE_SIZE)
    @Nullable
    private Integer size;

    public ChatHistoryRequest(Long roomId, @Nullable Long lastChatId, @Nullable Integer size) {
        if (size == null) this.size = DEFAULT_CHAT_SIZE;
        this.roomId = roomId;
        this.lastChatId = lastChatId;
    }
}
