package core.mcpclient.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewChatRoomInfo(String roomName, String answer) {
    public NewChatRoomInfo {
        if (roomName == null || roomName.isBlank()) {
            throw new IllegalArgumentException("roomName은 필수 값입니다.");
        }
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("answer는 필수 값입니다.");
        }
    }
}
