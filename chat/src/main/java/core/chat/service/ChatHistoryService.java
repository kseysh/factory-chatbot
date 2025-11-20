package core.chat.service;

import core.chat.controller.response.ChatHistoryResponse;
import core.chat.entity.ChatHistory;
import core.chat.repository.ChatHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;

    @Transactional
    public void saveChatHistory(ChatHistory userChat, ChatHistory llmChat){
        chatHistoryRepository.insertChatHistoryWithoutSelect(userChat);
        chatHistoryRepository.insertChatHistoryWithoutSelect(llmChat);
    }

    @Transactional(readOnly = true)
    public List<ChatHistoryResponse> getChatHistoriesLatest(Long roomId, Integer limit) {
        return chatHistoryRepository.findAllByRoomIdLatest(roomId, limit)
                .stream()
                .map(ChatHistoryResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatHistoryResponse> getChatHistoriesAfter(Long roomId, Long lastChatId, Integer limit) {
        return chatHistoryRepository.findAllByRoomIdAfterChatId(roomId, lastChatId, limit)
                .stream()
                .map(ChatHistoryResponse::of)
                .toList();
    }

    @Transactional
    public void deleteChatHistoryByRoomId(Long roomId) {
        chatHistoryRepository.deleteChatHistoryByRoomId(roomId);
    }
}
