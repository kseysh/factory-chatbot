package core.chat.controller.response;

import core.chat.entity.ChatHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ChatResponseDeprecated {
    private Long roomId;
    private Long answerId;
    private String answer;

    public static ChatResponseDeprecated of(ChatHistory chatHistory) {
        return ChatResponseDeprecated.builder()
                .roomId(chatHistory.getRoomId())
                .answerId(chatHistory.getId())
                .answer(chatHistory.getContent())
                .build();
    }
}
