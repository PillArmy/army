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

import java.time.LocalDateTime;
import java.util.Map;


/// Base domain class for Spring AI vector store.
///
/// This class provides the foundation for storing vector-embedded documents in Army ORM.
/// It extends Army's default domain model with Spring AI-specific fields.
///
/// ### Fields:
/// - {@link #id} - Primary key
/// - {@link #createTime} - Creation timestamp
/// - {@link #updateTime} - Last update timestamp
/// - {@link #version} - Optimistic lock
/// - {@link #documentId} - Document identifier
/// - {@link #content} - Document content
/// - {@link #metadata} - Document metadata (JSONB)
/// - {@link #embedding} - Vector embedding
///
/// ### Usage:
/// ```java
/// @Table(name = "document_vector_store")
/// public class DocumentVectorStore extends SpringAiVectorStore {
///     // Inherits all fields from SpringAiVectorStore
/// }
/// ```
///
/// @see ArmyVectorStore
/// @see SpringAiChatVectorStore
@MappedSuperclass
public abstract class SpringAiVectorStore {


    /// Primary key.
    ///
    /// In Army, id can be configured at runtime via property file:
    /// ```
    /// entity_class_name.field_name.class=classname
    /// entity_class_name.field_name.Column.name=column_name
    /// ```
    /// If not specified, {@link Long} will be used.
    ///
    /// **Note:** Do not use {@link org.springframework.ai.document.Document#id}
    /// as the value of this field. The former is uncertain and represents bad design.
    @Generator(type = GeneratorType.DEFAULT)
    @Column(name = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private String id;

    /// Creation timestamp.
    @Column(name = "${DEFAULT}")
    private LocalDateTime createTime;

    /// Last update timestamp.
    @Column(name = "${DEFAULT}")
    private LocalDateTime updateTime;

    /// Optimistic lock version.
    @Column(name = "${DEFAULT}")
    private Integer version;

    /// Document identifier (unique).
    @Column(name = "${DEFAULT}", notNull = true, precision = 36, comment = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private String documentId;

    /// Document content.
    @Column(name = "${DEFAULT}", comment = "${DEFAULT}")
    @Mapping("io.army.mapping.TextType")
    private String content;

    /// Document metadata (JSONB format).
    @Column(name = "${DEFAULT}", notNull = true, defaultValue = "'{}'", comment = "${DEFAULT}")
    @Mapping("io.army.mapping.PreferredJsonbType")
    private Map<String, Object> metadata;


    /// Vector embedding.
    ///
    /// The precision (vector dimension) can be configured at runtime via property file:
    /// ```
    /// entity_class_name.field_name.Column.precision=precision
    /// ```
    /// If not specified, will throw {@link io.army.meta.MetaException}.
    @Column(name = "${DEFAULT}", notNull = true, precision = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    @Mapping("io.army.mapping.VectorType")
    private float[] embedding;

    public String getId() {
        return id;
    }

    public SpringAiVectorStore setId(String id) {
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

    public String getDocumentId() {
        return documentId;
    }

    public SpringAiVectorStore setDocumentId(String documentId) {
        this.documentId = documentId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public SpringAiVectorStore setContent(String content) {
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
