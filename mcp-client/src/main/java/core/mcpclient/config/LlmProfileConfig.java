package core.mcpclient.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LlmProfileConfig {
    @Configuration
    @Profile("local")
    @PropertySource("classpath:/mcp-client/llm-ollama.properties")
    public static class LocalConfig {
    }

    @Configuration
    @Profile("prod")
    @PropertySource("classpath:/mcp-client/llm-bedrock.properties")
    public static class ProdConfig {
    }
}