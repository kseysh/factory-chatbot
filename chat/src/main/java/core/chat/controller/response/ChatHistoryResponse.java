package core.chat.controller.response;

import core.chat.service.dto.ChatHistoryDto;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatHistoryResponse {
    private Long roomId;
    private List<ChatHistoryDto> chatRooms;

    public static ChatHistoryResponse of(Long roomId, List<ChatHistoryDto> chatRooms) {
        return new ChatHistoryResponse(roomId, chatRooms);
    }
}
