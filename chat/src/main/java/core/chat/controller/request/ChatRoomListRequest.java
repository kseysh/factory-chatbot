package core.chat.controller.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatRoomListRequest {

    private Long lastRoomId;

    private int size;
}
