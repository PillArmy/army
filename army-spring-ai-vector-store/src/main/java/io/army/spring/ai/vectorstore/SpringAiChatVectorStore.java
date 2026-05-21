/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.spring.ai.vectorstore;

import io.army.annotation.*;


/// In Army, {@code @Table(name = "${DEFAULT}", comment = "${DEFAULT}")} allowing users to supply configurations at runtime via the property file:
/// classpath:META-INF/army/TableMeta.properties:
/// entity_class_name.Table.name=table_name
/// entity_class_name.Table.comment=table_comment
///
/// If you do not specify it, simple entity class name will be used as the table name(table comment) after converting.
///
///
/// {@code @Index(name = "${DEFAULT}", type = "${OPTIONAL}", fields = @IndexField(name = "embedding", opclass = "${DEFAULT}"))}
/// allowing users to supply configurations at runtime via the property file:
/// classpath:META-INF/army/TableMeta.properties:
/// entity_class_name.IndexMeta[0].name=index_name
/// entity_class_name.IndexMeta[0].type=index_type
/// entity_class_name.IndexMeta[0].field.field_name.opclass=opclass
///
/// ${OPTIONAL} means that If you do not specify it, index will not be created.

@Table(name = "${DEFAULT}",
        indexes = {
                @Index(name = "${DEFAULT}", type = "${RUNTIME}", fields = @IndexField(name = "embedding", opclass = "${DEFAULT}")),
                @Index(name = "${DEFAULT}", type = "${DEFAULT}", fieldList = "conversationId")
        },
        ddlMode = DdlMode.DEFAULT,
        comment = "${DEFAULT}")
public non-sealed class SpringAiChatVectorStore extends SpringAiVectorStore {

    /// {@link Mapping#value()} should be one of below:
    /// - {@link io.army.mapping.SqlBigIntType}
    /// - {@link io.army.mapping.StringType}
    /// - {@link io.army.mapping.UUIDType}
    ///
    /// If the entity is used to store long-term chat session memory, the conversationId field is required; otherwise,
    ///  it is unnecessary and shall not be configurable.
    ///
    /// @see org.springframework.ai.chat.memory.ChatMemory#CONVERSATION_ID
    @Column(name = "${DEFAULT}", notNull = true, defaultValue = "${DEFAULT}", precision = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private String conversationId;


    public String getConversationId() {
        return conversationId;
    }

    public SpringAiChatVectorStore setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }


}
