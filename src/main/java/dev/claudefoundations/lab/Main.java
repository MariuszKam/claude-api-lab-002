package dev.claudefoundations.lab;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.List;

public class Main {
    static void main(String[] args) throws JsonProcessingException {

        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        MessageCreateParams helloClaude500Tokens = MessageCreator.createHelloParam(500L);

        Message message = client.messages().create(helloClaude500Tokens);

        MessageParam messageParam = message.toParam();

        List<MessageParam> messageHistory = new ArrayList<>(helloClaude500Tokens.messages());

        messageHistory.add(messageParam);

        MessageParam messageNext = MessageParam.ofUser("How are you there?");

        messageHistory.add(messageNext);

        MessageCreateParams messageWithHistory = MessageCreator.createParamsWithHistory(messageHistory);

        client.messages().create(messageWithHistory);

    }
}
