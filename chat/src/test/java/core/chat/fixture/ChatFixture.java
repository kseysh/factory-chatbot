package core.chat.fixture;

import core.chat.controller.request.ChatRequest;
import core.chat.controller.request.CreateChatRoomRequest;

public class ChatFixture {

    public static final String TEST_USER_ID = "testUser";
    public static final String OTHER_USER_ID = "otherUser";

    public static final String QUESTION = "사용자 질문";
    public static final String ANSWER = "LLM 답변";

    public static final String TEST_ROOM_NAME = "방 이름";
    public static final String OTHER_ROOM_NAME = "다른 방 이름";
    public static final Long TEST_ROOM_ID = 1L;
    public static final Long OTHER_ROOM_ID = 2L;

    public static final ChatRequest CHAT_REQUEST = new ChatRequest(TEST_ROOM_ID, QUESTION);
    public static final CreateChatRoomRequest CREATE_CHAT_ROOM_REQUEST = new CreateChatRoomRequest(QUESTION);
}
