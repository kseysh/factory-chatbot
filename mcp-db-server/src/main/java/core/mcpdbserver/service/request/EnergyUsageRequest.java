package core.mcpdbserver.service.request;

import java.time.LocalDateTime;

public record EnergyUsageRequest(LocalDateTime measurementDateTime, Long buildingId) { }
