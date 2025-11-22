package core.chat.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public enum MessageType {
    USER("USER"),
    ASSISTANT("ASSISTANT"),
    SYSTEM("SYSTEM"),
    TOOL("TOOL");

    private final String value;
}
