package core.mcpclient.service.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewChatRoomInfo {

    String roomName;
    String answer;
}
