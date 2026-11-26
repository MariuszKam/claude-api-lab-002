package dev.claudefoundations.lab;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonField;
import com.anthropic.models.messages.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    static void main(String[] args) throws JsonProcessingException {

        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        MessageCreateParams helloClaude500Tokens = MessageHandler.createHelloParam(500L);

        MessageCreateParams byeClaude500Tokens = helloClaude500Tokens
                .toBuilder()
                .messages(List.of(MessageParam.ofUser("Bye Claude!")))
                .build();

        Message message = client.messages().create(helloClaude500Tokens);

        List<ContentBlock> content = message.content();

        content.stream()
                .filter(ContentBlock::isText)
                .map(ContentBlock::asText)
                .map(TextBlock::text)
                .forEach(System.out::println);

        message.stopReason().ifPresentOrElse(
                reason -> System.out.println(reason.asString()),
                        () -> System.out.println("No stop reason"));


        Usage usage = message.usage();

        System.out.println(usage.inputTokens());
        System.out.println(usage.outputTokens());

        String collect = message.content().stream()
                .filter(ContentBlock::isText)
                .map(ContentBlock::asText)
                .map(TextBlock::text)
                .collect(Collectors.joining());

        List<ToolUseBlock> toolUseBlockList = message.content().stream()
                .filter(ContentBlock::isToolUse)
                .map(ContentBlock::asToolUse)
                .toList();

        toolUseBlockList.stream()
                .map(ToolUseBlock::id)
                .forEach(System.out::println);

        toolUseBlockList.stream()
                .map(ToolUseBlock::name)
                .forEach(System.out::println);

        toolUseBlockList.stream()
                .map(ToolUseBlock::_input)
                .map(JsonField::asString)
                .forEach(System.out::println);

        List<String> thinking = MessageFilter.filterThinking(message);

        boolean isTextPresent = MessageFilter.isTextPresentInMessage(message);

        MessageParam messageParam = message.toParam();

        List<MessageParam> messageHistory = new ArrayList<>(helloClaude500Tokens.messages());

        messageHistory.add(messageParam);

        MessageParam messageNext = MessageParam.ofUser("How are you there?");

        messageHistory.add(messageNext);

        MessageCreateParams messageWithHistory = MessageHandler.createParamsWithHistory(messageHistory);

        client.messages().create(messageWithHistory);

    }
}
