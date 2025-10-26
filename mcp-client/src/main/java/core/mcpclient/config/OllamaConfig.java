package core.mcpclient.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Profile("local")
public class OllamaConfig {

    @Bean
    public ChatClient ollamaChatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}