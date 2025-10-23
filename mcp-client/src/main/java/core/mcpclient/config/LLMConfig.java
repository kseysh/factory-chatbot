package core.mcpclient.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LLMConfig {
    private final ChatModel chatModel;

    @Bean
    public ChatClient chatClient() {
        return ChatClient.create(chatModel);
    }
}