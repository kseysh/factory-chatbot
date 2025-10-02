package core.mcpclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LLMHealthCheckService {

    private final ChatModel chatModel;

    @Tool(description = "현재 LLM 연결 상태를 확인합니다")
    public String checkLLMConnection() {
        log.info("current Model = {}", chatModel.getDefaultOptions().getModel());

        ChatOptions opts = chatModel.getDefaultOptions();
        String configuredModel = (opts != null && opts.getModel() != null) ? opts.getModel() : "(미설정)";
        log.info("configuredModel: {}", configuredModel);

        try {
            String response = chatModel.call(new Prompt("Hello, respond with just 'OK'"))
                    .getResult().getOutput().toString();

            return "✅ LLM 연결 성공!\n" +
                    "테스트 응답: " + response.trim();
        } catch (Exception e) {
            return "❌ LLM 연결 실패\n" +
                    "오류: " + e + "\n" +
                    "해결방법: LLM 서버가 실행 중인지, 네트워크 연결이 가능한지 확인하세요.";
        }
    }
}
