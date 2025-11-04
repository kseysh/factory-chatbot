package core.chat.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatRequest {
    @NotNull(message = "채팅방 ID는 필수 입력 값입니다.")
    private Long roomId;

    @NotBlank(message = "질문은 필수 입력 값입니다.")
    private String question;
}