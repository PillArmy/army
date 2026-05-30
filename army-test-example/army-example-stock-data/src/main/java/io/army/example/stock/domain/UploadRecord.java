package io.army.example.stock.domain;

import io.army.annotation.*;
import io.army.generator.snowflake.Snowflake8Generator;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Table(name = "upload_record",
        indexes = {
                @Index(name = "${DEFAULT_VALUE}", fieldList = "userId"),
                @Index(name = "${DEFAULT_VALUE}", fieldList = "fileHash")
        },
        comment = "用户上传文档记录")
public class UploadRecord extends BaseDomain<UploadRecord> {

    @Generator(value = SNOWFLAKE8, params = @Param(name = Snowflake8Generator.START_TIME, value = "1779137083969"))
    @Column
    public Long id;

    @Column(notNull = true, updatable = false, comment = "用户ID")
    public Long userId;

    @Column(notNull = true, updatable = false, comment = "会话ID")
    public Long conversationId;

    @Column(notNull = true, precision = 100, updatable = false, comment = "文件名")
    public String fileName;

    @Column(notNull = true, precision = 255, updatable = false, comment = "文件路径")
    public String filePath;

    @Column(precision = 44, comment = "文件哈希值,SHA‑256算法,标准base64")
    @Mapping("io.army.mapping.SqlCharType")
    public String fileHash;

    @Column(notNull = true, defaultValue = "${DEFAULT}", comment = "上传完成时间")
    public LocalDateTime uploadCompleteTime;

    @Column(notNull = true, defaultValue = "'[]'", comment = "向量ID列表")
    @Mapping("io.army.mapping.PreferredJsonbType")
    private List<Long> vectorIdList;

    @Column(notNull = true, defaultValue = "false", comment = "是否删除")
    public boolean deleted;

    // below not column field

    public Path storePath;

    public Long getId() {
        return id;
    }

    public UploadRecord setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public UploadRecord setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public UploadRecord setConversationId(Long conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public UploadRecord setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public UploadRecord setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public String getFileHash() {
        return fileHash;
    }

    public UploadRecord setFileHash(String fileHash) {
        this.fileHash = fileHash;
        return this;
    }

    public LocalDateTime getUploadCompleteTime() {
        return uploadCompleteTime;
    }

    public UploadRecord setUploadCompleteTime(LocalDateTime uploadCompleteTime) {
        this.uploadCompleteTime = uploadCompleteTime;
        return this;
    }

    public List<Long> getVectorIdList() {
        return vectorIdList;
    }

    public UploadRecord setVectorIdList(List<Long> vectorIdList) {
        this.vectorIdList = vectorIdList;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public UploadRecord setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public Path getStorePath() {
        return storePath;
    }

    public UploadRecord setStorePath(Path storePath) {
        this.storePath = storePath;
        return this;
    }
}
