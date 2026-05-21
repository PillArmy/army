package io.army.example.stock.service.impl;

import io.army.example.stock.dao.StockBaseDao;
import io.army.example.stock.dao.UploadRecordDao;
import io.army.example.stock.domain.UploadRecord;
import io.army.example.stock.domain.VectorizedRecord;
import io.army.example.stock.service.DocumentService;
import io.army.example.stock.utils.CollectionUtils;
import io.army.example.stock.utils.FileUtils;
import io.army.generator.snowflake.Snowflake8s;
import io.army.spring.TransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DocumentServiceImpl extends AbstractStockBaseService implements DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final UploadRecordDao uploadRecordDao;

    private final VectorStore vectorStore;

    public DocumentServiceImpl(TransactionTemplate transactionTemplate, UploadRecordDao uploadRecordDao,
                               @Qualifier("stockDocumentVectorStore") VectorStore vectorStore) {
        super(transactionTemplate);
        this.uploadRecordDao = uploadRecordDao;
        this.vectorStore = vectorStore;
    }

    @Transactional(propagation = Propagation.NEVER)
    @Override
    public void storeDocuments(List<UploadRecord> recordList) {
        try {
            final int size = recordList.size();
            Path path;
            String fileType;
            UploadRecord o;
            List<Long> vectorIdList;
            for (int i = 0; i < size; i++, deleteFileIfExits(o)) {
                o = recordList.get(i);
                path = o.storePath;
                if (path == null || Files.notExists(path)) {
                    continue;
                }

                fileType = FileUtils.fileTypeName(path);
                if (fileType == null) {
                    continue;
                }
                if (existsByByField(VectorizedRecord.class, "fileHash", o.fileHash)) {
                    continue;
                }
                if (fileType.equalsIgnoreCase("pdf")) {
                    vectorIdList = addPdfVector(o);
                    updateField(UploadRecord.class, o.id, "vectorIdList", vectorIdList);
                }
            }
        } catch (Throwable e) {
            LOG.error("storeDocuments error", e);
        }


    }


    @Override
    protected StockBaseDao getDao() {
        return this.uploadRecordDao;
    }


    private List<Long> addPdfVector(UploadRecord o) {

        final PdfDocumentReaderConfig readerConfig;
        readerConfig = PdfDocumentReaderConfig
                .builder()
                .withPagesPerDocument(1)
                .withPageTopMargin(30)
                .withPageBottomMargin(50)
                .build();

        final PagePdfDocumentReader reader;
        reader = new PagePdfDocumentReader(new FileSystemResource(o.storePath), readerConfig);


        final Document.Builder builder = Document
                .builder();

        final List<Document> documentList, myDocumentList;
        documentList = reader.read();
        myDocumentList = new ArrayList<>(documentList.size());

        final List<Long> vectorIdList = new ArrayList<>(documentList.size());

        Document myDoc;
        Map<String, Object> metadata;
        long vectorId;
        for (Document document : documentList) {
            metadata = CollectionUtils.newHashMap(document.getMetadata().size());
            metadata.putAll(document.getMetadata());
            metadata.put("SHA-256", o.fileHash);
            metadata.put("fileName", o.fileName);

            vectorId = Snowflake8s.next(VectorizedRecord.SNOWFLAKE_START_TIME);
            vectorIdList.add(vectorId);

            myDoc = builder
                    .id(Long.toString(vectorId))
                    .text(document.getText())
                    .metadata(metadata)
                    .score(document.getScore())
                    .media(document.getMedia())
                    .build();

            myDocumentList.add(myDoc);

        }

        this.vectorStore.add(myDocumentList);

        final VectorizedRecord vectorizedRecord = new VectorizedRecord()
                .setFileName(o.fileName)
                .setFileHash(o.fileHash);

        save(vectorizedRecord);

        return vectorIdList;
    }

    private void deleteFileIfExits(UploadRecord o) {
        FileUtils.deleteIfExists(o.storePath);
        updateFieldWhenMatch(UploadRecord.class, o.id, "deleted", Boolean.TRUE, Boolean.FALSE);
    }


}
