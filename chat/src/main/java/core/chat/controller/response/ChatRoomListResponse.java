package core.chat.controller.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoomListResponse {
    private List<ChatRoomResponse> chatRooms;

    public static ChatRoomListResponse of(List<ChatRoomResponse> chatRooms) {
        return new ChatRoomListResponse(chatRooms);
    }
}
