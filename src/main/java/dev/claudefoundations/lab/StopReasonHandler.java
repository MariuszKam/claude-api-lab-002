package dev.claudefoundations.lab;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.*;

import java.util.ArrayList;
import java.util.List;

public final class StopReasonHandler {

    private static final int MAX_RETRIES = 3;

    public static Message handleStopReason(
            AnthropicClient client,
            MessageCreateParams originalParams,
            Message message
    ) {
        StopReason stopReason = message.stopReason().orElseThrow();

        switch (stopReason.value()) {
            case END_TURN -> {
                return message;
            }
            case MAX_TOKENS -> {
                return handleMaxToken(client, originalParams, message);
            }
            case TOOL_USE -> {
                return handleToolUse(client, originalParams, message);
            }
            case STOP_SEQUENCE -> {
                return handleStopSequence(message);
            }
            case PAUSE_TURN -> {
                return handlePauseTurn(client, originalParams, message);
            }
            case REFUSAL -> {
                return handleRefusal(message);
            }
            case MODEL_CONTEXT_WINDOW_EXCEEDED -> {
                return handleModelContextWindowExceeded(message);
            }
            default -> {
                return message;
            }
        }

    }

    private static Message handleModelContextWindowExceeded(Message message) {
        System.out.println("Context window exceeded");
        return message;
    }

    private static Message handleRefusal(Message message) {
        RefusalStopDetails refusalStopDetails = message.stopDetails().orElseThrow();
        String string = refusalStopDetails.explanation().orElse("No explanation");
        System.out.println(string);
        return message;
    }

    private static Message handlePauseTurn(AnthropicClient client, MessageCreateParams originalParams, Message message) {
        List<MessageParam> messages = new ArrayList<>(originalParams.messages());

        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            messages.add(message.toParam());

            MessageCreateParams currentParams = originalParams.toBuilder()
                    .messages(messages)
                    .build();

            message = client.messages().create(currentParams);

            if (!isPauseTurn(message)) {
                break;
            }

        }

        return message;
    }

    private static Message handleMaxToken(AnthropicClient client,
                                          MessageCreateParams originalParams,
                                          Message message) {

        MessageCreateParams currentParams = originalParams;

        for (int retry = 0; retry < MAX_RETRIES; retry++) {

            if (currentParams.maxTokens() >= 2000) {
                return message;
            }

            currentParams = currentParams.toBuilder()
                    .maxTokens(Math.min(currentParams.maxTokens() + 500L, 2000L))
                    .build();

            message = client.messages().create(currentParams);

            if (!isMaxTokens(message)) {
                break;
            }

        }

        return message;
    }

    private static Message handleToolUse(AnthropicClient client,
                                         MessageCreateParams originalParams,
                                         Message message) {

        List<ToolUseBlock> toolUseBlockList = MessageFilter.filterToolUse(message);
        List<MessageParam> messages = new ArrayList<>(originalParams.messages());
        messages.add(message.toParam());


        List<ToolResultBlockParam> toolResultBlockParamList = new ArrayList<>();
        for (ToolUseBlock toolUseBlock : toolUseBlockList) {
            ToolResultBlockParam toolResultBlockParam = ToolResultBlockParam
                    .builder()
                    .toolUseId(toolUseBlock.id())
                    .content("Tool executed successfully")
                    .build();
            toolResultBlockParamList.add(toolResultBlockParam);
        }

        MessageParam toolResult = MessageParam.ofUserOfContentBlockParams(toolResultBlockParamList.stream()
                .map(ContentBlockParam::ofToolResult)
                .toList());

        messages.add(toolResult);

        MessageCreateParams currentParams = originalParams.toBuilder().messages(messages).build();

        return client.messages().create(currentParams);

    }

    private static Message handleStopSequence(Message message) {
        String string = message.stopSequence().orElseThrow();
        System.out.println("Stop sequence: " + string);
        return message;
    }


    private static boolean isMaxTokens(Message message) {
        return message.stopReason()
                .map(reason -> reason.equals(StopReason.MAX_TOKENS))
                .orElse(false);
    }

    private static boolean isPauseTurn(Message message) {
        return message.stopReason()
                .map(reason -> reason.equals(StopReason.PAUSE_TURN))
                .orElse(false);
    }


}
