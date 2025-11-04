package core.chat.controller.response;

import core.chat.service.dto.ChatHistoryInfo;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatHistoryResponse {
    private Long roomId;
    private List<ChatHistoryInfo> chatRooms;

    public static ChatHistoryResponse of(Long roomId, List<ChatHistoryInfo> chatRooms) {
        return new ChatHistoryResponse(roomId, chatRooms);
    }
}
