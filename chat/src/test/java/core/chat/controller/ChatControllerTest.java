package core.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.chat.controller.request.*;
import core.chat.controller.response.*;
import core.chat.entity.ChatHistory;
import core.chat.service.ChatFacade;
import core.mcpclient.service.LLMHealthCheckService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static core.chat.fixture.ChatFixture.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatFacade chatFacade;

    @MockitoBean
    private LLMHealthCheckService llmHealthCheckService;

    private static final String CLIENT_ID_HEADER = "X-Client-Id";

    @Test
    @DisplayName("채팅 요청이 정상적으로 응답을 반환한다")
    void chat_shouldReturnChatResponse() throws Exception {
        ChatRequest request = new ChatRequest(TEST_ROOM_ID, QUESTION);
        ChatHistory chatHistory = ChatHistory.createLLMChatHistory(TEST_ROOM_ID, ANSWER);
        ChatResponse response = ChatResponse.of(chatHistory);

        given(chatFacade.chat(anyString(), any(ChatRequest.class))).willReturn(response);

        mockMvc.perform(post("/v1/chat")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(TEST_ROOM_ID))
                .andExpect(jsonPath("$.answerId").value(chatHistory.getId()))
                .andExpect(jsonPath("$.answer").value(ANSWER));

        verify(chatFacade).chat(anyString(), any(ChatRequest.class));
    }

    @Test
    @DisplayName("채팅 요청에서 roomId가 null이면 BadRequest를 반환한다")
    void chat_shouldReturnBadRequestWhenRoomIdIsNull() throws Exception {
        ChatRequest request = new ChatRequest(null, "What is Spring Boot?");

        mockMvc.perform(post("/v1/chat")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("채팅 요청에서 question이 비어있으면 BadRequest를 반환한다")
    void chat_shouldReturnBadRequestWhenQuestionIsBlank() throws Exception {
        ChatRequest request = new ChatRequest(1L, "");

        mockMvc.perform(post("/v1/chat")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("채팅 요청에서 권한이 없으면 BadRequest를 반환한다")
    @Disabled("Error 처리 구현 후 활성화")
    void chat_shouldThrowExceptionWhenUnauthorizedAccess() throws Exception {
        ChatRequest request = new ChatRequest(TEST_ROOM_ID, QUESTION);

        given(chatFacade.chat(anyString(), any()))
                .willThrow(new IllegalArgumentException());

        mockMvc.perform(post("/v1/chat")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("새로운 채팅방을 생성하고 응답을 반환한다")
    void createChatRoom_shouldReturnCreateChatRoomResponse() throws Exception {
        CreateChatRoomRequest request = new CreateChatRoomRequest(QUESTION);
        ChatHistory chatHistory = ChatHistory.createLLMChatHistory(TEST_ROOM_ID, ANSWER);
        CreateChatRoomResponse response = CreateChatRoomResponse.of(TEST_ROOM_NAME, chatHistory);

        given(chatFacade.startNewChat(any(String.class), any(CreateChatRoomRequest.class))).willReturn(response);

        mockMvc.perform(post("/v1/chat/room/create")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(response.getRoomId()))
                .andExpect(jsonPath("$.roomName").value(TEST_ROOM_NAME))
                .andExpect(jsonPath("$.answerId").value(chatHistory.getId()))
                .andExpect(jsonPath("$.answer").value(ANSWER));

        verify(chatFacade).startNewChat(any(String.class), any(CreateChatRoomRequest.class));
    }

    @Test
    @DisplayName("새로운 채팅방 생성에서 question이 비어있으면 BadRequest를 반환한다")
    void createChatRoom_shouldReturnBadRequestWhenQuestionIsBlank() throws Exception {
        CreateChatRoomRequest request = new CreateChatRoomRequest("");

        mockMvc.perform(post("/v1/chat/room/create")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("채팅 히스토리를 조회하고 응답을 반환한다")
    void getChatHistory_shouldReturnChatHistoriesResponse() throws Exception {
        Long roomId = 1L;
        ChatHistoriesResponse response = ChatHistoriesResponse.of(roomId, Collections.emptyList());

        given(chatFacade.getChatHistories(anyString(), any())).willReturn(response);

        mockMvc.perform(get("/v1/chat")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .param("roomId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.chattings", hasSize(0)));

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ChatHistoryRequest> captor = ArgumentCaptor.forClass(ChatHistoryRequest.class);
        verify(chatFacade).getChatHistories(userIdCaptor.capture(), captor.capture());
        assertThat(TEST_USER_ID).isEqualTo(userIdCaptor.getValue());
        assertThat(new ChatHistoryRequest(roomId, null, null)).usingRecursiveComparison().isEqualTo(captor.getValue());
    }

    @Test
    @DisplayName("페이지네이션과 함께 채팅 히스토리를 조회하고 응답을 반환한다")
    void getChatHistory_shouldReturnChatHistoriesWithPagination() throws Exception {
        Long roomId = 1L;
        Long lastChatId = 50L;
        Integer size = 10;

        ChatHistoriesResponse response = ChatHistoriesResponse.of(roomId, Collections.emptyList());

        given(chatFacade.getChatHistories(anyString(), any())).willReturn(response);

        mockMvc.perform(get("/v1/chat")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .param("roomId", "1")
                .param("lastChatId", "50")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.chattings", hasSize(0)));

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ChatHistoryRequest> captor = ArgumentCaptor.forClass(ChatHistoryRequest.class);
        verify(chatFacade).getChatHistories(userIdCaptor.capture(), captor.capture());
        assertThat(TEST_USER_ID).isEqualTo(userIdCaptor.getValue());
        assertThat(new ChatHistoryRequest(roomId, lastChatId, size)).usingRecursiveComparison().isEqualTo(captor.getValue());
    }

    @Test
    @DisplayName("채팅 히스토리 조회에서 roomId가 없으면 BadRequest를 반환한다")
    void getChatHistory_shouldReturnBadRequestWhenRoomIdIsNull() throws Exception {
        mockMvc.perform(get("/v1/chat")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("채팅 히스토리 조회에서 size가 최대값을 초과하면 BadRequest를 반환한다")
    @Disabled("Error 처리 구현 후 활성화")
    void getChatHistory_shouldReturnBadRequestWhenSizeExceedsMax() throws Exception {
        mockMvc.perform(get("/v1/chat")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .param("roomId", "1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== Get Chat Rooms Tests ====================
    @Test
    @DisplayName("채팅방 목록을 조회하고 응답을 반환한다")
    void getChatRooms_shouldReturnChatRoomListResponse() throws Exception {
        given(chatFacade.getChatRooms(anyString(), any(ChatRoomListRequest.class)))
                .willReturn(ChatRoomListResponse.of(Collections.emptyList()));

        mockMvc.perform(get("/v1/chat/room/list")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRooms", hasSize(0)));

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ChatRoomListRequest> requestCaptor = ArgumentCaptor.forClass(ChatRoomListRequest.class);
        verify(chatFacade).getChatRooms(userIdCaptor.capture(), requestCaptor.capture());
        assertThat(TEST_USER_ID).isEqualTo(userIdCaptor.getValue());
        assertThat(new ChatRoomListRequest(null, null)).usingRecursiveComparison()
                .isEqualTo(requestCaptor.getValue());
    }

    @Test
    @DisplayName("페이지네이션과 함께 채팅방 목록을 조회하고 응답을 반환한다")
    void getChatRooms_shouldReturnChatRoomListWithPagination() throws Exception {
        Long lastRoomId = 5L;
        Integer size = 10;
        ChatRoomListResponse response = ChatRoomListResponse.of(Collections.emptyList());

        given(chatFacade.getChatRooms(any(), any())).willReturn(response);

        mockMvc.perform(get("/v1/chat/room/list")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .param("lastRoomId", lastRoomId.toString())
                .param("size", size.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRooms", hasSize(0)));

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ChatRoomListRequest> requestCaptor = ArgumentCaptor.forClass(ChatRoomListRequest.class);
        verify(chatFacade).getChatRooms(userIdCaptor.capture(), requestCaptor.capture());
        assertThat(TEST_USER_ID).isEqualTo(userIdCaptor.getValue());
        assertThat(new ChatRoomListRequest(lastRoomId, size)).usingRecursiveComparison()
                .isEqualTo(requestCaptor.getValue());
    }

    @Test
    @DisplayName("채팅방 목록 조회에서 size가 최대값을 초과하면 BadRequest를 반환한다")
    void getChatRooms_shouldReturnBadRequestWhenSizeExceedsMax() throws Exception {
        mockMvc.perform(get("/v1/chat/room/list")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .param("size", "101")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== Delete Chat Room Tests ====================
    @Test
    @DisplayName("채팅방을 삭제하고 NoContent를 반환한다")
    void deleteChatRoom_shouldReturnNoContent() throws Exception {
        Long roomId = 1L;

        doNothing().when(chatFacade).deleteRoom(TEST_USER_ID, roomId);

        mockMvc.perform(delete("/v1/chat/room")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .param("roomId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(chatFacade).deleteRoom(TEST_USER_ID, roomId);
    }

    @Test
    @DisplayName("채팅방 삭제에서 roomId가 없으면 BadRequest를 반환한다")
    void deleteChatRoom_shouldReturnBadRequestWhenRoomIdIsNull() throws Exception {
        mockMvc.perform(delete("/v1/chat/room")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("채팅방 삭제에서 권한이 없으면 BadRequest를 반환한다")
    @Disabled("Error 처리 구현 후 활성화")
    void deleteChatRoom_shouldThrowExceptionWhenUnauthorizedAccess() throws Exception {
        Long roomId = 1L;

        doThrow(new IllegalArgumentException("Room Id(1)를 삭제할 권한이 test-user-123에게 존재하지 않습니다."))
                .when(chatFacade).deleteRoom(TEST_USER_ID, roomId);

        mockMvc.perform(delete("/v1/chat/room")
                .header(CLIENT_ID_HEADER, TEST_USER_ID)
                .param("roomId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== Health Check Tests ====================
    @Test
    @DisplayName("LLM 헬스 체크 - 연결되었을 때 Ok를 반환한다")
    void llmHealthCheck_shouldReturnOkWhenConnected() throws Exception {
        given(llmHealthCheckService.isLLMConnected()).willReturn(true);

        mockMvc.perform(get("/v1/mcp/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(llmHealthCheckService).isLLMConnected();
    }

    @Test
    @DisplayName("LLM 헬스 체크 - 연결되지 않았을 때 InternalServerError를 반환한다")
    void llmHealthCheck_shouldReturnInternalServerErrorWhenDisconnected() throws Exception {
        given(llmHealthCheckService.isLLMConnected()).willReturn(false);

        mockMvc.perform(get("/v1/mcp/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(llmHealthCheckService).isLLMConnected();
    }

    @Test
    @DisplayName("애플리케이션 헬스 체크가 Ok를 반환한다")
    void healthCheck_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}