package dev.claudefoundations.lab;

import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.Usage;

public final class UsageHandler {

    public static void printTokenUsage(Message message) {
        long input = message.usage().inputTokens();
        long output = message.usage().outputTokens();

        System.out.println("Input: " + input + "\nOutput: " + output);
    }

    public static void printCacheToken(Message message) {
        long cacheInput = message.usage().cacheCreationInputTokens().orElse(0L);
        long cacheRead = message.usage().cacheReadInputTokens().orElse(0L);

        System.out.println("Cache Input: " + cacheInput + "\nCache Read: " + cacheRead);
    }

    public static String printTotalCost(Message message,
                                        double inputPrice,
                                        double outputPrice,
                                        double cacheCreationPrice,
                                        double cacheReadPrice) {
        return String.format("Request cost: $%.6f%n", calculateCost(message, inputPrice, outputPrice, cacheCreationPrice, cacheReadPrice));
    }

    public static boolean isWithinTokenBudget(
            long inputTokens,
            long contextWindow,
            long reservedOutputTokens
    ) {
        return inputTokens <= contextWindow - reservedOutputTokens;
    }

    private static double calculateCost(
            Message message,
            double inputPrice,
            double outputPrice,
            double cacheCreationPrice,
            double cacheReadPrice
    ) {
        Usage usage = message.usage();

        double inputCost = usage.inputTokens() * inputPrice;
        double outputCost = usage.outputTokens() * outputPrice;

        double cacheCreationCost = usage.cacheCreationInputTokens()
                .orElse(0L) * cacheCreationPrice;

        double cacheReadCost = usage.cacheReadInputTokens()
                .orElse(0L) * cacheReadPrice;

        return (inputCost + outputCost + cacheCreationCost + cacheReadCost)
                / 1_000_000;
    }


}
