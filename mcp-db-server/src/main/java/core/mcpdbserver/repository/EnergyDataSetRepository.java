package core.mcpdbserver.repository;

import core.mcpdbserver.entity.EnergyDataSet;
import core.mcpdbserver.service.response.EnergyUsageInfo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnergyDataSetRepository extends JpaRepository<EnergyDataSet, Long> {

    List<EnergyUsageInfo> findByMeasurementDateTimeBetweenAndBuildingId(LocalDateTime measurementDateTimeAfter, LocalDateTime measurementDateTimeAfter1, Long buildingId);
    Optional<EnergyUsageInfo> findByMeasurementDateTimeAndBuildingId(LocalDateTime measurementDateTime, Long buildingId);
}
