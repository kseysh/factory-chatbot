package core.chat.service;

import core.chat.controller.request.*;
import core.chat.controller.response.*;
import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import core.chat.service.dto.ChatRoomDto;
import core.mcpclient.service.LLMService;
import core.mcpclient.service.dto.NewChatRoomInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static core.chat.fixture.ChatFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatFacadeTest {

    @Mock
    private LLMService llmService;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private ChatHistoryService chatHistoryService;

    @InjectMocks
    private ChatFacade chatFacade;

    @Test
    @DisplayName("기존 방에서 채팅을 할 수 있다")
    void chat() {
        // given
        given(chatRoomService.canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID)).willReturn(true);
        given(llmService.chat(TEST_ROOM_ID, QUESTION)).willReturn(ANSWER);

        // when
        ChatResponse result = chatFacade.chat(TEST_USER_ID, CHAT_REQUEST);

        // then
        assertThat(result)
                .isNotNull()
                .extracting(ChatResponse::getRoomId, ChatResponse::getAnswer)
                .containsExactly(TEST_ROOM_ID, ANSWER);

        verify(chatRoomService).canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID);
        verify(llmService).chat(TEST_ROOM_ID, QUESTION);
        verify(chatHistoryService).saveChatHistory(any(ChatHistory.class), any(ChatHistory.class));
    }

    @Test
    @DisplayName("권한이 없는 방에 접근하면 예외가 발생한다")
    void chat_UnauthorizedAccess() {
        // given
        given(chatRoomService.canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> chatFacade.chat(TEST_USER_ID, CHAT_REQUEST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid room ID");

        verify(chatRoomService).canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID);
        verify(llmService, never()).chat(anyLong(), anyString());
    }

    @Test
    @DisplayName("새로운 채팅 방을 시작할 수 있다")
    void startNewChat() {
        // given
        given(llmService.startNewChat(anyLong(), anyString()))
                .willReturn(new NewChatRoomInfo(TEST_ROOM_NAME, ANSWER));

        // when
        CreateChatRoomResponse result = chatFacade.startNewChat(TEST_USER_ID, CREATE_CHAT_ROOM_REQUEST);

        // then
        assertThat(result)
                .isNotNull()
                .extracting(CreateChatRoomResponse::getRoomName, CreateChatRoomResponse::getAnswer)
                .containsExactly(TEST_ROOM_NAME, ANSWER);

        verify(llmService).startNewChat(anyLong(), anyString());
        verify(chatRoomService).saveChatRoom(any(ChatRoom.class));
        verify(chatHistoryService).saveChatHistory(any(ChatHistory.class), any(ChatHistory.class));
    }

    @Test
    @DisplayName("새로운 채팅 방 시작 시 사용자 ID가 저장된다")
    void startNewChat_UserIdSaved() {
        // given
        given(llmService.startNewChat(anyLong(), anyString()))
                .willReturn(new NewChatRoomInfo(TEST_ROOM_NAME, ANSWER));

        // when
        chatFacade.startNewChat(TEST_USER_ID, CREATE_CHAT_ROOM_REQUEST);

        // then
        verify(chatRoomService).saveChatRoom(any(ChatRoom.class));
    }

    @Test
    @DisplayName("최신 채팅 이력을 조회할 수 있다")
    void getChatHistories_Latest() {
        // given
        List<ChatHistoryResponse> histories = List.of(
                ChatHistoryResponse.of(ChatHistory.createUserChatHistory(TEST_ROOM_ID, QUESTION)),
                ChatHistoryResponse.of(ChatHistory.createLLMChatHistory(TEST_ROOM_ID, ANSWER))
        );

        given(chatRoomService.canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID))
                .willReturn(true);
        given(chatHistoryService.getChatHistoriesLatest(TEST_ROOM_ID, 10))
                .willReturn(histories);

        // when
        ChatHistoriesResponse result = chatFacade.getChatHistories(
                TEST_USER_ID, new ChatHistoryRequest(TEST_ROOM_ID, null, 10)
        );

        // then
        assertThat(result)
                .isNotNull()
                .extracting(ChatHistoriesResponse::getRoomId)
                .isEqualTo(TEST_ROOM_ID);
        assertThat(result.getChattings())
                .hasSize(2);

        verify(chatRoomService).canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID);
        verify(chatHistoryService).getChatHistoriesLatest(TEST_ROOM_ID, 10);
    }

    @Test
    @DisplayName("페이지네이션으로 채팅 이력을 조회할 수 있다")
    void getChatHistories_Paginated() {
        // given
        Long lastChatId = 100L;
        ChatHistoryRequest request = new ChatHistoryRequest(TEST_ROOM_ID, lastChatId, 10);
        List<ChatHistoryResponse> histories = List.of(
                ChatHistoryResponse.of(ChatHistory.createUserChatHistory(1L, QUESTION))
        );

        given(chatRoomService.canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID))
                .willReturn(true);
        given(chatHistoryService.getChatHistoriesAfter(TEST_ROOM_ID, lastChatId, 10))
                .willReturn(histories);

        // when
        ChatHistoriesResponse result = chatFacade.getChatHistories(TEST_USER_ID, request);

        // then
        assertThat(result)
                .isNotNull()
                .extracting(ChatHistoriesResponse::getRoomId)
                .isEqualTo(TEST_ROOM_ID);

        verify(chatRoomService).canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID);
        verify(chatHistoryService).getChatHistoriesAfter(TEST_ROOM_ID, lastChatId, 10);
    }

    @Test
    @DisplayName("권한이 없는 방의 이력은 조회할 수 없다")
    void getChatHistories_UnauthorizedAccess() {
        // given
        ChatHistoryRequest request = new ChatHistoryRequest(TEST_ROOM_ID, null, 10);
        given(chatRoomService.canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> chatFacade.getChatHistories(TEST_USER_ID, request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(chatRoomService).canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID);
        verify(chatHistoryService, never()).getChatHistoriesLatest(anyLong(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    @DisplayName("사용자의 최신 채팅 방 목록을 조회할 수 있다")
    void getChatRooms_Latest() {
        // given
        ChatRoomListRequest request = new ChatRoomListRequest(null, 10);
        ChatRoom chatRoom1 = ChatRoom.createChatRoom(TEST_ROOM_ID, TEST_USER_ID, "방1");
        ChatRoom chatRoom2 = ChatRoom.createChatRoom(OTHER_ROOM_ID, TEST_USER_ID, "방2");
        ReflectionTestUtils.setField(chatRoom1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(chatRoom2, "createdAt", LocalDateTime.now());
        List<ChatRoomResponse> rooms = List.of(ChatRoomResponse.of(chatRoom1), ChatRoomResponse.of(chatRoom2));

        given(chatRoomService.findChatRoomsLatest(TEST_USER_ID, 10))
                .willReturn(rooms);

        // when
        ChatRoomListResponse result = chatFacade.getChatRooms(TEST_USER_ID, request);

        // then
        assertThat(result)
                .isNotNull();
        assertThat(result.getChatRooms())
                .hasSize(2);

        verify(chatRoomService).findChatRoomsLatest(TEST_USER_ID, 10);
    }

    @Test
    @DisplayName("페이지네이션으로 채팅 방 목록을 조회할 수 있다")
    void getChatRooms_Paginated() {
        // given
        Long lastRoomId = 100L;
        ChatRoomListRequest request = new ChatRoomListRequest(lastRoomId, 10);
        ChatRoom chatRoom = ChatRoom.createChatRoom(OTHER_ROOM_ID, TEST_USER_ID, "방2");
        ReflectionTestUtils.setField(chatRoom, "createdAt", LocalDateTime.now());
        List<ChatRoomResponse> rooms = List.of(ChatRoomResponse.of(chatRoom));

        given(chatRoomService.findChatRoomsAfter(TEST_USER_ID, lastRoomId, 10))
                .willReturn(rooms);

        // when
        ChatRoomListResponse result = chatFacade.getChatRooms(TEST_USER_ID, request);

        // then
        assertThat(result)
                .isNotNull();
        assertThat(result.getChatRooms())
                .hasSize(1);

        verify(chatRoomService).findChatRoomsAfter(TEST_USER_ID, lastRoomId, 10);
    }

    @Test
    @DisplayName("자신의 방을 삭제할 수 있다")
    void deleteRoom() {
        // given
        ChatRoomDto chatRoomDto = ChatRoomDto.of(ChatRoom.createChatRoom(TEST_ROOM_ID, TEST_USER_ID, TEST_ROOM_NAME));
        given(chatRoomService.findChatRoomByRoomId(TEST_ROOM_ID))
                .willReturn(Optional.of(chatRoomDto));

        // when
        chatFacade.deleteRoom(TEST_USER_ID, TEST_ROOM_ID);

        // then
        verify(chatRoomService).findChatRoomByRoomId(TEST_ROOM_ID);
        verify(chatRoomService).deleteRoom(TEST_ROOM_ID);
        verify(chatHistoryService).deleteChatHistoryByRoomId(TEST_ROOM_ID);
    }

    @Test
    @DisplayName("다른 사용자의 방은 삭제할 수 없다")
    void deleteRoom_UnauthorizedDelete() {
        // given
        given(chatRoomService.findChatRoomByRoomId(TEST_ROOM_ID))
                .willReturn(Optional.of(
                        ChatRoomDto.of(ChatRoom.createChatRoom(TEST_ROOM_ID, OTHER_USER_ID, TEST_ROOM_NAME))
                ));

        // when & then
        assertThatThrownBy(() -> chatFacade.deleteRoom(TEST_USER_ID, TEST_ROOM_ID))
                .isInstanceOf(IllegalArgumentException.class);

        verify(chatRoomService).findChatRoomByRoomId(TEST_ROOM_ID);
        verify(chatRoomService, never()).deleteRoom(anyLong());
        verify(chatHistoryService, never()).deleteChatHistoryByRoomId(anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 방을 삭제할 수 있다")
    void deleteRoom_NonExistentRoom() {
        // given
        given(chatRoomService.findChatRoomByRoomId(999L))
                .willReturn(Optional.empty());

        // when
        chatFacade.deleteRoom(TEST_USER_ID, 999L);

        // then
        verify(chatRoomService).findChatRoomByRoomId(999L);
        verify(chatRoomService).deleteRoom(999L);
        verify(chatHistoryService).deleteChatHistoryByRoomId(999L);
    }

    @Test
    @DisplayName("채팅 시 사용자와 LLM 메시지가 모두 저장된다")
    void chat_SavesBothUserAndLLMMessages() {
        // given
        given(chatRoomService.canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID)).willReturn(true);
        given(llmService.chat(TEST_ROOM_ID, QUESTION)).willReturn(ANSWER);

        // when
        chatFacade.chat(TEST_USER_ID, new ChatRequest(TEST_ROOM_ID, QUESTION));

        // then
        verify(chatHistoryService, times(1))
                .saveChatHistory(any(ChatHistory.class), any(ChatHistory.class));
    }

    @Test
    @DisplayName("새 방 시작 시 사용자와 LLM 메시지가 모두 저장된다")
    void startNewChat_SavesBothUserAndLLMMessages() {
        // given
        given(llmService.startNewChat(anyLong(), anyString()))
                .willReturn(new NewChatRoomInfo(TEST_ROOM_NAME, ANSWER));

        // when
        chatFacade.startNewChat(TEST_USER_ID, new CreateChatRoomRequest(QUESTION));

        // then
        verify(chatHistoryService, times(1)).saveChatHistory(any(ChatHistory.class), any(ChatHistory.class));
    }

    @Test
    @DisplayName("채팅 응답은 LLM 메시지의 ID를 포함한다")
    void chat_ResponseContainsAnswerId() {
        // given
        given(chatRoomService.canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID)).willReturn(true);
        given(llmService.chat(TEST_ROOM_ID, QUESTION)).willReturn(ANSWER);

        // when
        ChatResponse result = chatFacade.chat(TEST_USER_ID, new ChatRequest(TEST_ROOM_ID, QUESTION));

        // then
        assertThat(result.getAnswerId()).isNotNull();
    }

    @Test
    @DisplayName("새 방 시작 응답은 방 ID를 포함한다")
    void startNewChat_ResponseContainsRoomId() {
        // given
        given(llmService.startNewChat(anyLong(), anyString()))
                .willReturn(new NewChatRoomInfo(TEST_ROOM_NAME, ANSWER));

        // when
        CreateChatRoomResponse result = chatFacade.startNewChat(TEST_USER_ID, new CreateChatRoomRequest(QUESTION));

        // then
        assertThat(result.getRoomId()).isNotNull();
    }

    @Test
    @DisplayName("빈 채팅 이력 목록을 조회할 수 있다")
    void getChatHistories_EmptyList() {
        // given
        given(chatRoomService.canUserAccessRoom(TEST_ROOM_ID, TEST_USER_ID)).willReturn(true);
        given(chatHistoryService.getChatHistoriesLatest(TEST_ROOM_ID, 10)).willReturn(List.of());

        // when
        ChatHistoriesResponse result =
                chatFacade.getChatHistories(TEST_USER_ID, new ChatHistoryRequest(TEST_ROOM_ID, null, 10));

        // then
        assertThat(result.getChattings()).isEmpty();
    }

    @Test
    @DisplayName("빈 채팅 방 목록을 조회할 수 있다")
    void getChatRooms_EmptyList() {
        // given
        given(chatRoomService.findChatRoomsLatest(TEST_USER_ID, 10)).willReturn(List.of());

        // when
        ChatRoomListResponse result =
                chatFacade.getChatRooms(TEST_USER_ID, new ChatRoomListRequest(null, 10));

        // then
        assertThat(result.getChatRooms()).isEmpty();
    }
}