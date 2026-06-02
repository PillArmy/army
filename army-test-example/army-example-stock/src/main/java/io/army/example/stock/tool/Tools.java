package io.army.example.stock.tool;

import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;

/// Spring AI `@Tool` provider class exposing utility functions to the LLM agent.
///
/// <p>Currently provides a single tool for retrieving the current date and time,
/// which the LLM can invoke during chat conversations.</p>
public class Tools {

    public static final Tools INSTANCE = new Tools();

    public Tools() {
    }


    @Tool(description = "Get current date and time")
    public String localDateTime() {
        return LocalDateTime.now().toString();
    }


}
