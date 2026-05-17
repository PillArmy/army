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

package io.army.spring.sync;

import io.army.dao.SyncBaseDao;
import io.army.spring.ArmyTransactionTemplate;
import org.jspecify.annotations.Nullable;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;

public abstract class ArmySyncBaseService<D extends SyncBaseDao> implements SyncBaseService {

    protected final ArmyTransactionTemplate transactionTemplate;

    public ArmySyncBaseService(ArmyTransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }


    @Override
    public <T> void save(T domain) {
        this.transactionTemplate.executeWithoutResult(Isolation.READ_COMMITTED, false,
                _ -> getDao().save(domain)
        );
    }

    @Override
    public <T> void batchSave(List<T> domainList) {
        this.transactionTemplate.executeWithoutResult(Isolation.READ_COMMITTED, false,
                _ -> getDao().batchSave(domainList)
        );
    }

    @Nullable
    @Override
    public <T> T get(Class<T> domainClass, Object id) {
        return this.transactionTemplate.execute(Isolation.READ_COMMITTED, true,
                _ -> getDao().get(domainClass, id)
        );
    }

    @Nullable
    @Override
    public <T> T getByUnique(Class<T> domainClass, String fieldName, Object fieldValue) {
        return this.transactionTemplate.execute(Isolation.READ_COMMITTED, true,
                _ -> getDao().getByUnique(domainClass, fieldName, fieldValue)
        );
    }

    @Nullable
    @Override
    public <T> T findById(Class<T> domainClass, Object id) {
        return this.transactionTemplate.execute(Isolation.READ_COMMITTED, true,
                _ -> getDao().findById(domainClass, id)
        );
    }

    @Nullable
    @Override
    public <T> T findByUnique(Class<T> domainClass, String fieldName, Object fieldValue) {
        return this.transactionTemplate.execute(Isolation.READ_COMMITTED, true,
                _ -> getDao().findByUnique(domainClass, fieldName, fieldValue)
        );
    }


    @Override
    public <T> long rowCount(Class<T> domainClass) {
        final Long rowCont;
        rowCont = this.transactionTemplate.execute(Isolation.READ_COMMITTED, true,
                _ -> getDao().rowCount(domainClass)
        );
        assert rowCont != null;
        return rowCont;
    }

    @Override
    public <T> int rowCountAsInt(Class<T> domainClass) {
        final Integer rowCont;
        rowCont = this.transactionTemplate.execute(Isolation.READ_COMMITTED, true,
                _ -> getDao().rowCountAsInt(domainClass)
        );
        assert rowCont != null;
        return rowCont;
    }

    protected abstract D getDao();


}
