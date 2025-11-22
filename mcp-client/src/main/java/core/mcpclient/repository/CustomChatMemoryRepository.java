package core.mcpclient.repository;

import static core.mcpclient.config.ChatMemoryConfig.MAX_MESSAGES;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.jdbc.core.RowMapper;

@Repository
@RequiredArgsConstructor
public class CustomChatMemoryRepository implements ChatMemoryRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final String SELECT_MESSAGES_SQL =
            "SELECT content, type FROM chat_history WHERE room_id = ? ORDER BY id LIMIT ?";
    // 원래 구현되어 있었던 SQL:
    // return "SELECT content, type FROM SPRING_AI_CHAT_MEMORY WHERE conversation_id = ? ORDER BY \"timestamp\"";

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be empty");
        Long roomId = Long.valueOf(conversationId);
        return jdbcTemplate.query(SELECT_MESSAGES_SQL, new MessageRowMapper(), roomId, MAX_MESSAGES);
    }

    private static class MessageRowMapper implements RowMapper<Message> {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            String content = rs.getString("content");
            String typeStr = rs.getString("type");

            MessageType type = MessageType.valueOf(typeStr);

            return switch (type) {
                case USER -> new UserMessage(content);
                case ASSISTANT -> new AssistantMessage(content);
                case SYSTEM -> new SystemMessage(content);
                case TOOL -> new ToolResponseMessage(List.of());
            };
        }
    }

    @Override
    public List<String> findConversationIds() {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        throw new IllegalArgumentException("Not implemented");
    }
}
