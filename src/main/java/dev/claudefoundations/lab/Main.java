package dev.claudefoundations.lab;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;

public class Main {
    static void main(String[] args) {
        System.out.println("Claude API Lab 002");
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

    }
}
