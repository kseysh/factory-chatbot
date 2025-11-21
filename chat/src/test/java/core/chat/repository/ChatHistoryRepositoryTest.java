package core.chat.repository;

import core.chat.entity.ChatHistory;
import core.chat.entity.MessageType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static core.chat.fixture.ChatFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(ChatHistoryRepositoryImpl.class)
class ChatHistoryRepositoryTest {

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private ChatHistoryJpaRepository chatHistoryJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        chatHistoryJpaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 데이터 정리
        chatHistoryJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("채팅 이력을 저장할 수 있다")
    void insertChatHistoryWithoutSelect() {
        // given
        ChatHistory chatHistory = ChatHistory.createUserChatHistory(TEST_ROOM_ID, "안녕하세요");

        // when
        chatHistoryRepository.insertChatHistoryWithoutSelect(chatHistory);

        // then
        ChatHistory found = chatHistoryJpaRepository.findById(chatHistory.getId()).orElse(null);
        assertThat(found)
                .isNotNull()
                .extracting(ChatHistory::getRoomId, ChatHistory::getContent, ChatHistory::getType)
                .containsExactly(TEST_ROOM_ID, "안녕하세요", MessageType.USER);
    }

    @Test
    @DisplayName("USER 타입의 채팅 이력을 저장할 수 있다")
    void insertUserChatHistory() {
        // given
        ChatHistory userChat = ChatHistory.createUserChatHistory(TEST_ROOM_ID, "사용자 메시지");

        // when
        chatHistoryRepository.insertChatHistoryWithoutSelect(userChat);

        // then
        ChatHistory found = chatHistoryJpaRepository.findById(userChat.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getType()).isEqualTo(MessageType.USER);
        assertThat(found.getContent()).isEqualTo("사용자 메시지");
    }

    @Test
    @DisplayName("LLM 타입의 채팅 이력을 저장할 수 있다")
    void insertLLMChatHistory() {
        // given
        ChatHistory llmChat = ChatHistory.createLLMChatHistory(TEST_ROOM_ID, "LLM 응답");

        // when
        chatHistoryRepository.insertChatHistoryWithoutSelect(llmChat);
        entityManager.flush();

        // then
        ChatHistory found = chatHistoryJpaRepository.findById(llmChat.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getType()).isEqualTo(MessageType.LLM);
        assertThat(found.getContent()).isEqualTo("LLM 응답");
    }

    @Test
    @DisplayName("특정 roomId의 모든 채팅 이력을 삭제할 수 있다")
    void deleteChatHistoryByRoomId() {
        // given
        ChatHistory chat1 = ChatHistory.createUserChatHistory(TEST_ROOM_ID, "메시지1");
        ChatHistory chat2 = ChatHistory.createLLMChatHistory(TEST_ROOM_ID, "응답1");
        ChatHistory chat3 = ChatHistory.createUserChatHistory(OTHER_ROOM_ID, "다른방 메시지");

        chatHistoryJpaRepository.saveAll(List.of(chat1, chat2, chat3));
        entityManager.flush();

        // when
        chatHistoryRepository.deleteChatHistoryByRoomId(TEST_ROOM_ID);
        entityManager.flush();

        // then
        List<ChatHistory> remaining = chatHistoryJpaRepository.findAll();
        assertThat(remaining)
                .hasSize(1)
                .extracting(ChatHistory::getRoomId)
                .containsOnly(OTHER_ROOM_ID);
    }

    @Test
    @DisplayName("특정 roomId의 최신 채팅 이력을 limit 개수로 조회할 수 있다")
    void findAllByRoomIdLatest() {
        // given
        List<ChatHistory> histories = createChatHistories(TEST_ROOM_ID, 5);
        chatHistoryJpaRepository.saveAll(histories);
        entityManager.flush();

        // when
        List<ChatHistory> result = chatHistoryRepository.findAllByRoomIdLatest(TEST_ROOM_ID, 3);

        // then
        assertThat(result)
                .hasSize(3)
                .extracting(ChatHistory::getRoomId)
                .containsOnly(TEST_ROOM_ID);

        // ID가 내림차순으로 정렬되어 있는지 확인 (최신순)
        assertThat(result).extracting(ChatHistory::getId)
                .isSortedAccordingTo((a, b) -> Long.compare(b, a));
    }

