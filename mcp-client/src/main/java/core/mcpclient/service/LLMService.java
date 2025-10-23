package core.mcpclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LLMService {

    private final ChatClient chatClient;

    public String chat(String question) {
        try {
            return chatClient.prompt(question).call().content();
        } catch (Exception e) {
            return e.toString();
        }
    }

}
