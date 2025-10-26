package core.mcpclient.config;

import core.mcpclient.config.properties.BedrockProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.bedrock.converse.BedrockChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.bedrock.autoconfigure.BedrockAwsConnectionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(BedrockProperties.class)
@Profile("prod")
public class BedrockConfig {

    private final BedrockProperties bedrockProperties;
    private final BedrockAwsConnectionProperties awsConnectionProperties;

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        log.info("🤔 Creating BedrockRuntimeClient");
        log.info("📝 Region: {}", awsConnectionProperties.getRegion());

        BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .region(Region.of(awsConnectionProperties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .overrideConfiguration(config -> config
                        .apiCallTimeout(Duration.parse("PT" + awsConnectionProperties.getTimeout()))
                        .apiCallAttemptTimeout(Duration.parse("PT" + awsConnectionProperties.getTimeout())))
                .build();

        log.info("✅ BedrockRuntimeClient created");
        return client;
    }

    @Bean
    public BedrockChatOptions bedrockChatOptions() {
        log.info("📋 Creating BedrockChatOptions");
        log.info("📝 Model ID: {}", bedrockProperties.getModelId());
        log.info("📝 Temperature: {}", bedrockProperties.getTemperature());
        log.info("📝 Max Tokens: {}", bedrockProperties.getMaxTokens());

        return BedrockChatOptions.builder()
                .model(bedrockProperties.getModelId())
                .temperature(bedrockProperties.getTemperature())
                .maxTokens(bedrockProperties.getMaxTokens())
                .build();
    }

    @Bean
    public BedrockProxyChatModel bedrockProxyChatModel(
            BedrockRuntimeClient client,
            BedrockChatOptions chatOptions) {
        log.info("🤔 Creating BedrockProxyChatModel");

        BedrockProxyChatModel model = BedrockProxyChatModel.builder()
                .defaultOptions(chatOptions)
                .bedrockRuntimeClient(client)
                .region(Region.of(awsConnectionProperties.getRegion()))
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
        log.info("📝 Model ID from options: {}",
                chatModel.getDefaultOptions().getModel());

        ChatClient client = ChatClient.builder(chatModel)
                .build();

        log.info("✅ ChatClient created");
        return client;
    }
}