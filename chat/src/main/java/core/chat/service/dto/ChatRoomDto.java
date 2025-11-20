package core.chat.service.dto;

import core.chat.entity.ChatRoom;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoomDto {

    private final Long id;
    @NotNull
    private final String userId;
    @NotNull
    private final String name;

    public static ChatRoomDto of(ChatRoom chatRoom) {
        return new ChatRoomDto(chatRoom.getId(), chatRoom.getUserId(), chatRoom.getName());
    }
}
