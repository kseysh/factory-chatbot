package core.mcpclient.service;

import core.mcpclient.service.constant.PromptContent;
import core.mcpclient.service.dto.NewChatRoomInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LLMService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    @Transactional
    public String chat(Long roomId, String question) {
        String conversationId = roomId.toString();
        chatMemory.add(conversationId, new UserMessage(question));
        CallResponseSpec response = chatClient.prompt(new Prompt(chatMemory.get(conversationId)))
                .system(PromptContent.SYSTEM_PROMPT_DEFAULT_CHAT.getContent())
                .call();
        checkLlmResponse(response);

        String answer = response.content();
        chatMemory.add(conversationId, new AssistantMessage(answer));
        return answer;
    }

    @Transactional
    public NewChatRoomInfo startNewChat(Long roomId, String question) {
        String conversationId = roomId.toString();

        chatMemory.add(conversationId, new UserMessage(question));
        CallResponseSpec response = chatClient.prompt(new Prompt(chatMemory.get(conversationId)))
                .system(PromptContent.SYSTEM_PROMPT_CREATE_NEW_CHAT.getContent())
                .call();
        checkLlmResponse(response);
        NewChatRoomInfo answerDto = response.entity(NewChatRoomInfo.class);

        chatMemory.add(conversationId, new AssistantMessage(response.content()));
        return answerDto;
    }

    private void checkLlmResponse(CallResponseSpec response) {
        if(response == null) {
            throw new IllegalArgumentException("LLM response is null");
        }
        String content = response.content();
        if (content == null || content.trim().isEmpty()){
            throw new IllegalArgumentException("LLM response content is null or empty");
        }
    }

}
