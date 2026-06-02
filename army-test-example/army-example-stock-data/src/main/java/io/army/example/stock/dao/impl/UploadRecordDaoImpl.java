package io.army.example.stock.dao.impl;

import io.army.criteria.Update;
import io.army.criteria.impl.SQLs;
import io.army.example.stock.dao.UploadRecordDao;
import io.army.example.stock.domain.UploadRecord_;
import io.army.example.stock.util.StringUtils;
import io.army.session.SyncSessionContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import static io.army.criteria.impl.SQLs.AS;

/// Implementation of `UploadRecordDao` using Army's type-safe criteria API.
///
/// <p>The `uploadComplete` method atomically updates the record with the file hash
/// and completion timestamp, guarded by a `fileHash IS NULL` condition to prevent
/// double-completion.</p>
@Repository("uploadRecordDao")
public class UploadRecordDaoImpl extends ArmyStockBaseDao implements UploadRecordDao {


    public UploadRecordDaoImpl(SyncSessionContext sessionContext) {
        super(sessionContext);
    }


    @Override
    public long uploadComplete(long id, String fileHast) {
        if (!StringUtils.hasText(fileHast) || fileHast.length() != UploadRecord_.fileHash.precision()) {
            throw new IllegalArgumentException("fileHash must have text and length must be 44");
        }
        final Update stmt;
        stmt = SQLs.singleUpdate()
                .update(UploadRecord_.T, AS, "t")
                .set(UploadRecord_.uploadCompleteTime, LocalDateTime.now())
                .set(UploadRecord_.fileHash, fileHast)
                .where(UploadRecord_.id.equal(id))
                .and(UploadRecord_.fileHash.isNull())
                .asUpdate();
        return this.sessionContext.currentSession().update(stmt);
    }


}
