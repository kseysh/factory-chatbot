package core.mcpclient.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
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