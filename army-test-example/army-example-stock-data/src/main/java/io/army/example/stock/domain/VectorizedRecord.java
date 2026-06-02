package io.army.example.stock.domain;

import io.army.annotation.*;
import io.army.generator.snowflake.Snowflake8Generator;

/// Domain entity tracking files that have been **vectorized** into embeddings.
///
/// <p>Maps to the `vectorized_record` table. Used for deduplication: before vectorizing
/// an uploaded document, the system checks whether its SHA-256 hash already exists
/// in this table to avoid redundant processing.</p>
///
/// <p>This table is marked as **immutable** — records are insert-only and never updated.</p>
///
/// @see MinBaseDomain
@Table(name = "vectorized_record", indexes = {
        @Index(name = "${DEFAULT_VALUE}", unique = true, fieldList = {"fileHash"})
},
        immutable = true,
        comment = "文件已向量化的记录")
public class VectorizedRecord extends MinBaseDomain<VectorizedRecord> {

    public static final long SNOWFLAKE_START_TIME = 1779201880496L;

    @Generator(value = SNOWFLAKE8, params = @Param(name = Snowflake8Generator.START_TIME, value = "" + SNOWFLAKE_START_TIME))
    @Column
    public long id;

    @Column(notNull = true, precision = 100, updatable = false, comment = "文件名")
    public String fileName;

    @Column(notNull = true, updatable = false, precision = 44, comment = "文件哈希值,SHA‑256算法,标准base64")
    @Mapping("io.army.mapping.SqlCharType")
    public String fileHash;


    public long getId() {
        return id;
    }

    public VectorizedRecord setId(long id) {
        this.id = id;
        return this;
    }

    public String getFileHash() {
        return fileHash;
    }

    public VectorizedRecord setFileHash(String fileHash) {
        this.fileHash = fileHash;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public VectorizedRecord setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
}
