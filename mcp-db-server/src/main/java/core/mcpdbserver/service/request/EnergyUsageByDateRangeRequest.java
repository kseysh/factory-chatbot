package core.mcpdbserver.service.request;

import java.time.LocalDateTime;

public record EnergyUsageByDateRangeRequest(LocalDateTime startDateTime, LocalDateTime endDateTime, Long buildingId) { }
