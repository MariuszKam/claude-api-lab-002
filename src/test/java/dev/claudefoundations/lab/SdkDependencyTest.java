package dev.claudefoundations.lab;

import com.anthropic.models.messages.MessageCreateParams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SdkDependencyTest {
    @Test
    void officialAnthropicSdkIsAvailableOnTheClasspath() {
        assertNotNull(MessageCreateParams.class);
    }
}
