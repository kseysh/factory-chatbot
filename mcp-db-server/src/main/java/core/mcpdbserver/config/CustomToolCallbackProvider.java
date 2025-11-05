package core.mcpdbserver.config;

import core.mcpdbserver.service.DateTimeService;
import core.mcpdbserver.service.EnergyDataSetService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CustomToolCallbackProvider {

    @Bean
    @Primary
    public ToolCallbackProvider tools(
            EnergyDataSetService energyDataSetService,
            DateTimeService dateTimeService
    ) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(energyDataSetService, dateTimeService)
                .build();

    }
}
