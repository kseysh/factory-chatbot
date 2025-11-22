package core.chat.controller.response;

import core.chat.entity.ChatHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CreateChatRoomResponseDeprecated {

    private Long roomId;
    private String roomName;
    private Long answerId;
    private String answer;

    public static CreateChatRoomResponseDeprecated of(String roomName, ChatHistory chatHistory) {
        return CreateChatRoomResponseDeprecated.builder()
                .roomId(chatHistory.getRoomId())
                .roomName(roomName)
                .answerId(chatHistory.getId())
                .answer(chatHistory.getContent())
                .build();
    }
}
