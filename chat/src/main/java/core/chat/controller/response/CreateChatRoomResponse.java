package core.chat.controller.response;

import core.chat.entity.ChatHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CreateChatRoomResponse {
    String roomId;
    String roomName;
    Long answerId;
    String answer;

    public CreateChatRoomResponse of(String roomName, ChatHistory chatHistory) {
        return CreateChatRoomResponse.builder()
                .roomId(chatHistory.getRoomId())
                .roomName(roomName)
                .answerId(chatHistory.getId())
                .answer(chatHistory.getContent())
                .build();
    }
}
