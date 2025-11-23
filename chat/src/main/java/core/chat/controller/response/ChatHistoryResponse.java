package core.chat.controller.response;

import core.chat.entity.ChatHistory;
import core.chat.entity.MessageType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatHistoryResponse {
    private final Long chatId;
    private final String content;
    private final Boolean isChatbot;

    public static ChatHistoryResponse of(ChatHistory chatHistory) {
        return new ChatHistoryResponse(
                chatHistory.getId(),
                chatHistory.getContent(),
                chatHistory.getType() == MessageType.ASSISTANT
        );
    }
}
