package dev.claudefoundations.lab;

import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.Model;

import java.util.List;

public final class MessageCreator {

    public static MessageCreateParams createHelloParam(long maxTokens) {
        return MessageCreateParams.builder()
                .model(Model.CLAUDE_HAIKU_5_5)
                .maxTokens(maxTokens)
                .system("You are nice welcoming assistant")
                .addUserMessage("Hello Claude")
                .build();
    }

    public static MessageCreateParams createParamsWithHistory(List<MessageParam> messages) {
        return basicMessageCreateParams().messages(messages).build();
    }

    private static MessageCreateParams.Builder basicMessageCreateParams() {
        return MessageCreateParams.builder()
                .model(Model.CLAUDE_HAIKU_5_5)
                .maxTokens(1200L)
                .system("You are a nice welcoming assistant");
    }


}
