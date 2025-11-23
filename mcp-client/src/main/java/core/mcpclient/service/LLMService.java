package core.mcpclient.service;

import static core.mcpclient.service.constant.PromptContent.SYSTEM_PROMPT_CREATE_NEW_CHAT;
import static core.mcpclient.service.constant.PromptContent.SYSTEM_PROMPT_DEFAULT_CHAT;

import core.mcpclient.service.dto.NewChatRoomInfo;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

@Service
@Slf4j
@RequiredArgsConstructor
public class LLMService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    public static final char TITLE_SEPARATOR = '§';

    @Transactional
    public String chat(Long roomId, String question) {
        String conversationId = roomId.toString();
        CallResponseSpec response = this.getChatClientRequestSpec(
                conversationId,
                question,
                SYSTEM_PROMPT_DEFAULT_CHAT.getContent()
        ).call();

        return response.content();
    }

    @Transactional
    public Flux<String> chatStream(Long roomId, String question) {
        String conversationId = roomId.toString();
        return this.getChatClientRequestSpec(
                        conversationId,
                        question,
                        SYSTEM_PROMPT_DEFAULT_CHAT.getContent()
                ).stream().content();
    }

    @Transactional
    public NewChatRoomInfo startNewChat(Long roomId, String question) {
        String conversationId = roomId.toString();
        CallResponseSpec response = this.getChatClientRequestSpec(
                conversationId,
                question,
                SYSTEM_PROMPT_CREATE_NEW_CHAT.getContent()
        ).call();

        String responseContent = response.content();

        int separatorIndex = responseContent.indexOf(TITLE_SEPARATOR);
        if (separatorIndex == -1) throw new RuntimeException("LLM의 답변에 제목과 본문을 구분하는 구분자가 존재하지 않습니다.");
        String roomName = responseContent.substring(0, separatorIndex);
        String answer = responseContent.substring(separatorIndex + 1);
        return new NewChatRoomInfo(roomName, answer);
    }

    @Transactional
    public Flux<NewChatRoomInfo> startNewChatStream(Long roomId, String question) {
        String conversationId = roomId.toString();
        AtomicBoolean isAnswerMode = new AtomicBoolean(false);

        return this.getChatClientRequestSpec(conversationId, question, SYSTEM_PROMPT_CREATE_NEW_CHAT.getContent())
                .stream()
                .content()
                .handle((String chunk, SynchronousSink<NewChatRoomInfo> sink) -> {
                    if (isAnswerMode.get()) {
                        sink.next(new NewChatRoomInfo(null, chunk));
                        return;
                    }

                    int separatorIndex = chunk.indexOf(TITLE_SEPARATOR);
                    if (separatorIndex == -1) {
                        sink.next(new NewChatRoomInfo(chunk, null));
                    } else {
                        String remainingTitle = chunk.substring(0, separatorIndex);
                        String remainingAnswer = chunk.substring(separatorIndex + 1);

                        sink.next(new NewChatRoomInfo(
                                remainingTitle.isEmpty() ? null : remainingTitle,
                                remainingAnswer.isEmpty() ? null : remainingAnswer)
                        );
                        isAnswerMode.set(true);
                    }
                });
    }

    private ChatClientRequestSpec getChatClientRequestSpec(
            String conversationId, String question, String systemPrompt
    ) {
        List<Message> messages = chatMemory.get(conversationId);
        messages.add(new UserMessage(question));
        Prompt prompt = new Prompt(messages);
        return chatClient.prompt(prompt).system(systemPrompt);
    }

}
