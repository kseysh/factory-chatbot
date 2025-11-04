package core.chat.controller.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatRoomListRequest {
    private static final int DEFAULT_CHAT_ROOMS_SIZE = 20;
    private static final int MIN_CHAT_ROOM_PAGE_SIZE = 1;
    private static final int MAX_CHAT_ROOM_PAGE_SIZE = 100;

    @Nullable
    private Long lastRoomId;

    @Min(MIN_CHAT_ROOM_PAGE_SIZE)
    @Max(MAX_CHAT_ROOM_PAGE_SIZE)
    private int size = DEFAULT_CHAT_ROOMS_SIZE;
}
