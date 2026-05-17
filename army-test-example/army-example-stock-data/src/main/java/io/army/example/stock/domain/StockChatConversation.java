package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.Generator;
import io.army.annotation.Param;
import io.army.annotation.Table;

import static io.army.generator.snowflake.Snowflake8Generator.START_TIME;


@Table(name = "stock_chat_conversation", comment = "股票聊天会话")
public class StockChatConversation extends BaseDomain<StockChatConversation> {

    @Generator(value = SNOWFLAKE8, params = @Param(name = START_TIME, value = "1779012217056"))
    @Column
    private long id;

    @Column(notNull = true, precision = 30, comment = "会话标题")
    private String title;


    public long getId() {
        return id;
    }

    public StockChatConversation setId(long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public StockChatConversation setTitle(String title) {
        this.title = title;
        return this;
    }


}
