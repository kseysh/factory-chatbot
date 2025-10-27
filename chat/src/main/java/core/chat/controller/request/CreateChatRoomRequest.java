package core.chat.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CreateChatRoomRequest {
    @NotBlank(message = "질문은 필수 입력 값입니다.")
    private String question;
}
