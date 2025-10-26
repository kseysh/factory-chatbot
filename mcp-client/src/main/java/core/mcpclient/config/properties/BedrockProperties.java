package core.mcpclient.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(BedrockProperties.CONFIG_PREFIX)
public class BedrockProperties {

    public static final String CONFIG_PREFIX = "mcp.bedrock";

    private String modelId;

    private Double temperature;

    private Integer maxTokens;
}