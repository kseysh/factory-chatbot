package core.chat.controller.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class ChatRoomListRequest {
    private static final int DEFAULT_CHAT_ROOMS_SIZE = 20;
    private static final int MIN_CHAT_ROOM_PAGE_SIZE = 1;
    private static final int MAX_CHAT_ROOM_PAGE_SIZE = 100;

    @Nullable
    private final Long lastRoomId;

    @Min(MIN_CHAT_ROOM_PAGE_SIZE)
    @Max(MAX_CHAT_ROOM_PAGE_SIZE)
    @Nullable
    private final Integer size;

    public ChatRoomListRequest(@Nullable Long lastRoomId, @Nullable Integer size) {
        this.size = (size == null) ? DEFAULT_CHAT_ROOMS_SIZE : size;
        this.lastRoomId = lastRoomId;
    }
}
