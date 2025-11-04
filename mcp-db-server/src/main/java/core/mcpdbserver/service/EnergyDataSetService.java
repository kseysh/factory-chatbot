package core.mcpdbserver.service;

import core.mcpdbserver.repository.EnergyDataSetRepository;
import core.mcpdbserver.service.request.EnergyUsageByDateRangeRequest;
import core.mcpdbserver.service.request.EnergyUsageRequest;
import core.mcpdbserver.service.response.EnergyUsageInfo;
import core.mcpdbserver.service.response.EnergyUsageMetaInfo;
import core.mcpdbserver.service.response.EnergyUsageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnergyDataSetService {

    private final EnergyDataSetRepository energyDataSetRepository;

    @Tool
    public EnergyUsageResponse getEnergyUsagesByDateRange(EnergyUsageByDateRangeRequest request) {
        List<EnergyUsageInfo> energyUsageInfos =
                energyDataSetRepository.findByMeasurementDateTimeBetweenAndBuildingId(
                        request.startDateTime(),
                        request.endDateTime(),
                        request.buildingId()
                );
        EnergyUsageMetaInfo meta = new EnergyUsageMetaInfo(request.buildingId());
        return new EnergyUsageResponse(meta, energyUsageInfos);
    }

    @Tool
    public EnergyUsageInfo getEnergyUsage(EnergyUsageRequest request) {

        return energyDataSetRepository.findByMeasurementDateTimeAndBuildingId(
                request.measurementDateTime(), request.buildingId()
                ).orElseThrow();
    }
}
