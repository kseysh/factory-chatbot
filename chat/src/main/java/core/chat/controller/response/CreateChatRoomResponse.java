package core.chat.controller.response;

import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PUBLIC)
public class CreateChatRoomResponse {

    private Long roomId;
    private String roomName;
    private Long userChatId;
    private Long llmChatId;
    private String answer;
}
