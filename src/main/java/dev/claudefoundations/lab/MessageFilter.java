package dev.claudefoundations.lab;

import com.anthropic.models.messages.*;

import java.util.List;
import java.util.stream.Collectors;

public final class MessageFilter {


    public static String filterMessages(Message message) {
        return message.content().stream()
                .filter(ContentBlock::isText)
                .map(ContentBlock::asText)
                .map(TextBlock::text)
                .collect(Collectors.joining());
    }

    public static List<String> filterThinking(Message message) {
        return message.content().stream()
                .filter(ContentBlock::isThinking)
                .map(ContentBlock::asThinking)
                .map(ThinkingBlock::thinking)
                .toList();
    }

    public static List<ToolUseBlock> filterToolUse(Message message) {
        return message.content().stream()
                .filter(ContentBlock::isToolUse)
                .map(ContentBlock::asToolUse)
                .toList();
    }

    public static boolean isTextPresentInMessage(Message message) {
        return message.content().stream()
                .anyMatch(ContentBlock::isText);
    }
}
