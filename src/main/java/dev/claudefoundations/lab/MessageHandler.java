package dev.claudefoundations.lab;

import com.anthropic.models.messages.*;

import java.util.ArrayList;
import java.util.List;

public final class MessageHandler {

    public static MessageCreateParams createHelloParam(long maxTokens) {
        return MessageCreateParams.builder()
                .model(Model.CLAUDE_HAIKU_5_5)
                .maxTokens(maxTokens)
                .cacheControl(CacheControlEphemeral.builder().build())
                .system("You are nice welcoming assistant")
                .addUserMessage("Hello Claude")
                .build();
    }

    public static MessageCreateParams createParamsWithHistory(List<MessageParam> messages) {
        return basicMessageCreateParams().messages(messages).build();
    }

    public static List<MessageParam> slideWindow(
            List<MessageParam> messageParamList,
            long inputTokens,
            long contextWindow,
            long reservedOutputTokens
    ) {
        List<MessageParam> cleanUp = new ArrayList<>(messageParamList);

        if (!UsageHandler.isWithinTokenBudget(
                inputTokens, contextWindow, reservedOutputTokens)
                && cleanUp.size() > 2) {

            cleanUp.removeFirst();
            cleanUp.removeFirst();
        }

        return cleanUp;
    }

    private static MessageCreateParams.Builder basicMessageCreateParams() {
        return MessageCreateParams.builder()
                .model(Model.CLAUDE_HAIKU_5_5)
                .maxTokens(1200L)
                .system("You are a nice welcoming assistant");
    }


}
