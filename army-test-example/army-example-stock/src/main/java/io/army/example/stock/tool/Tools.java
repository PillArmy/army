package io.army.example.stock.tool;

import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;

public class Tools {

    public static final Tools INSTANCE = new Tools();

    public Tools() {
    }


    @Tool(description = "Get current date and time")
    public String localDateTime() {
        return LocalDateTime.now().toString();
    }


}
