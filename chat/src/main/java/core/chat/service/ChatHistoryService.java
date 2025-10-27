package core.chat.service;

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
        chatHistoryRepository.saveAll(List.of(userChat, llmChat));
    }
}
