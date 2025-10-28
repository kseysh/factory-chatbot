package core.chat.controller.response;

import core.chat.entity.ChatRoom;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoomResponse {
    Long roomId;
    String roomName;
    String date;

    public static ChatRoomResponse of(ChatRoom chatRoom) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy'년' M'월' d'일'");
        return new ChatRoomResponse(chatRoom.getId(), chatRoom.getName(), chatRoom.getCreatedAt().format(formatter));
    }
}
