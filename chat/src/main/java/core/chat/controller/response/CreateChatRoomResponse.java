package core.chat.controller.response;

import core.chat.entity.ChatHistory;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CreateChatRoomResponse {

    Long roomId;
    String roomName;
    Long answerId;
    String answer;

    public static CreateChatRoomResponse of(String roomName, ChatHistory chatHistory) {
        return CreateChatRoomResponse.builder()
                .roomId(chatHistory.getRoomId())
                .roomName(roomName)
                .answerId(chatHistory.getId())
                .answer(chatHistory.getContent())
                .build();
    }
}
