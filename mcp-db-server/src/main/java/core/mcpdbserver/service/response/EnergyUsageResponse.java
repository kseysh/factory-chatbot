package core.mcpdbserver.service.response;

import java.util.List;

public record EnergyUsageResponse(EnergyUsageMetaInfo meta, List<EnergyUsageInfo> data) {

}
