package core.mcpclient.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LLMHealthCheckServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private CallResponseSpec callResponseSpec;

    @Mock
    private ChatClientRequestSpec callRequestSpec;

    @InjectMocks
    private LLMHealthCheckService llmHealthCheckService;

    @Test
    @DisplayName("isLLMConnected는 LLM이 연결되었을 때 true를 반환한다")
    void isLLMConnected_shouldReturnTrueWhenLlmIsConnected() {
        // given
        given(chatClient.prompt()).willReturn(callRequestSpec);
        given(callRequestSpec.user(anyString())).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn("OK");

        // when
        boolean result = llmHealthCheckService.isLLMConnected();

        // then
        assertTrue(result);
        verify(chatClient).prompt();
    }

    @Test
    @DisplayName("isLLMConnected는 LLM이 연결되지 않았을 때 false를 반환한다")
    void isLLMConnected_shouldReturnFalseWhenLlmIsNotConnected() {
        // given
        given(chatClient.prompt()).willThrow(new RuntimeException("Connection failed"));

        // when
        boolean result = llmHealthCheckService.isLLMConnected();

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("isLLMConnected는 LLM 응답이 예외를 발생시켰을 때 false를 반환한다")
    void isLLMConnected_shouldReturnFalseWhenLlmThrowsException() {
        // given
        given(chatClient.prompt()).willThrow(new IllegalStateException());

        // when
        boolean result = llmHealthCheckService.isLLMConnected();

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("isLLMConnected는 다양한 런타임 예외에서 false를 반환한다")
    void isLLMConnected_shouldReturnFalseWhenAnyExceptionOccurs() {
        // given
        given(chatClient.prompt()).willThrow(new NullPointerException("Null pointer"));

        // when
        boolean result = llmHealthCheckService.isLLMConnected();

        // then
        assertFalse(result);
    }
}