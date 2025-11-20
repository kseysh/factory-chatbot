package core.chat.service;

import core.chat.controller.response.ChatRoomResponse;
import core.chat.entity.ChatRoom;
import core.chat.repository.ChatRoomRepository;
import core.chat.service.dto.ChatRoomDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static core.chat.fixture.ChatFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    private ChatRoom testChatRoom;
    private ChatRoom otherChatRoom;

    @BeforeEach
    void setUp() {
        testChatRoom = ChatRoom.builder()
                .id(TEST_ROOM_ID)
                .userId(TEST_USER_ID)
                .name(TEST_ROOM_NAME)
                .createdAt(LocalDateTime.now())
                .build();

        otherChatRoom = ChatRoom.builder()
                .id(OTHER_ROOM_ID)
                .userId(TEST_USER_ID)
                .name(OTHER_ROOM_NAME)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("채팅 방을 저장할 수 있다")
    void saveChatRoom() {
        // given
        ChatRoom newRoom = ChatRoom.createChatRoom(3L, TEST_USER_ID, "새로운 방");

        // when
        chatRoomService.saveChatRoom(newRoom);

        // then
        verify(chatRoomRepository).insertWithoutSelect(newRoom);
    }

    @Test
    @DisplayName("roomId로 채팅 방을 조회할 수 있다")
    void findChatRoomByRoomId() {
        // given
        given(chatRoomRepository.findByRoomId(TEST_ROOM_ID)).willReturn(Optional.of(testChatRoom));

        // when
        Optional<ChatRoomDto> result = chatRoomService.findChatRoomByRoomId(TEST_ROOM_ID);

        // then
        assertThat(result)
                .isPresent()
                .get()
                .extracting(ChatRoomDto::getId, ChatRoomDto::getUserId, ChatRoomDto::getName)
                .containsExactly(TEST_ROOM_ID, TEST_USER_ID, TEST_ROOM_NAME);

        verify(chatRoomRepository).findByRoomId(TEST_ROOM_ID);
    }

    @Test
    @DisplayName("존재하지 않는 roomId로 조회하면 빈 Optional을 반환한다")
    void findChatRoomByRoomId_WithNonExistentId() {
        // given
        Long nonExistentId = 999L;
        given(chatRoomRepository.findByRoomId(nonExistentId)).willReturn(Optional.empty());

        // when
        Optional<ChatRoomDto> result = chatRoomService.findChatRoomByRoomId(nonExistentId);

        // then
        assertThat(result).isEmpty();
        verify(chatRoomRepository).findByRoomId(nonExistentId);
    }

    @Test
    @DisplayName("사용자의 최신 채팅 방을 조회할 수 있다")
    void findChatRoomsLatest() {
        // given
        given(chatRoomRepository.findAllByUserIdLatest(TEST_USER_ID, 10))
                .willReturn(List.of(testChatRoom, otherChatRoom));

        // when
        List<ChatRoomResponse> result = chatRoomService.findChatRoomsLatest(TEST_USER_ID, 10);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting(ChatRoomResponse::getRoomId, ChatRoomResponse::getRoomName)
                .containsExactly(
                        tuple(TEST_ROOM_ID, TEST_ROOM_NAME),
                        tuple(OTHER_ROOM_ID, OTHER_ROOM_NAME)
                );

        verify(chatRoomRepository).findAllByUserIdLatest(TEST_USER_ID, 10);
    }

    @Test
    @DisplayName("사용자가 최신 채팅 방을 조회할 때 빈 리스트를 반환할 수 있다")
    void findChatRoomsLatest_EmptyResult() {
        // given
        given(chatRoomRepository.findAllByUserIdLatest(TEST_USER_ID, 10)).willReturn(List.of());

        // when
        List<ChatRoomResponse> result = chatRoomService.findChatRoomsLatest(TEST_USER_ID, 10);

        // then
        assertThat(result).isEmpty();
        verify(chatRoomRepository).findAllByUserIdLatest(TEST_USER_ID, 10);
    }

    @Test
    @DisplayName("특정 limit으로 최신 채팅 방을 조회할 수 있다")
    void findChatRoomsLatest_WithLimit() {
        // given
        given(chatRoomRepository.findAllByUserIdLatest(TEST_USER_ID, 1)).willReturn(List.of(testChatRoom));

        // when
        List<ChatRoomResponse> result = chatRoomService.findChatRoomsLatest(TEST_USER_ID, 1);

        // then
        assertThat(result).hasSize(1);
        verify(chatRoomRepository).findAllByUserIdLatest(TEST_USER_ID, 1);
    }

    @Test
    @DisplayName("특정 방 이후의 사용자 채팅 방을 조회할 수 있다")
    void findChatRoomsAfter() {
        // given
        when(chatRoomRepository.findAllByUserIdAfterRoomId(TEST_USER_ID, TEST_ROOM_ID, 10))
                .thenReturn(List.of(otherChatRoom));

        // when
        List<ChatRoomResponse> result = chatRoomService.findChatRoomsAfter(TEST_USER_ID, TEST_ROOM_ID, 10);

        // then
        assertThat(result)
                .hasSize(1)
                .extracting(ChatRoomResponse::getRoomId, ChatRoomResponse::getRoomName)
                .containsExactly(
                        tuple(OTHER_ROOM_ID, OTHER_ROOM_NAME)
                );

        verify(chatRoomRepository).findAllByUserIdAfterRoomId(TEST_USER_ID, TEST_ROOM_ID, 10);
    }

    @Test
    @DisplayName("특정 방 이후의 채팅 방 조회가 빈 리스트를 반환할 수 있다")
    void findChatRoomsAfter_EmptyResult() {
        // given
        Long nonExistentId = 999L;
        given(chatRoomRepository.findAllByUserIdAfterRoomId(TEST_USER_ID, nonExistentId, 10))
                .willReturn(List.of());

        // when
        List<ChatRoomResponse> result = chatRoomService.findChatRoomsAfter(TEST_USER_ID, nonExistentId, 10);

        // then
        assertThat(result).isEmpty();
        verify(chatRoomRepository).findAllByUserIdAfterRoomId(TEST_USER_ID, nonExistentId, 10);
    }

    @Test
    @DisplayName("페이지네이션으로 채팅 방을 조회할 수 있다")
    void findChatRoomsAfter_WithPagination() {
        // given
        given(chatRoomRepository.findAllByUserIdAfterRoomId(TEST_USER_ID, TEST_ROOM_ID, 5))
                .willReturn(List.of(otherChatRoom));

        // when
        List<ChatRoomResponse> result = chatRoomService.findChatRoomsAfter(TEST_USER_ID, TEST_ROOM_ID, 5);

        // then
        assertThat(result).hasSize(1);
        verify(chatRoomRepository).findAllByUserIdAfterRoomId(TEST_USER_ID, TEST_ROOM_ID, 5);
    }

    @Test
    @DisplayName("채팅 방을 삭제할 수 있다")
    void deleteRoom() {
        // when
        chatRoomService.deleteRoom(TEST_ROOM_ID);

        // then
        verify(chatRoomRepository).deleteById(TEST_ROOM_ID);
    }

    @Test
    @DisplayName("사용자가 특정 방에 접근 권한을 가지는지 확인할 수 있다")
    void canUserAccessRoom_WithAccess() {
        // given
        given(chatRoomRepository.existsByRoomIdAndUserId(TEST_ROOM_ID, TEST_USER_ID)).willReturn(true);

        // when
        boolean result = chatRoomService.canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID);

        // then
        assertTrue(result);
        verify(chatRoomRepository).existsByRoomIdAndUserId(TEST_ROOM_ID, TEST_USER_ID);
    }

    @Test
    @DisplayName("사용자가 특정 방에 접근 권한이 없는지 확인할 수 있다")
    void canUserAccessRoom_WithoutAccess() {
        // given
        given(chatRoomRepository.existsByRoomIdAndUserId(TEST_ROOM_ID, OTHER_USER_ID)).willReturn(false);

        // when
        boolean result = chatRoomService.canUserAccessRoom(TEST_ROOM_ID, OTHER_USER_ID);

        // then
        assertFalse(result);
        verify(chatRoomRepository).existsByRoomIdAndUserId(TEST_ROOM_ID, OTHER_USER_ID);
    }

    @Test
    @DisplayName("다른 사용자는 특정 방에 접근할 수 없다")
    void canUserAccessRoom_DifferentUser() {
        // given
        String unknownUserId = "unknown_user";
        given(chatRoomRepository.existsByRoomIdAndUserId(TEST_ROOM_ID, unknownUserId)).willReturn(false);

        // when
        boolean result = chatRoomService.canUserAccessRoom(TEST_ROOM_ID, unknownUserId);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("존재하지 않는 방은 접근할 수 없다")
    void canUserAccessRoom_NonExistentRoom() {
        // given
        Long nonExistentId = 999L;
        given(chatRoomRepository.existsByRoomIdAndUserId(nonExistentId, TEST_USER_ID)).willReturn(false);

        // when
        boolean result = chatRoomService.canUserAccessRoom(nonExistentId, TEST_USER_ID);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("여러 채팅 방을 조회할 때 각각의 응답이 올바른 형식인지 확인한다")
    void findChatRoomsLatest_ResponseFormat() {
        // given
        given(chatRoomRepository.findAllByUserIdLatest(TEST_USER_ID, 1)).willReturn(List.of(testChatRoom));

        // when
        List<ChatRoomResponse> result = chatRoomService.findChatRoomsLatest(TEST_USER_ID, 1);

        // then
        assertThat(result)
                .hasSize(1)
                .allSatisfy(response -> {
                    assertThat(response.getRoomId()).isNotNull();
                    assertThat(response.getRoomName()).isNotNull();
                    assertThat(response.getDate()).isNotNull();
                });
    }
}