package core.mcpclient.service.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public enum PromptContent {
    // SYSTEM_PROMPT
    SYSTEM_PROMPT_CREATE_NEW_CHAT("You are a helpful assistant. When a user asks a question, "
            + "provide both a brief room name (summary of the question in 3-5 words) and a detailed answer. "
            + "Always respond in JSON format.");
    // USER_PROMPT
    // ASSISTANT_PROMPT
    // TOOL_PROMPT
    private final String content;
}
