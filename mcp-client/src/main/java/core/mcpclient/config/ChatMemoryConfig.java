package core.mcpclient.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    public static final int MAX_MESSAGES = 20;

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository customChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(MAX_MESSAGES)
                .chatMemoryRepository(customChatMemoryRepository)
                .build();
    }
}
