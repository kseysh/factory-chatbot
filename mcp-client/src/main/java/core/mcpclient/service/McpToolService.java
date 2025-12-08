package core.mcpclient.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class McpToolService {

    private final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;

    public void refreshTools() {
        syncMcpToolCallbackProvider.invalidateCache();
    }
}