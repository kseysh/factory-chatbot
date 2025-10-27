package core.mcpclient.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewChatRoomInfo(String roomName, String answer) { }
