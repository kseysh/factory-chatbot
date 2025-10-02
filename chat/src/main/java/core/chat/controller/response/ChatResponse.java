package core.chat.controller.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatResponse {
    String  message;

    public static ChatResponse of(String message) {
        return new ChatResponse(message);
    }
}
