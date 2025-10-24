package core.mcpclient.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
public class LLMConfig {

    @Bean
    @Profile("local")
    public ChatClient ollamaChatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    @Profile("prod")
    public ChatClient bedrockChatClient(BedrockProxyChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}