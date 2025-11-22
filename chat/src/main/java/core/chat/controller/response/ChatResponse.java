package core.chat.controller.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PUBLIC)
public class ChatResponse {
    private Long roomId;
    private Long userChatId;
    private Long llmChatId;
    private String answer;
}
