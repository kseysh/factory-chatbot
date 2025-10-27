package core.mcpclient.service;

import core.mcpclient.service.constant.PromptContent;
import core.mcpclient.service.dto.NewChatRoomInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LLMService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public String chat(Long roomId, String question) {
        String conversationId = roomId.toString();
        chatMemory.add(conversationId, new UserMessage(question));
        Prompt prompt = new Prompt(chatMemory.get(conversationId));
        return chatClient.prompt(prompt).call().content();
    }

    public NewChatRoomInfo startNewChat(Long roomId, String question) {
        String conversationId = roomId.toString();
        chatMemory.add(conversationId, new UserMessage(question));

        return chatClient.prompt(new Prompt(chatMemory.get(conversationId)))
                .system(PromptContent.SYSTEM_PROMPT_CREATE_NEW_CHAT.getContent())
                .call()
                .entity(NewChatRoomInfo.class);
    }

}
