package core.chat.controller.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatHistoriesResponse {
    private Long roomId;
    private List<ChatHistoryResponse> chatRooms;

    public static ChatHistoriesResponse of(Long roomId, List<ChatHistoryResponse> chatRooms) {
        return new ChatHistoriesResponse(roomId, chatRooms);
    }
}
