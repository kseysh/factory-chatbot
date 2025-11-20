package core.chat.repository;

import core.chat.entity.ChatRoom;
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
import java.util.Optional;

import static core.chat.fixture.ChatFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(ChatRoomRepositoryImpl.class)
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomJpaRepository chatRoomJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        chatRoomJpaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 데이터 정리
        chatRoomJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("채팅 방을 저장할 수 있다")
    void insertChatRoomWithoutSelect() {
        // given
        ChatRoom chatRoom = ChatRoom.createChatRoom(TEST_ROOM_ID, TEST_USER_ID, "테스트 방");

        // when
        chatRoomRepository.insertWithoutSelect(chatRoom);
        entityManager.flush();

        // then
        ChatRoom found = chatRoomJpaRepository.findById(TEST_ROOM_ID).orElse(null);
        assertThat(found)
                .isNotNull()
                .extracting(ChatRoom::getId, ChatRoom::getUserId, ChatRoom::getName)
                .containsExactly(TEST_ROOM_ID, TEST_USER_ID, "테스트 방");
    }

    @Test
    @DisplayName("roomId로 채팅 방을 조회할 수 있다")
    void findByRoomId() {
        // given
        ChatRoom chatRoom = ChatRoom.createChatRoom(TEST_ROOM_ID, TEST_USER_ID, "조회 테스트");
        chatRoomJpaRepository.save(chatRoom);
        entityManager.flush();

        // when
        Optional<ChatRoom> result = chatRoomRepository.findByRoomId(TEST_ROOM_ID);

        // then
        assertThat(result)
                .isPresent()
                .get()
                .extracting(ChatRoom::getId, ChatRoom::getUserId, ChatRoom::getName)
                .containsExactly(TEST_ROOM_ID, TEST_USER_ID, "조회 테스트");
    }

    @Test
    @DisplayName("존재하지 않는 roomId로 조회하면 빈 Optional을 반환한다")
    void findByRoomId_WithNonExistentId() {
        // given
        ChatRoom chatRoom = ChatRoom.createChatRoom(TEST_ROOM_ID, TEST_USER_ID, "테스트 방");
        chatRoomJpaRepository.save(chatRoom);
        entityManager.flush();

        // when
        Optional<ChatRoom> result = chatRoomRepository.findByRoomId(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자의 최신 채팅 방을 limit 개수로 조회할 수 있다")
    void findAllByUserIdLatest() {
        // given
        List<ChatRoom> rooms = createChatRooms(TEST_USER_ID, 5);
        chatRoomJpaRepository.saveAll(rooms);
        entityManager.flush();

        // when
        List<ChatRoom> result = chatRoomRepository.findAllByUserIdLatest(TEST_USER_ID, 3);

        // then
        assertThat(result)
                .hasSize(3)
                .extracting(ChatRoom::getUserId)
                .containsOnly(TEST_USER_ID);

        // ID가 내림차순으로 정렬되어 있는지 확인 (최신순)
        assertThat(result).extracting(ChatRoom::getId)
                .isSortedAccordingTo((a, b) -> Long.compare(b, a));
    }

    @Test
    @DisplayName("limit이 전체 개수보다 크면 전체 개수를 반환한다")
    void findAllByUserIdLatest_WithLargeLimit() {
        // given
        List<ChatRoom> rooms = createChatRooms(TEST_USER_ID, 3);
        chatRoomJpaRepository.saveAll(rooms);
        entityManager.flush();

        // when
        List<ChatRoom> result = chatRoomRepository.findAllByUserIdLatest(TEST_USER_ID, 10);

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("존재하지 않는 userId로 최신 채팅방을 조회하면 빈 리스트를 반환한다")
    void findAllByUserIdLatest_WithNonExistentUserId() {
        // given
        List<ChatRoom> rooms = createChatRooms(TEST_USER_ID, 3);
        chatRoomJpaRepository.saveAll(rooms);
        entityManager.flush();

        // when
        List<ChatRoom> result = chatRoomRepository.findAllByUserIdLatest("nonexistent", 10);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("특정 방 이후의 사용자 채팅 방을 limit 개수로 조회할 수 있다")
    void findAllByUserIdAfterRoomId() {
        // given
        List<ChatRoom> rooms = createChatRooms(TEST_USER_ID, 5);
        chatRoomJpaRepository.saveAll(rooms);
        entityManager.flush();

        Long targetRoomId = rooms.get(2).getId(); // 중간값

        // when
        List<ChatRoom> result = chatRoomRepository.findAllByUserIdAfterRoomId(
                TEST_USER_ID,
                targetRoomId,
                3
        );

        // then
        assertThat(result)
                .hasSizeLessThanOrEqualTo(3)
                .extracting(ChatRoom::getUserId)
                .containsOnly(TEST_USER_ID);

        // 모든 결과가 targetRoomId보다 작은 ID를 가져야 함
        assertThat(result)
                .extracting(ChatRoom::getId)
                .allMatch(id -> id < targetRoomId);
    }

    @Test
    @DisplayName("특정 방 이후의 채팅 방은 ID 내림차순으로 정렬된다")
    void findAllByUserIdAfterRoomId_OrderedByIdDesc() {
        // given
        List<ChatRoom> rooms = createChatRooms(TEST_USER_ID, 5);
        chatRoomJpaRepository.saveAll(rooms);
        entityManager.flush();

        Long targetRoomId = rooms.get(3).getId();

        // when
        List<ChatRoom> result = chatRoomRepository.findAllByUserIdAfterRoomId(
                TEST_USER_ID,
                targetRoomId,
                10
        );

        // then
        assertThat(result).extracting(ChatRoom::getId)
                .isSortedAccordingTo((a, b) -> Long.compare(b, a));
    }

    @Test
    @DisplayName("다른 사용자의 방은 포함하지 않는다")
    void findAllByUserIdAfterRoomId_ExcludeOtherUser() {
        // given
        List<ChatRoom> user1Rooms = createChatRooms(TEST_USER_ID, 3);
        List<ChatRoom> user2Rooms = createChatRooms(OTHER_USER_ID, 3);
        chatRoomJpaRepository.saveAll(user1Rooms);
        chatRoomJpaRepository.saveAll(user2Rooms);
        entityManager.flush();

        Long targetRoomId = user1Rooms.get(2).getId();

        // when
        List<ChatRoom> result = chatRoomRepository.findAllByUserIdAfterRoomId(
                TEST_USER_ID,
                targetRoomId,
                10
        );

        // then
        assertThat(result)
                .extracting(ChatRoom::getUserId)
                .containsOnly(TEST_USER_ID);
    }

    @Test
    @DisplayName("존재하지 않는 roomId 이후의 방을 조회하면 빈 리스트를 반환한다")
    void findAllByUserIdAfterRoomId_WithNonExistentRoomId() {
        // given
        List<ChatRoom> rooms = createChatRooms(TEST_USER_ID, 3);
        chatRoomJpaRepository.saveAll(rooms);
        entityManager.flush();

        Long nonExistentRoomId = 999999999L;

        // when
        List<ChatRoom> result = chatRoomRepository.findAllByUserIdAfterRoomId(
                TEST_USER_ID,
                nonExistentRoomId,
                10
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("채팅 방을 ID로 삭제할 수 있다")
    void deleteById() {
        // given
        ChatRoom room1 = ChatRoom.createChatRoom(TEST_ROOM_ID, TEST_USER_ID, "삭제 테스트 1");
        ChatRoom room2 = ChatRoom.createChatRoom(OTHER_ROOM_ID, TEST_USER_ID, "삭제 테스트 2");
        chatRoomJpaRepository.saveAll(List.of(room1, room2));
        entityManager.flush();

        // when
        chatRoomRepository.deleteById(TEST_ROOM_ID);
        entityManager.flush();

        // then
        Optional<ChatRoom> deleted = chatRoomJpaRepository.findById(TEST_ROOM_ID);
        Optional<ChatRoom> remaining = chatRoomJpaRepository.findById(OTHER_ROOM_ID);

        assertThat(deleted).isEmpty();
        assertThat(remaining).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 방을 삭제해도 예외가 발생하지 않는다")
    void deleteById_WithNonExistentId() {
        // given
        ChatRoom room = ChatRoom.createChatRoom(TEST_ROOM_ID, TEST_USER_ID, "테스트");
        chatRoomJpaRepository.save(room);
        entityManager.flush();

        // when & then
        // 예외가 발생하지 않아야 함
        chatRoomRepository.deleteById(999L);
        entityManager.flush();

        assertThat(chatRoomJpaRepository.findById(TEST_ROOM_ID)).isPresent();
    }

    @Test
    @DisplayName("사용자가 채팅 방에 접근 권한이 있는지 확인할 수 있다")
    void existsByRoomIdAndUserId() {
        // given
        ChatRoom room = ChatRoom.createChatRoom(TEST_ROOM_ID, TEST_USER_ID, "권한 테스트");
        chatRoomJpaRepository.save(room);
        entityManager.flush();

        // when
        boolean userHasAccess = chatRoomRepository.existsByRoomIdAndUserId(TEST_ROOM_ID, TEST_USER_ID);
        boolean otherUserHasAccess = chatRoomRepository.existsByRoomIdAndUserId(TEST_ROOM_ID, OTHER_USER_ID);

        // then
        assertThat(userHasAccess).isTrue();
        assertThat(otherUserHasAccess).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 방의 권한은 false를 반환한다")
    void existsByRoomIdAndUserId_WithNonExistentRoom() {
        // when
        boolean exists = chatRoomRepository.existsByRoomIdAndUserId(999L, TEST_USER_ID);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("사용자의 여러 방을 저장하고 최신순으로 조회할 수 있다")
    void multipleRoomsHandling() {
        // given
        List<ChatRoom> rooms = createChatRooms(TEST_USER_ID, 5);
        chatRoomJpaRepository.saveAll(rooms);
        entityManager.flush();

        // when
        List<ChatRoom> latestRooms = chatRoomRepository.findAllByUserIdLatest(TEST_USER_ID, 3);
        List<ChatRoom> allRooms = chatRoomRepository.findAllByUserIdLatest(TEST_USER_ID, 5);

        // then
        assertThat(latestRooms).hasSize(3);
        assertThat(allRooms).hasSize(5);

        // 최신 3개가 전체 5개의 마지막 3개인지 확인
        List<Long> latestIds = latestRooms.stream().map(ChatRoom::getId).toList();
        List<Long> allIds = allRooms.stream().map(ChatRoom::getId).toList();

        assertThat(latestIds).containsAll(allIds.subList(0, 3));
    }

    @Test
    @DisplayName("여러 사용자의 방이 있을 때 특정 사용자의 방만 조회된다")
    void multipleUsersDataIsolation() {
        // given
        List<ChatRoom> user1Rooms = createChatRooms(TEST_USER_ID, 3);
        List<ChatRoom> user2Rooms = createChatRooms(OTHER_USER_ID, 3);
        chatRoomJpaRepository.saveAll(user1Rooms);
        chatRoomJpaRepository.saveAll(user2Rooms);
        entityManager.flush();

        // when
        List<ChatRoom> user1Result = chatRoomRepository.findAllByUserIdLatest(TEST_USER_ID, 10);
        List<ChatRoom> user2Result = chatRoomRepository.findAllByUserIdLatest(OTHER_USER_ID, 10);

        // then
        assertThat(user1Result)
                .hasSize(3)
                .extracting(ChatRoom::getUserId)
                .containsOnly(TEST_USER_ID);

        assertThat(user2Result)
                .hasSize(3)
                .extracting(ChatRoom::getUserId)
                .containsOnly(OTHER_USER_ID);
    }

    private List<ChatRoom> createChatRooms(String userId, int count) {
        List<ChatRoom> rooms = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            long roomId = System.nanoTime() + i;
            rooms.add(ChatRoom.createChatRoom(roomId, userId, "방 " + i));
        }
        return rooms;
    }
}