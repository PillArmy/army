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

package io.army.modelgen;

import io.army.annotation.Table;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/// Main annotation processor.
@SupportedAnnotationTypes("io.army.annotation.Table")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class ArmyMetaModelDomainProcessor extends AbstractProcessor {


    private static final boolean ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS = false;

    private ProcessingEnvironment processingEnv;

    // private Messager messager;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<? extends Element> elementSet;
        elementSet = roundEnv.getElementsAnnotatedWith(Table.class);

        final int domainSetSize;
        if ((domainSetSize = elementSet.size()) == 0) {
            return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
        }

        final long startTime = System.currentTimeMillis();
        try {
            final StringBuilder tempBuilder = new StringBuilder(30);

            final TableAnnotationHandler handler;
            handler = TableAnnotationHandlerImpl.create(this.processingEnv, tempBuilder);

            generateTableStaticModelClass(elementSet, handler);

            final List<String> errorMsgList = handler.getErrorMessages();

            if (!errorMsgList.isEmpty()) {
                final String m, title;
                title = "handle army annotation occur error,detail:";
                m = _MetaBridge.createErrorMessage(title, errorMsgList);
                throw new AnnotationMetaException(m);
            }


            writeClassFiles(this.processingEnv.getFiler(), handler.compositeSourceList());

            handler.endHandle();

        } catch (AnnotationMetaException e) {
            throw e;
        } catch (Exception e) {
            throw new AnnotationMetaException("Army create source file occur.", e);
        }
        String msg = String.format("%s generate %s army static metamodel class source file, take %s ms.%n",
                ArmyMetaModelDomainProcessor.class.getName(),
                domainSetSize,
                System.currentTimeMillis() - startTime);

        // this.messager.printMessage(Diagnostic.Kind.NOTE, msg);
        System.out.printf("[%sINFO%s] %s", "\u001B[34m", "\u001B[0m", msg);
        return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
    }


    private void generateTableStaticModelClass(Set<? extends Element> elementSet, TableAnnotationHandler handler)
            throws IOException {

        final Filer filer = this.processingEnv.getFiler();

        final List<Pair> pairList = new ArrayList<>(50);

        int count = 0;
        Pair pair;
        for (Element element : elementSet) {
            pair = handler.handle((TypeElement) element);
            if (pair == null) {
                continue;
            }

            pairList.add(pair);

            count++;

            if (count < 50) {
                continue;
            }

            writeClassFiles(filer, pairList);
            pairList.clear();
            count = 0;

        } // loop

        if (!pairList.isEmpty()) {
            writeClassFiles(filer, pairList);
            pairList.clear();
        }


    }


    private static void writeClassFiles(Filer filer, final List<Pair> pairList) throws IOException {

        for (Pair pair : pairList) {

            writeOneClassFile(pair.content, filer.createSourceFile(pair.className));

        } // loop

    }

    private static void writeOneClassFile(CharSequence content, FileObject fileObject) throws IOException {
        try (PrintWriter pw = new PrintWriter(fileObject.openOutputStream())) {
            pw.print(content);
        }
    }


}
