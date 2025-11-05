package core.mcpclient.config;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("local")
public class OllamaConfig {

    private final ToolCallbackProvider toolCallbackProvider;

    @Bean
    public ChatClient ollamaChatClient(ChatModel chatModel) {
        log.info("사용할 수 있는 Tool: ");
        log.info(Arrays.toString(toolCallbackProvider.getToolCallbacks()));
        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }
}