package core.chat.service.dto;

import core.chat.entity.ChatHistory;
import core.chat.entity.MessageType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatHistoryDto {
    private final Long chatId;
    private final String content;
    private final Boolean isChatbot;

    public static ChatHistoryDto of(ChatHistory chatHistory) {
        boolean isChatbot = chatHistory.getType() == MessageType.LLM;
        return new ChatHistoryDto(chatHistory.getId(), chatHistory.getContent(), isChatbot);
    }
}
