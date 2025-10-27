package core.mcpclient.service.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PromptContent {
    // SYSTEM_PROMPT
    SYSTEM_PROMPT_CREATE_NEW_CHAT(
            "You are a helpful assistant for Korean users. Reply in Korean only. "
            + "Return ONLY a JSON object with exactly two keys: "
            + "\"roomName\" and \"answer\". "
            + "\"roomName\": 3-5 Korean words summarizing the user's question. "
            + "\"answer\": a detailed answer. "
            + "Do NOT include code fences, explanations, or extra fields. "
            + "Example: {\"roomName\":\"질문 요약 3~5단어\",\"answer\":\"상세한 답변\"}"
    );

    // USER_PROMPT
    // ASSISTANT_PROMPT
    // TOOL_PROMPT
    private final String content;
}
