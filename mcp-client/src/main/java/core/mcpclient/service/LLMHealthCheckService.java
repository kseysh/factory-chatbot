package core.mcpclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LLMHealthCheckService {

    private final ChatClient chatClient;

    public boolean isLLMConnected() {
        log.info("current Model = {}", chatClient);
        try {
            log.info("LLM Connected!\n " + "LLM answer = {}",
                    chatClient.prompt().user("Hello, respond with just 'OK'").call().content());
            return true;
        } catch (Exception e) {
            log.error("exception", e);
            return false;
        }
    }
}
