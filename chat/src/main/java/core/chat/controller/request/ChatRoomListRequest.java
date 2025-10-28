package core.chat.controller.request;

import jakarta.annotation.Nullable;
import lombok.Getter;

@Getter
public class ChatRoomListRequest {
    private static final int DEFAULT_CHAT_ROOMS_SIZE = 20;
    private static final int MIN_CHAT_ROOM_PAGE_SIZE = 1;
    private static final int MAX_CHAT_ROOM_PAGE_SIZE = 100;

    @Nullable
    private final Long lastRoomId;

    private final int size;

    public ChatRoomListRequest(@Nullable Long lastRoomId, Integer size) {
        this.lastRoomId = lastRoomId;
        this.size = size == null ? DEFAULT_CHAT_ROOMS_SIZE : size;
        if(this.size < MIN_CHAT_ROOM_PAGE_SIZE || this.size > MAX_CHAT_ROOM_PAGE_SIZE){
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
    }
}
