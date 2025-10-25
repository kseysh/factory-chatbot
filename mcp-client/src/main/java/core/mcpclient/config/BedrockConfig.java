package core.mcpclient.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.bedrock.converse.BedrockChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.time.Duration;

@Slf4j
@Configuration
@Profile("prod")
public class BedrockConfig {

    private static final String MODEL_ID = "us.anthropic.claude-3-5-sonnet-20240620-v1:0";

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        log.info("=== Creating BedrockRuntimeClient ===");

        BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .overrideConfiguration(config -> config
                        .apiCallTimeout(Duration.ofMinutes(5))
                        .apiCallAttemptTimeout(Duration.ofMinutes(5)))
                .build();

        log.info("✅ BedrockRuntimeClient created");
        return client;
    }

    @Bean
    @Primary
    public BedrockProxyChatModel bedrockProxyChatModel(BedrockRuntimeClient client) {
        log.info("=== Creating BedrockProxyChatModel ===");
        log.info("📝 Model ID: {}", MODEL_ID);

        // BedrockConverseApiOptions 사용
        BedrockChatOptions options = BedrockChatOptions.builder()
                .model(MODEL_ID)
                .temperature(0.7)
                .maxTokens(1000)
                .build();

        log.info("📋 Options created - Model: {}, MaxTokens: {}",
                options.getModel(), options.getMaxTokens());

        BedrockProxyChatModel model = BedrockProxyChatModel.builder()
                .defaultOptions(options)
                .bedrockRuntimeClient(client)
                .region(Region.US_WEST_2)
                .build();

        log.info("✅ BedrockProxyChatModel created");
        log.info("📋 Default options - Model: {}, MaxTokens: {}",
                model.getDefaultOptions().getModel(),
                model.getDefaultOptions().getMaxTokens());

        return model;
    }

    @Bean
    public ChatClient bedrockChatClient(BedrockProxyChatModel chatModel) {
        log.info("=== Creating ChatClient ===");
        log.info("📋 ChatModel default options: {}", chatModel.getDefaultOptions());
        log.info("📝 Model ID from options: {}", chatModel.getDefaultOptions().getModel());

        ChatClient client = ChatClient.builder(chatModel)
                .defaultOptions(BedrockChatOptions.builder()
                        .model(MODEL_ID)
                        .temperature(0.7)
                        .maxTokens(1000)
                        .build())
                .build();

        log.info("✅ ChatClient created");
        return client;
    }
}