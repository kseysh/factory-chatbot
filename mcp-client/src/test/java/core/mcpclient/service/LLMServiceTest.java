package core.mcpclient.service;

import core.mcpclient.service.dto.NewChatRoomInfo;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.ChatClient.StreamResponseSpec;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import static core.mcpclient.service.LLMService.TITLE_SEPARATOR;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LLMServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private ChatClientRequestSpec callRequestSpec;

    @Mock
    private CallResponseSpec callResponseSpec;

    @Mock
    private StreamResponseSpec streamResponseSpec;

    @InjectMocks
    private LLMService llmService;

    private static final Long TEST_ROOM_ID = 1L;
    private static final String TEST_CONVERSATION_ID = "1";
    private static final String TEST_QUESTION = "유저 질문";
    private static final String TEST_ANSWER = "LLM 답변";
    private static final String TEST_ROOM_NAME = "방 이름";
    private static final String TEST_NEW_CHAT_ANSWER = "New Chat LLM 답변";

    // ==================== chat Tests ====================
    @Test
    @DisplayName("chat 메서드는 질문을 입력받아 답변을 반환한다")
    void chat_shouldReturnAnswerForQuestion() {
        // given
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(TEST_QUESTION));
        given(chatMemory.get(TEST_CONVERSATION_ID)).willReturn(messages);
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.system(anyString())).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn(TEST_ANSWER);

        // when
        String result = llmService.chat(TEST_ROOM_ID, TEST_QUESTION);

        // then
        assertThat(result).isEqualTo(TEST_ANSWER);
    }

//    @ParameterizedTest
//    @DisplayName("chat 메서드는 LLM 응답이 null 또는 Empty 또는 공백이면 예외를 발생시킨다")
//    @NullAndEmptySource
//    @ValueSource(strings = {" "})
//    void chat_shouldThrowExceptionWhenResponseIsNull(String llmAnswer) {
//        // given
//        ArrayList<Message> messages = new ArrayList<>();
//        messages.add(new UserMessage(TEST_QUESTION));
//        given(chatMemory.get(TEST_CONVERSATION_ID)).willReturn(messages);
//        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
//        given(callRequestSpec.system(anyString())).willReturn(callRequestSpec);
//        given(callRequestSpec.call()).willReturn(callResponseSpec);
//        given(callResponseSpec.content()).willReturn(llmAnswer);
//
//        // when & then
//        assertThatThrownBy(() -> llmService.chat(TEST_ROOM_ID, TEST_QUESTION))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("LLM response content is null or empty");
//    }

    // ==================== startNewChat Tests ====================

    @Test
    @DisplayName("startNewChat 메서드는 system prompt를 호출한다")
    void startNewChat_shouldCallChatClientWithPrompt() {
        // given
        String llmResponse = String.format("%s"+TITLE_SEPARATOR+"%s", TEST_ROOM_NAME, TEST_NEW_CHAT_ANSWER);
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(TEST_QUESTION));
        given(chatMemory.get(TEST_CONVERSATION_ID)).willReturn(messages);
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.system(anyString())).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn(llmResponse);

        // when
        llmService.startNewChat(TEST_ROOM_ID, TEST_QUESTION);

        // then
        verify(chatClient).prompt(any(Prompt.class));
    }

//    @ParameterizedTest
//    @DisplayName("startNewChat 메서드는 LLM 응답이 null 또는 Empty 또는 공백이면 예외를 발생시킨다")
//    @NullAndEmptySource
//    @ValueSource(strings = {" "})
//    void startNewChat_shouldThrowExceptionWhenResponseIsNull(String llmAnswer) {
//        // given
//        ArrayList<Message> messages = new ArrayList<>();
//        messages.add(new UserMessage(TEST_QUESTION));
//        given(chatMemory.get(TEST_CONVERSATION_ID)).willReturn(messages);
//        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
//        given(callRequestSpec.system(anyString())).willReturn(callRequestSpec);
//        given(callRequestSpec.call()).willReturn(callResponseSpec);
//        given(callResponseSpec.content()).willReturn(llmAnswer);
//
//        // when & then
//        assertThatThrownBy(() -> llmService.startNewChat(TEST_ROOM_ID, TEST_QUESTION))
//                .isInstanceOf(IllegalArgumentException.class);
//    }

    // ==================== chatStream Tests ====================
    @Test
    @DisplayName("chatStream 메서드는 질문을 입력받아 스트리밍 응답을 반환한다")
    void chatStream_shouldReturnStreamingResponse() {
        // given
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(TEST_QUESTION));
        given(chatMemory.get(TEST_CONVERSATION_ID)).willReturn(messages);
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.system(anyString())).willReturn(callRequestSpec);
        given(callRequestSpec.stream()).willReturn(streamResponseSpec);
        given(streamResponseSpec.content()).willReturn(Flux.just(TEST_ANSWER));

        // when
        Flux<String> result = llmService.chatStream(TEST_ROOM_ID, TEST_QUESTION);

        // then
        assertThat(result).isNotNull();
        result.subscribe(chunk -> assertThat(chunk).isEqualTo(TEST_ANSWER));
    }

    // ==================== startNewChatStream Tests ====================
    @Test
    @DisplayName("startNewChatStream 메서드는 새로운 채팅방 생성 스트리밍 응답을 반환한다")
    void startNewChatStream_shouldReturnStreamingResponse() {
        // given
        String llmResponse = String.format("%s"+TITLE_SEPARATOR+"%s", TEST_ROOM_NAME, TEST_NEW_CHAT_ANSWER);
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(TEST_QUESTION));
        given(chatMemory.get(TEST_CONVERSATION_ID)).willReturn(messages);
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.system(anyString())).willReturn(callRequestSpec);
        given(callRequestSpec.stream()).willReturn(streamResponseSpec);
        given(streamResponseSpec.content()).willReturn(Flux.just(llmResponse));

        // when
        Flux<NewChatRoomInfo> result = llmService.startNewChatStream(TEST_ROOM_ID, TEST_QUESTION);

        // then
        assertThat(result).isNotNull();
    }
}