package io.army.example.coder.domain;

import io.army.annotation.*;


/// Domain entity representing a **stock chat conversation session**.
///
/// <p>Maps to the `stock_chat_conversation` table. Each conversation is owned by a user
/// and contains a truncated title (max 30 chars) auto-generated from the first message content.
/// The `firstContent` field is mapped to a `TEXT` column via `@Mapping("io.army.mapping.TextType")`
/// to store the initial user message.</p>
///
/// ### Index
/// - Non-unique index on `userId` for efficient user conversation listing
///
/// @see BaseDomain
@Table(name = "coder_chat_conversation",
        indexes = {
                @Index(name = "${DEFAULT_VALUE}", fieldList = {"userId"})
        },
        comment = "股票聊天会话")
public class CoderChatConversation extends BaseDomain<CoderChatConversation> {

    @Generator(value = SNOWFLAKE8, params = @Param(name = START_TIME, value = "1779012217056"))
    @Column
    private Long id;


    @Column(notNull = true, comment = "用户ID")
    private Long userId;

    @Column(notNull = true, precision = 30, comment = "会话标题")
    private String title;

    public Long getId() {
        return id;
    }

    public CoderChatConversation setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public CoderChatConversation setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CoderChatConversation setTitle(String title) {
        this.title = title;
        return this;
    }

}
