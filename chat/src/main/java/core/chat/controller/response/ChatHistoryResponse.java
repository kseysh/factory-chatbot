package core.chat.controller.response;

import core.chat.service.dto.ChatHistoryDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatHistoryResponse {
    private final Long chatId;
    private final String content;
    private final Boolean isChatbot;

    public static ChatHistoryResponse of(ChatHistoryDto chatHistoryDto) {
        return new ChatHistoryResponse(
                chatHistoryDto.getChatId(),
                chatHistoryDto.getContent(),
                chatHistoryDto.getIsChatbot()
        );
    }
}
