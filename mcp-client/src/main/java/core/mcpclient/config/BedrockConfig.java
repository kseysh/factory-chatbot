package core.mcpclient.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.bedrock.converse.BedrockChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.time.Duration;

@Slf4j
@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class BedrockConfig {

    @Value("${spring.ai.bedrock.converse.chat.options.model}")
    private String modelId;

    @Value("${spring.ai.bedrock.aws.region}")
    private String region;

    @Value("${spring.ai.bedrock.converse.chat.options.max-tokens}")
    private Integer maxTokens;

    @Value("${spring.ai.bedrock.converse.chat.options.temperature}")
    private Double temperature;

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        log.info("🤔 Creating BedrockRuntimeClient");
        BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .overrideConfiguration(config -> config
                        .apiCallTimeout(Duration.ofMinutes(5))
                        .apiCallAttemptTimeout(Duration.ofMinutes(5)))
                .build();

        log.info("✅ BedrockRuntimeClient created");
        return client;
    }

    @Bean
    public BedrockChatOptions bedrockChatOptions() {
        return BedrockChatOptions.builder()
                .model(modelId)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }

    @Bean
    public BedrockProxyChatModel bedrockProxyChatModel(BedrockRuntimeClient client, BedrockChatOptions chatOptions) {
        log.info("🤔 Creating BedrockProxyChatModel");
        log.info("📝 Model ID: {}", modelId);
        log.info("📋 Options created - Model: {}, MaxTokens: {}", modelId, maxTokens);

        BedrockProxyChatModel model = BedrockProxyChatModel.builder()
                .defaultOptions(chatOptions)
                .bedrockRuntimeClient(client)
                .region(Region.of(region))
                .build();

        log.info("✅ BedrockProxyChatModel created");
        log.info("📋 Default options - Model: {}, MaxTokens: {}",
                model.getDefaultOptions().getModel(),
                model.getDefaultOptions().getMaxTokens());

        return model;
    }

    @Bean
    public ChatClient bedrockChatClient(BedrockProxyChatModel chatModel, BedrockChatOptions chatOptions) {
        log.info("=== Creating ChatClient ===");
        log.info("📋 ChatModel default options: {}", chatModel.getDefaultOptions());
        log.info("📝 Model ID from options: {}", chatModel.getDefaultOptions().getModel());

        ChatClient client = ChatClient.builder(chatModel)
                .defaultOptions(chatOptions)
                .build();

        log.info("✅ ChatClient created");
        return client;
    }
}