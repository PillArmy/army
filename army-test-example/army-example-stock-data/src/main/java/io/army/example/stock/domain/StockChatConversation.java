package io.army.example.stock.domain;

import io.army.annotation.*;

import static io.army.generator.snowflake.Snowflake8Generator.START_TIME;


@Table(name = "stock_chat_conversation",
        indexes = {
                @Index(name = "${DEFAULT}", fieldList = {"userId"})
        },
        comment = "股票聊天会话")
public class StockChatConversation extends BaseDomain<StockChatConversation> {

    @Generator(value = SNOWFLAKE8, params = @Param(name = START_TIME, value = "1779012217056"))
    @Column
    private Long id;


    @Column(notNull = true, comment = "用户ID")
    private Long userId;

    @Column(notNull = true, precision = 30, comment = "会话标题")
    private String title;

    @Column(notNull = true, updatable = false, comment = "首次会话内容")
    @Mapping("io.army.mapping.TextType")
    private String firstContent;

    public Long getId() {
        return id;
    }

    public StockChatConversation setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public StockChatConversation setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public StockChatConversation setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getFirstContent() {
        return firstContent;
    }

    public StockChatConversation setFirstContent(String firstContent) {
        this.firstContent = firstContent;
        return this;
    }
}
