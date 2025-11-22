package core.mcpclient.service;

import static core.mcpclient.service.constant.PromptContent.SYSTEM_PROMPT_CREATE_NEW_CHAT;
import static core.mcpclient.service.constant.PromptContent.SYSTEM_PROMPT_DEFAULT_CHAT;

import core.mcpclient.service.dto.NewChatRoomInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class LLMService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private static final char TITLE_SEPARATOR = '§';

    @Transactional
    public String chat(Long roomId, String question) {
        String conversationId = roomId.toString();
        CallResponseSpec response = this.getChatClientRequestSpec(
                conversationId,
                question,
                SYSTEM_PROMPT_DEFAULT_CHAT.getContent()
        ).call();
        checkLlmResponse(response);
        String answer = response.content();
        chatMemory.add(conversationId, new AssistantMessage(answer));
        return answer;
    }

    @Transactional
    public Flux<String> chatStream(Long roomId, String question) {
        String conversationId = roomId.toString();
        StringBuilder responseBuilder = new StringBuilder();
        return this.getChatClientRequestSpec(
                        conversationId,
                        question,
                        SYSTEM_PROMPT_DEFAULT_CHAT.getContent()
                ).stream().content()
                .mapNotNull(response -> {
                    responseBuilder.append(response);
                    return response;
                }).doOnComplete(() -> chatMemory.add(conversationId, new AssistantMessage(responseBuilder.toString())));
    }

    @Transactional
    public NewChatRoomInfo startNewChat(Long roomId, String question) {
        String conversationId = roomId.toString();
        CallResponseSpec response = this.getChatClientRequestSpec(
                conversationId,
                question,
                SYSTEM_PROMPT_CREATE_NEW_CHAT.getContent()
        ).call();
        checkLlmResponse(response);
        String answer = response.content();
        chatMemory.add(conversationId, new AssistantMessage(answer));

        int separatorIdx = answer.indexOf(TITLE_SEPARATOR);
        if (separatorIdx != -1) throw new RuntimeException("LLM의 답변에 제목과 본문을 구분하는 구분자가 존재하지 않습니다.");
        return new NewChatRoomInfo(answer.substring(0, separatorIdx), answer.substring(separatorIdx + 1));
    }

//    @Transactional
//    public Flux<NewChatRoomInfo> startNewChatStream(Long roomId, String question) {
//        String conversationId = roomId.toString();
//        return this.getChatClientRequestSpec(
//                conversationId,
//                question,
//                SYSTEM_PROMPT_CREATE_NEW_CHAT.getContent()
//        ).stream().content()
//                        .mapNotNull(response -> {
//            responseBuilder.append(response);
//            return response;
//        }).doOnComplete(() -> chatMemory.add(conversationId, new AssistantMessage(responseBuilder.toString())));
//    }

    private ChatClientRequestSpec getChatClientRequestSpec(
            String conversationId, String question, String systemPrompt
    ) {
        chatMemory.add(conversationId, new UserMessage(question));
        Prompt prompt = new Prompt(chatMemory.get(conversationId));
        return chatClient.prompt(prompt).system(systemPrompt);
    }

    private void checkLlmResponse(CallResponseSpec response) {
        if (response == null) {
            throw new IllegalArgumentException("LLM response is null");
        }
        String content = response.content();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("LLM response content is null or empty");
        }
    }

}