    @Test
    @DisplayName("limit이 전체 개수보다 크면 전체 개수를 반환한다")
    void findAllByRoomIdLatest_WithLargeLimit() {
        // given
        List<ChatHistory> histories = createChatHistories(TEST_ROOM_ID, 3);
        chatHistoryJpaRepository.saveAll(histories);
        entityManager.flush();

        // when
        List<ChatHistory> result = chatHistoryRepository.findAllByRoomIdLatest(TEST_ROOM_ID, 10);

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("존재하지 않는 roomId로 최신 채팅을 조회하면 빈 리스트를 반환한다")
    void findAllByRoomIdLatest_WithNonExistentRoomId() {
        // given
        List<ChatHistory> histories = createChatHistories(TEST_ROOM_ID, 3);
        chatHistoryJpaRepository.saveAll(histories);
        entityManager.flush();

        // when
        List<ChatHistory> result = chatHistoryRepository.findAllByRoomIdLatest(999L, 10);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("특정 채팅 이후의 이력을 limit 개수로 조회할 수 있다")
    void findAllByRoomIdAfterChatId() {
        // given
        List<ChatHistory> histories = createChatHistories(TEST_ROOM_ID, 5);
        chatHistoryJpaRepository.saveAll(histories);
        entityManager.flush();

        Long targetChatId = histories.get(2).getId(); // 중간값

        // when
        List<ChatHistory> result = chatHistoryRepository.findAllByRoomIdAfterChatId(
                TEST_ROOM_ID,
                targetChatId,
                3
        );

        // then
        assertThat(result)
                .hasSizeLessThanOrEqualTo(3)
                .extracting(ChatHistory::getRoomId)
                .containsOnly(TEST_ROOM_ID);

        // 모든 결과가 targetChatId보다 작은 ID를 가져야 함
        assertThat(result)
                .extracting(ChatHistory::getId)
                .allMatch(id -> id < targetChatId);
    }

    @Test
    @DisplayName("특정 채팅 이후의 이력은 ID 내림차순으로 정렬된다")
    void findAllByRoomIdAfterChatId_OrderedByIdDesc() {
        // given
        List<ChatHistory> histories = createChatHistories(TEST_ROOM_ID, 5);
        chatHistoryJpaRepository.saveAll(histories);
        entityManager.flush();

        Long targetChatId = histories.get(3).getId();

        // when
        List<ChatHistory> result = chatHistoryRepository.findAllByRoomIdAfterChatId(
                TEST_ROOM_ID,
                targetChatId,
                10
        );

        // then
        assertThat(result).extracting(ChatHistory::getId)
                .isSortedAccordingTo((a, b) -> Long.compare(b, a));
    }

    @Test
    @DisplayName("다른 roomId의 데이터는 포함하지 않는다")
    void findAllByRoomIdAfterChatId_ExcludeOtherRoomId() {
        // given
        List<ChatHistory> room1Histories = createChatHistories(TEST_ROOM_ID, 3);
        List<ChatHistory> room2Histories = createChatHistories(OTHER_ROOM_ID, 3);
        chatHistoryJpaRepository.saveAll(room1Histories);
        chatHistoryJpaRepository.saveAll(room2Histories);

        Long targetChatId = room1Histories.get(2).getId();

        // when
        List<ChatHistory> result = chatHistoryRepository.findAllByRoomIdAfterChatId(
                TEST_ROOM_ID,
                targetChatId,
                10
        );

        // then
        assertThat(result)
                .extracting(ChatHistory::getRoomId)
                .containsOnly(TEST_ROOM_ID);
    }

    @Test
    @DisplayName("존재하지 않는 chatId로 조회하면 반환하지 않는다.")
    void findAllByRoomIdAfterChatId_WithNonExistentChatId() {
        // given
        List<ChatHistory> histories = createChatHistories(TEST_ROOM_ID, 3);
        chatHistoryJpaRepository.saveAll(histories);

        Long nonExistentChatId = 999999999L;

        // when
        List<ChatHistory> result = chatHistoryRepository.findAllByRoomIdAfterChatId(
                TEST_ROOM_ID,
                nonExistentChatId,
                10
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 방의 데이터가 있을 때 특정 방의 데이터만 삭제된다")
    void deleteChatHistoryByRoomId_OnlyDeletesTargetRoom() {
        // given
        List<ChatHistory> room1Histories = createChatHistories(TEST_ROOM_ID, 3);
        List<ChatHistory> room2Histories = createChatHistories(OTHER_ROOM_ID, 3);
        chatHistoryJpaRepository.saveAll(room1Histories);
        chatHistoryJpaRepository.saveAll(room2Histories);
        entityManager.flush();

        // when
        chatHistoryRepository.deleteChatHistoryByRoomId(TEST_ROOM_ID);
        entityManager.flush();

        // then
        List<ChatHistory> remaining = chatHistoryJpaRepository.findAll();
        assertThat(remaining)
                .hasSize(3)
                .extracting(ChatHistory::getRoomId)
                .containsOnly(OTHER_ROOM_ID);
    }

    private List<ChatHistory> createChatHistories(Long roomId, int count) {
        List<ChatHistory> histories = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                histories.add(ChatHistory.createUserChatHistory(roomId, "사용자 메시지 " + i));
            } else {
                histories.add(ChatHistory.createLLMChatHistory(roomId, "LLM 응답 " + i));
            }
        }
        return histories;
    }
}