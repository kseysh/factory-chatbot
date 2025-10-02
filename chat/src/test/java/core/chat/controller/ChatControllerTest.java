package core.chat.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

class ChatControllerTest {

    RestClient restClient = RestClient.create("http://localhost:8080");

    @Test
    void statusCheckTest() {
        ResponseEntity responseEntity = restClient.get()
            .uri("/v1/mcp/health")
            .retrieve()
            .body(ResponseEntity.class);

        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}