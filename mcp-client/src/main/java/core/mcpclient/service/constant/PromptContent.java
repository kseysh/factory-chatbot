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
            + "You MUST return ONLY a valid JSON object with exactly two keys: roomName and answer. "
            + "Do NOT call any tools or functions. "
            + "Do NOT include markdown, code fences, or any text outside the JSON object. "
            + "roomName: 3-5 Korean words summarizing the user's question (string). "
            + "answer: your response to the user's question (string). "
            + "Example: {\"roomName\":\"질문 요약 3~5단어\",\"answer\":\"상세한 답변\"}"
    ),
    SYSTEM_PROMPT_DEFAULT_CHAT(
           "Tool을 적극적으로 활용하세요" // SYSTEM_PROMPT_DEFAULT_CHAT은 Null이거나 빈칸이면 안됩니다.
    );

    // USER_PROMPT
    // ASSISTANT_PROMPT
    // TOOL_PROMPT
    private final String content;
}
