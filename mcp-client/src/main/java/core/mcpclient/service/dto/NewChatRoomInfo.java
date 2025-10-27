package core.mcpclient.service.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewChatRoomInfo {

    private String roomName;
    private String answer;
}
