package core.mcpdbserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EnergyDataSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull
    @Column(name = "building_id")
    Long buildingId;

    @NotNull
    @Column(name = "measurement_date_time")
    LocalDateTime measurementDateTime;

    @NotNull
    @Column(name = "temperature")
    Float temperature;

    @NotNull
    @Column(name = "rainfall")
    Float rainfall;

    @NotNull
    @Column(name = "wind_speed")
    Float windSpeed;

    @NotNull
    @Column(name = "humidity_percentage")
    Float humidityPercentage;

    @NotNull
    @Column(name = "sunlight_hour")
    Float sunlightHour;

    @NotNull
    @Column(name = "solar")
    Float solar;

    @NotNull
    @Column(name = "energy_usage")
    Float energyUsage;
}
