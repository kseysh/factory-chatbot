package core.mcpclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LLMHealthCheckService {

    private final ChatModel chatModel;

    public boolean isLLMConnected() {
        log.info("current Model = {}", chatModel);
        log.info("current Model = {}", chatModel.getDefaultOptions().getModel());
        log.info("current Model = {}", chatModel.getDefaultOptions().getMaxTokens());
        try {
            log.info("LLM Connected!\n " + "LLM answer = {}",
                    chatModel.call(new Prompt("Hello, respond with just 'OK'")).getResult().getOutput().getText());
            return true;
        } catch (Exception e) {
            log.error("exception", e);
            return false;
        }
    }
}
