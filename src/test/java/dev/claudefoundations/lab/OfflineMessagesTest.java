package dev.claudefoundations.lab;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.InetAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class OfflineMessagesTest {
    @Test
    @Timeout(15)
    void sendsMessageToLocalMockAndReadsItsResponse() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            // Bind only to loopback; port 0 lets the OS choose an available port.
            server.start(InetAddress.getByName("127.0.0.1"), 0);
            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("""
                            {
                              "id": "msg_offline_001",
                              "type": "message",
                              "role": "assistant",
                              "model": "offline-test-model",
                              "content": [{"type": "text", "text": "Hello from the local mock!"}],
                              "stop_reason": "end_turn",
                              "stop_sequence": null,
                              "usage": {"input_tokens": 5, "output_tokens": 7}
                            }
                            """));

            // No fromEnv(): neither credentials nor the destination come from the environment.
            AnthropicClient client = AnthropicOkHttpClient.builder()
                    .apiKey("offline-test-key")
                    .baseUrl(server.url("/").toString())
                    .timeout(Duration.ofSeconds(5))
                    .maxRetries(0)
                    .build();
            try {
                MessageCreateParams params = MessageCreateParams.builder()
                        .model("offline-test-model")
                        .maxTokens(32L)
                        .addUserMessage("Hello from the offline test")
                        .build();

                Message message = client.messages().create(params);

                RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
                assertNotNull(request, "The local mock must receive the SDK request");
                assertEquals(1, server.getRequestCount());
                assertEquals("POST", request.getMethod());
                assertEquals("/v1/messages", request.getPath());
                assertEquals("Bearer offline-test-key", request.getHeader("Authorization"));

                // Compare JSON fields, without depending on whitespace or property order.
                JsonNode body = new ObjectMapper().readTree(request.getBody().readUtf8());
                assertEquals("offline-test-model", body.path("model").asText());
                assertEquals(32, body.path("max_tokens").asInt());
                assertEquals(1, body.path("messages").size());
                assertEquals("user", body.at("/messages/0/role").asText());
                assertEquals("Hello from the offline test",
                        body.at("/messages/0/content").asText());

                assertEquals("msg_offline_001", message.id());
                assertEquals("Hello from the local mock!",
                        message.content().getFirst().asText().text());
            } finally {
                client.close();
            }
        }
    }
}
