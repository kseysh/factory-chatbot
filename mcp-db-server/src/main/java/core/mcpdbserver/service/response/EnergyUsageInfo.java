package core.mcpdbserver.service.response;

import java.time.LocalDateTime;

public record EnergyUsageInfo(LocalDateTime measurementDateTime, Float energyUsage) {

}
