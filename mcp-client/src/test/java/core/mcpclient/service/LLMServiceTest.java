package core.mcpclient.service;

import core.mcpclient.service.dto.NewChatRoomInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

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
        given(chatMemory.get(TEST_CONVERSATION_ID)).willReturn(List.of(new UserMessage(TEST_QUESTION)));
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn(TEST_ANSWER);

        // when
        String result = llmService.chat(TEST_ROOM_ID, TEST_QUESTION);

        // then
        assertThat(result).isEqualTo(TEST_ANSWER);
        verify(chatMemory).add(TEST_CONVERSATION_ID, new UserMessage(TEST_QUESTION));
        verify(chatMemory).add(TEST_CONVERSATION_ID, new AssistantMessage(TEST_ANSWER));
    }

    @Test
    @DisplayName("chat 메서드는 대화 히스토리를 chatMemory에 저장한다")
    void chat_shouldSaveConversationHistory() {
        // given
        given(chatMemory.get(TEST_CONVERSATION_ID))
                .willReturn(List.of(new UserMessage(TEST_QUESTION)));
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn(TEST_ANSWER);

        // when
        llmService.chat(TEST_ROOM_ID, TEST_QUESTION);

        // then
        verify(chatMemory).add(TEST_CONVERSATION_ID, new UserMessage(TEST_QUESTION));
        verify(chatMemory).add(TEST_CONVERSATION_ID, new AssistantMessage(TEST_ANSWER));
    }

    @Test
    @DisplayName("chat 메서드는 LLM 응답이 null이면 예외를 발생시킨다")
    void chat_shouldThrowExceptionWhenResponseIsNull() {
        // given
        given(chatMemory.get(TEST_CONVERSATION_ID))
                .willReturn(List.of(new UserMessage(TEST_QUESTION)));
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> llmService.chat(TEST_ROOM_ID, TEST_QUESTION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("LLM response content is null or empty");
    }

    @Test
    @DisplayName("chat 메서드는 LLM 응답이 빈 문자열이면 예외를 발생시킨다")
    void chat_shouldThrowExceptionWhenResponseIsEmpty() {
        // given
        given(chatMemory.get(TEST_CONVERSATION_ID))
                .willReturn(List.of(new UserMessage(TEST_QUESTION)));
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn("");

        // when & then
        assertThatThrownBy(() -> llmService.chat(TEST_ROOM_ID, TEST_QUESTION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("LLM response content is null or empty");
    }

    @Test
    @DisplayName("chat 메서드는 LLM 응답이 공백만 있으면 예외를 발생시킨다")
    void chat_shouldThrowExceptionWhenResponseIsBlank() {
        // given
        given(chatMemory.get(TEST_CONVERSATION_ID))
                .willReturn(List.of(new UserMessage(TEST_QUESTION)));
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn("   ");

        // when & then
        assertThatThrownBy(() -> llmService.chat(TEST_ROOM_ID, TEST_QUESTION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("LLM response content is null or empty");
    }

    // ==================== startNewChat Tests ====================
    @Test
    @DisplayName("startNewChat 메서드는 새로운 채팅방을 시작하고 NewChatRoomInfo를 반환한다")
    void startNewChat_shouldReturnNewChatRoomInfo() {
        // given
        String jsonResponse = String.format("{\"roomName\":\"%s\",\"answer\":\"%s\"}", TEST_ROOM_NAME, TEST_NEW_CHAT_ANSWER);
        NewChatRoomInfo expectedInfo = new NewChatRoomInfo(TEST_ROOM_NAME, TEST_NEW_CHAT_ANSWER);
        given(chatMemory.get(TEST_CONVERSATION_ID))
                .willReturn(List.of(new UserMessage(TEST_QUESTION)));
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.system(anyString())).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn(jsonResponse);
        given(callResponseSpec.entity(NewChatRoomInfo.class)).willReturn(expectedInfo);

        // when
        NewChatRoomInfo result = llmService.startNewChat(TEST_ROOM_ID, TEST_QUESTION);

        // then
        assertThat(result).isEqualTo(expectedInfo);
        assertThat(result.roomName()).isEqualTo(TEST_ROOM_NAME);
        assertThat(result.answer()).isEqualTo(TEST_NEW_CHAT_ANSWER);
    }

    @Test
    @DisplayName("startNewChat 메서드는 대화 히스토리를 저장한다")
    void startNewChat_shouldSaveConversationHistory() {
        // given
        String jsonResponse = String.format("{\"roomName\":\"%s\",\"answer\":\"%s\"}", TEST_ROOM_NAME, TEST_NEW_CHAT_ANSWER);
        NewChatRoomInfo expectedInfo = new NewChatRoomInfo(TEST_ROOM_NAME, TEST_NEW_CHAT_ANSWER);
        given(chatMemory.get(TEST_CONVERSATION_ID))
                .willReturn(List.of(new UserMessage(TEST_QUESTION)));
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.system(anyString())).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn(jsonResponse);
        given(callResponseSpec.entity(NewChatRoomInfo.class)).willReturn(expectedInfo);

        // when
        llmService.startNewChat(TEST_ROOM_ID, TEST_QUESTION);

        // then
        verify(chatMemory).add(TEST_CONVERSATION_ID, new UserMessage(TEST_QUESTION));
        verify(chatMemory).add(TEST_CONVERSATION_ID, new AssistantMessage(jsonResponse));
    }

    @Test
    @DisplayName("startNewChat 메서드는 system prompt를 호출한다")
    void startNewChat_shouldCallChatClientWithPrompt() {
        // given
        String jsonResponse = String.format("{\"roomName\":\"%s\",\"answer\":\"%s\"}", TEST_ROOM_NAME, TEST_NEW_CHAT_ANSWER);
        NewChatRoomInfo expectedInfo = new NewChatRoomInfo(TEST_ROOM_NAME, TEST_NEW_CHAT_ANSWER);
        given(chatMemory.get(TEST_CONVERSATION_ID))
                .willReturn(List.of(new UserMessage(TEST_QUESTION)));
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.system(anyString())).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn(jsonResponse);
        given(callResponseSpec.entity(NewChatRoomInfo.class)).willReturn(expectedInfo);

        // when
        llmService.startNewChat(TEST_ROOM_ID, TEST_QUESTION);

        // then
        verify(chatClient).prompt(any(Prompt.class));
    }

    @Test
    @DisplayName("startNewChat 메서드는 LLM 응답이 null이면 예외를 발생시킨다")
    void startNewChat_shouldThrowExceptionWhenResponseIsNull() {
        // given
        given(chatMemory.get(TEST_CONVERSATION_ID))
                .willReturn(List.of(new UserMessage(TEST_QUESTION)));
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.system(anyString())).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> llmService.startNewChat(TEST_ROOM_ID, TEST_QUESTION))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("startNewChat 메서드는 LLM 응답이 빈 문자열이면 예외를 발생시킨다")
    void startNewChat_shouldThrowExceptionWhenResponseIsEmpty() {
        // given
        given(chatMemory.get(TEST_CONVERSATION_ID))
                .willReturn(List.of(new UserMessage(TEST_QUESTION)));
        given(chatClient.prompt(any(Prompt.class))).willReturn(callRequestSpec);
        given(callRequestSpec.system(anyString())).willReturn(callRequestSpec);
        given(callRequestSpec.call()).willReturn(callResponseSpec);
        given(callResponseSpec.content()).willReturn("");

        // when & then
        assertThatThrownBy(() -> llmService.startNewChat(TEST_ROOM_ID, TEST_QUESTION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("LLM response content is null or empty");
    }
}