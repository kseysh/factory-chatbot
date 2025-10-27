package core.chat.controller.response;

import core.chat.entity.ChatHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ChatResponse {
    Long roomId;
    Long answerId;
    String answer;

    public static ChatResponse of(ChatHistory chatHistory) {
        return ChatResponse.builder()
                .roomId(chatHistory.getRoomId())
                .answerId(chatHistory.getId())
                .answer(chatHistory.getContent())
                .build();
    }
}
