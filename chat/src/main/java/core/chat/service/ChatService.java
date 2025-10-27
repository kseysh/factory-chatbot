package core.chat.service;

import core.chat.entity.ChatHistory;
import core.chat.entity.ChatRoom;
import core.chat.repository.ChatHistoryRepository;
import core.chat.repository.ChatRoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public void saveChatHistory(ChatHistory userChat, ChatHistory llmChat){
        chatHistoryRepository.saveAll(List.of(userChat, llmChat));
    }

    @Transactional
    public void saveChatHistoryAndChatRoom(ChatRoom chatRoom, ChatHistory userChat, ChatHistory llmChat){
        chatRoomRepository.save(chatRoom);
        chatHistoryRepository.saveAll(List.of(userChat, llmChat));
    }

    @Transactional(readOnly = true)
    public boolean checkIsValidRoomId(String userId, Long roomId) {
        return !chatRoomRepository.findByUserId(userId).getId().equals(roomId);
    }
}
