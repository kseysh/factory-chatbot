package core.mcpdbserver.service;

import core.mcpdbserver.repository.EnergyDataSetRepository;
import core.mcpdbserver.service.response.EnergyUsageInfo;
import core.mcpdbserver.service.response.EnergyUsageMetaInfo;
import core.mcpdbserver.service.response.EnergyUsageResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnergyDataSetService {

    private final EnergyDataSetRepository energyDataSetRepository;

    @Tool(name = "get_energy_usages_by_date_range", description = "Get energy usages list using date range and roomId")
    public EnergyUsageResponse getEnergyUsagesByDateRange(
            @ToolParam(description = "Start time in ISO-8601 format") String startDateTime,
            @ToolParam(description = "end time in ISO-8601 format") String endDateTime,
            @ToolParam Long buildingId
    ) {
        log.info("get_energy_usages_by_date_range Tool Use, param : {}, {}, {}", startDateTime, endDateTime, buildingId);
        List<EnergyUsageInfo> energyUsageInfos =
                energyDataSetRepository.findByMeasurementDateTimeBetweenAndBuildingId(
                        LocalDateTime.parse(startDateTime, DateTimeFormatter.ISO_DATE_TIME),
                        LocalDateTime.parse(endDateTime, DateTimeFormatter.ISO_DATE_TIME),
                        buildingId
                );
        EnergyUsageMetaInfo meta = new EnergyUsageMetaInfo(buildingId);
        return new EnergyUsageResponse(meta, energyUsageInfos);
    }

    @Tool(name = "get_energy_usage", description = "Get a single energy usage using measurementDateTime and roomId")
    public EnergyUsageInfo getEnergyUsage(
            @ToolParam(description = "Measurement date time in ISO-8601 format") String measurementDateTime,
            @ToolParam Long buildingId
    ) {
        log.info("get_energy_usage Tool Use, param : {}, {}", measurementDateTime, buildingId);
        return energyDataSetRepository.findByMeasurementDateTimeAndBuildingId(
                LocalDateTime.parse(measurementDateTime, DateTimeFormatter.ISO_DATE_TIME),
                buildingId
                ).orElseThrow();
    }
}
