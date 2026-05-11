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

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;

import java.util.List;

public final class ArmyVectorStore extends AbstractObservationVectorStore {


    public ArmyVectorStore(AbstractVectorStoreBuilder<?> builder) {
        super(builder);
    }


    @Override
    public void doAdd(List<Document> documents) {

    }

    @Override
    public void doDelete(List<String> idList) {

    }

    @Override
    public List<Document> doSimilaritySearch(SearchRequest request) {
        return List.of();
    }

    @Override
    public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {
        return null;
    }


}
