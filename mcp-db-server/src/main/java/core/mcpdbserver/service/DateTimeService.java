package core.mcpdbserver.service;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DateTimeService {

    @Tool(name = "get_current_date_time", description = "Get the current date and time in the user's timezone")
    public String getCurrentDateTime() {
        log.info("get_current_date_time Tool Use");
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }
}
