package core.chat.service;

import core.chat.controller.response.ChatHistoryResponse;
import core.chat.entity.ChatHistory;
import core.chat.repository.ChatHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static core.chat.fixture.ChatFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatHistoryServiceTest {

    @Mock
    private ChatHistoryRepository chatHistoryRepository;

    @InjectMocks
    private ChatHistoryService chatHistoryService;

    private ChatHistory userChatHistory;
    private ChatHistory llmChatHistory;

    @BeforeEach
    void setUp() {
        userChatHistory = ChatHistory.createUserChatHistory(TEST_ROOM_ID, QUESTION);
        llmChatHistory = ChatHistory.createLLMChatHistory(TEST_ROOM_ID, ANSWER);
    }

    @Test
    @DisplayName("사용자와 LLM의 채팅 이력을 함께 저장할 수 있다")
    void saveChatHistory() {
        // when
        chatHistoryService.saveChatHistory(userChatHistory, llmChatHistory);

        // then
        verify(chatHistoryRepository, times(2)).insertChatHistoryWithoutSelect(any(ChatHistory.class));
        verify(chatHistoryRepository).insertChatHistoryWithoutSelect(userChatHistory);
        verify(chatHistoryRepository).insertChatHistoryWithoutSelect(llmChatHistory);
    }

    @Test
    @DisplayName("특정 방의 최신 채팅 이력을 조회할 수 있다")
    void getChatHistoriesLatest() {
        // given
        given(chatHistoryRepository.findAllByRoomIdLatest(TEST_ROOM_ID, 10))
                .willReturn(List.of(userChatHistory, llmChatHistory));

        // when
        List<ChatHistoryResponse> result = chatHistoryService.getChatHistoriesLatest(TEST_ROOM_ID, 10);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting(
                        ChatHistoryResponse::getChatId,
                        ChatHistoryResponse::getContent,
                        ChatHistoryResponse::getIsChatbot
                )
                .containsExactly(
                        tuple(userChatHistory.getId(), QUESTION, false),
                        tuple(llmChatHistory.getId(), ANSWER, true)
                );

        verify(chatHistoryRepository).findAllByRoomIdLatest(TEST_ROOM_ID, 10);
    }

    @Test
    @DisplayName("최신 채팅 이력 조회 시 사용자 메시지는 isChatbot이 false이다")
    void getChatHistoriesLatest_UserMessageIsChatbot() {
        // given
        given(chatHistoryRepository.findAllByRoomIdLatest(TEST_ROOM_ID, 10))
                .willReturn(List.of(userChatHistory));

        // when
        List<ChatHistoryResponse> result = chatHistoryService.getChatHistoriesLatest(TEST_ROOM_ID, 10);

        // then
        assertThat(result)
                .hasSize(1)
                .extracting(ChatHistoryResponse::getIsChatbot)
                .containsExactly(false);
    }

    @Test
    @DisplayName("최신 채팅 이력 조회 시 LLM 메시지는 isChatbot이 true이다")
    void getChatHistoriesLatest_LLMMessageIsChatbot() {
        // given
        given(chatHistoryRepository.findAllByRoomIdLatest(TEST_ROOM_ID, 10)).willReturn(List.of(llmChatHistory));

        // when
        List<ChatHistoryResponse> result = chatHistoryService.getChatHistoriesLatest(TEST_ROOM_ID, 10);

        // then
        assertThat(result)
                .hasSize(1)
                .extracting(ChatHistoryResponse::getIsChatbot)
                .containsExactly(true);
    }

    @Test
    @DisplayName("limit 개수만큼 최신 채팅 이력을 조회할 수 있다")
    void getChatHistoriesLatest_WithLimit() {
        // given
        given(chatHistoryRepository.findAllByRoomIdLatest(TEST_ROOM_ID, 1)).willReturn(List.of(userChatHistory));

        // when
        List<ChatHistoryResponse> result = chatHistoryService.getChatHistoriesLatest(TEST_ROOM_ID, 1);

        // then
        assertThat(result).hasSize(1);
        verify(chatHistoryRepository).findAllByRoomIdLatest(TEST_ROOM_ID, 1);
    }

    @Test
    @DisplayName("존재하지 않는 방의 채팅 이력을 조회하면 빈 리스트를 반환한다")
    void getChatHistoriesLatest_NonExistentRoom() {
        // given
        Long nonExistentRoomId = 999L;
        given(chatHistoryRepository.findAllByRoomIdLatest(nonExistentRoomId, 10)).willReturn(List.of());

        // when
        List<ChatHistoryResponse> result = chatHistoryService.getChatHistoriesLatest(nonExistentRoomId, 10);

        // then
        assertThat(result).isEmpty();
        verify(chatHistoryRepository).findAllByRoomIdLatest(nonExistentRoomId, 10);
    }

    @Test
    @DisplayName("특정 채팅 이후의 이력을 조회할 수 있다")
    void getChatHistoriesAfter() {
        // given
        given(chatHistoryRepository.findAllByRoomIdAfterChatId(TEST_ROOM_ID, userChatHistory.getId(), 10))
                .willReturn(List.of(llmChatHistory));

        // when
        List<ChatHistoryResponse> result = chatHistoryService.getChatHistoriesAfter(
                TEST_ROOM_ID, userChatHistory.getId(), 10
        );

        // then
        assertThat(result)
                .hasSize(1)
                .extracting(ChatHistoryResponse::getChatId, ChatHistoryResponse::getContent)
                .containsExactly(
                        tuple(llmChatHistory.getId(), ANSWER)
                );

        verify(chatHistoryRepository).findAllByRoomIdAfterChatId(TEST_ROOM_ID, userChatHistory.getId(), 10);
    }

    @Test
    @DisplayName("특정 채팅 이후의 이력은 올바른 형식으로 변환된다")
    void getChatHistoriesAfter_ResponseFormat() {
        // given
        given(chatHistoryRepository.findAllByRoomIdAfterChatId(TEST_ROOM_ID, userChatHistory.getId(), 10))
                .willReturn(List.of(llmChatHistory));

        // when
        List<ChatHistoryResponse> result = chatHistoryService.getChatHistoriesAfter(
                TEST_ROOM_ID, userChatHistory.getId(), 10);

        // then
        assertThat(result)
                .hasSize(1)
                .allSatisfy(response -> {
                    assertThat(response.getChatId()).isNotNull();
                    assertThat(response.getContent()).isNotNull();
                    assertThat(response.getIsChatbot()).isNotNull();
                });
    }

    @Test
    @DisplayName("limit 개수만큼 특정 채팅 이후의 이력을 조회할 수 있다")
    void getChatHistoriesAfter_WithLimit() {
        // given
        given(chatHistoryRepository.findAllByRoomIdAfterChatId(TEST_ROOM_ID, userChatHistory.getId(), 1))
                .willReturn(List.of(llmChatHistory));

        // when
        List<ChatHistoryResponse> result =
                chatHistoryService.getChatHistoriesAfter(TEST_ROOM_ID, userChatHistory.getId(), 1);

        // then
        assertThat(result).hasSize(1);
        verify(chatHistoryRepository).findAllByRoomIdAfterChatId(TEST_ROOM_ID, userChatHistory.getId(), 1);
    }

    @Test
    @DisplayName("특정 채팅 이후에 조회할 이력이 없으면 빈 리스트를 반환한다")
    void getChatHistoriesAfter_EmptyResult() {
        // given
        Long lastChatId = 999L;
        given(chatHistoryRepository.findAllByRoomIdAfterChatId(TEST_ROOM_ID, lastChatId, 10))
                .willReturn(List.of());

        // when
        List<ChatHistoryResponse> result =
                chatHistoryService.getChatHistoriesAfter(TEST_ROOM_ID, lastChatId, 10);

        // then
        assertThat(result).isEmpty();
        verify(chatHistoryRepository).findAllByRoomIdAfterChatId(TEST_ROOM_ID, lastChatId, 10);
    }

    @Test
    @DisplayName("페이지네이션으로 채팅 이력을 조회할 수 있다")
    void getChatHistoriesAfter_WithPagination() {
        // given
        given(chatHistoryRepository.findAllByRoomIdAfterChatId(TEST_ROOM_ID, userChatHistory.getId(), 5))
                .willReturn(List.of(llmChatHistory));

        // when
        List<ChatHistoryResponse> result =
                chatHistoryService.getChatHistoriesAfter(TEST_ROOM_ID, userChatHistory.getId(), 5);

        // then
        assertThat(result).hasSize(1);
        verify(chatHistoryRepository).findAllByRoomIdAfterChatId(TEST_ROOM_ID, userChatHistory.getId(), 5);
    }

    @Test
    @DisplayName("특정 방의 채팅 이력을 삭제할 수 있다")
    void deleteChatHistoryByRoomId() {
        // when
        chatHistoryService.deleteChatHistoryByRoomId(TEST_ROOM_ID);

        // then
        verify(chatHistoryRepository).deleteChatHistoryByRoomId(TEST_ROOM_ID);
    }

    @Test
    @DisplayName("빈 채팅 이력 리스트를 조회할 수 있다")
    void getChatHistoriesLatest_EmptyList() {
        // given
        given(chatHistoryRepository.findAllByRoomIdLatest(TEST_ROOM_ID, 10))
                .willReturn(List.of());

        // when
        List<ChatHistoryResponse> result = chatHistoryService.getChatHistoriesLatest(TEST_ROOM_ID, 10);

        // then
        assertThat(result).isEmpty();
    }
}