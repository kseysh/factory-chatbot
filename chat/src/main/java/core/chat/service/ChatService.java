package core.chat.service;

import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import core.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    @Transactional
    public void saveChatHistory(ChatHistory userChat, ChatHistory llmChat){
        chatRepository.insertChatHistoryWithoutSelect(userChat);
        chatRepository.insertChatHistoryWithoutSelect(llmChat);
    }

    @Transactional
    public void saveChatHistoryAndChatRoom(ChatRoom chatRoom, ChatHistory userChat, ChatHistory llmChat){
        chatRepository.insertChatRoomWithoutSelect(chatRoom);
        chatRepository.insertChatHistoryWithoutSelect(userChat);
        chatRepository.insertChatHistoryWithoutSelect(llmChat);
    }

    @Transactional(readOnly = true)
    public boolean checkIsValidRoomId(String userId, Long roomId) {
        return chatRepository.findAllRoomIdByUserId(userId).contains(roomId);
    }
}
