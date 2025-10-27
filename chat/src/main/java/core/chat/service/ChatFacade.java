package core.chat.service;

import core.chat.controller.request.ChatRequest;
import core.chat.controller.request.CreateChatRoomRequest;
import core.chat.controller.response.ChatResponse;
import core.chat.controller.response.CreateChatRoomResponse;
import core.chat.entity.ChatHistory;
import core.chat.entity.MessageType;
import core.chat.repository.ChatRoomRepository;
import core.common.snowflake.Snowflake;
import core.mcpclient.service.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatFacade {

    private final LLMService llmService;
    private final ChatHistoryService chatHistoryService;
    private final ChatRoomRepository chatRoomRepository;
    private final Snowflake snowflake = Snowflake.getInstance();

    public ChatResponse chat(String userId, ChatRequest chatRequest) {
        Long roomId = chatRequest.getRoomId();
        String question = chatRequest.getQuestion();
        if (!checkIsValidRoomId(userId, roomId)) {
            throw new IllegalArgumentException();
        }

        String answer = llmService.chat(roomId, question);

        ChatHistory userChat = ChatHistory.builder()
                .id(snowflake.nextId())
                .roomId(roomId)
                .type(MessageType.USER)
                .content(question)
                .build();

        ChatHistory llmChat = ChatHistory.builder()
                .id(snowflake.nextId())
                .roomId(roomId)
                .type(MessageType.SYSTEM)
                .content(answer)
                .build();

        chatHistoryService.saveChatHistory(userChat, llmChat);

        return ChatResponse.of(llmChat);
    }

    private boolean checkIsValidRoomId(String userId, Long roomId) {
        return chatRoomRepository.findByUserId(userId).getId().equals(roomId);
    }



    public CreateChatRoomResponse createChatRoom(String userId, CreateChatRoomRequest request) {
        Long roomId = Snowflake.getInstance().nextId();
        // 1. chatRoom을 위한 Id를 만든다.
        // 2. roomId를 이용해 LLM에게 질의한다.

        // 3. LLM은 채팅방 이름과 질문에 대한 답을 준다.

        // 4. 채팅방 이름을 이용해 chatRoom을 만든다.

        // 5. 유저의 질문과 LLM 답변을 저장한다.
        return CreateChatRoomResponse.of(roomId.toString(), ChatHistory.builder().build());
    }
}
