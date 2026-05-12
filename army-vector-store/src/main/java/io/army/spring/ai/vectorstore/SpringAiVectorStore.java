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
import io.army.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Map;

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
/// entity_class_name.IndexMeta[0].field_name.opclass=opclass
///
/// ${OPTIONAL} means that If you do not specify it, index will not be created.
@Table(name = "${DEFAULT}",
        indexes = @Index(name = "${DEFAULT}", type = "${OPTIONAL}", fields = @IndexField(name = "embedding", opclass = "${DEFAULT}")),
        comment = "${DEFAULT}")
public class SpringAiVectorStore {

    /// In Army, id are of type Object, allowing users to supply configurations at runtime via the property file:
    /// classpath:META-INF/army/TableMeta.properties:
    /// entity_class_name.field_name.class=classname
    /// entity_class_name.field_name.Column.name=column_name
    ///
    /// If you do not specify it, {@link Long} will be used as the id java type.
    @Column(name = "${DEFAULT}")
    private Object id;

    @Column(name = "${DEFAULT}")
    private LocalDateTime createTime;

    @Column(name = "${DEFAULT}")
    private LocalDateTime updateTime;

    @Column(name = "${DEFAULT}")
    private Integer version;

    @Column(name = "${DEFAULT}", comment = "${DEFAULT}")
    @Mapping("io.army.mapping.TextType")
    private String content;

    @Column(name = "${DEFAULT}", notNull = true, comment = "${DEFAULT}")
    @Mapping("io.army.mapping.PreferredJsonbType")
    private Map<String, Object> metadata;


    /// In Army, precision is {@link Integer#MIN_VALUE}, allowing users to supply configurations at runtime via the property file:
    /// classpath:META-INF/army/TableMeta.properties:
    /// entity_class_name.field_name.Column.precision=precision
    /// If you do not specify it, will throw {@link io.army.meta.MetaException}
    ///
    @Column(name = "${DEFAULT}", notNull = true, precision = Integer.MIN_VALUE, comment = "${DEFAULT}")
    @Mapping("io.army.mapping.optional.VectorType")
    private float[] embedding;


    public Object getId() {
        return id;
    }

    public SpringAiVectorStore setId(Object id) {
        this.id = id;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public SpringAiVectorStore setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public SpringAiVectorStore setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public SpringAiVectorStore setVersion(Integer version) {
        this.version = version;
        return this;
    }

    @Nullable
    public String getContent() {
        return content;
    }

    public SpringAiVectorStore setContent(@Nullable String content) {
        this.content = content;
        return this;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public SpringAiVectorStore setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public SpringAiVectorStore setEmbedding(float[] embedding) {
        this.embedding = embedding;
        return this;
    }

}
