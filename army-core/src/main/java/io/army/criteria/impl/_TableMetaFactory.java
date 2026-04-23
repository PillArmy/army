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

package io.army.criteria.impl;

import io.army.annotation.Table;
import io.army.lang.Nullable;
import io.army.meta.*;
import io.army.modelgen.ArmyMetaModelDomainProcessor;
import io.army.modelgen._MetaBridge;
import io.army.util._Collections;
import io.army.util._StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public abstract class _TableMetaFactory {

    private _TableMetaFactory() {
        throw new UnsupportedOperationException();
    }

    public static <T> SimpleTableMeta<T> getSimpleTableMeta(final Class<T> domainClass) {
        return DefaultTableMeta.getSimpleTableMeta(domainClass);
    }

    public static <T> ParentTableMeta<T> getParentTableMeta(final Class<T> domainClass) {
        return DefaultTableMeta.getParentTableMeta(domainClass);
    }

    public static <P, T> ComplexTableMeta<P, T> getChildTableMeta(
            ParentTableMeta<P> parent, Class<T> domainClass) {
        return DefaultTableMeta.getChildTableMeta(parent, domainClass);
    }


    /**
     * @return an unmodifiable map.
     */
    public static Map<Class<?>, TableMeta<?>> getTableMetaMap(final SchemaMeta schemaMeta,
                                                              final List<String> basePackages,
                                                              final boolean loadStaticModel,
                                                              final @Nullable Consumer<TableMeta<?>> consumer,
                                                              @Nullable ClassLoader classLoader)
            throws TableMetaLoadException {

        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        synchronized (DefaultTableMeta.LOCK) {
            URL url = null;
            try {
                final Map<Class<?>, TableMeta<?>> tableMetaMap = _Collections.hashMap();
                final ClassFile classFile = ClassFile.of();
                String protocol;
                Enumeration<URL> enumeration;
                for (String basePackage : basePackages) {
                    if (!_StringUtils.hasText(basePackage)) {
                        throw new IllegalArgumentException("basePackage must have text.");
                    }
                    //1. convert base package
                    if (basePackage.indexOf('.') > 0) {
                        basePackage = basePackage.replace('.', '/');
                    }
                    // 2. get url from base package

                    enumeration = classLoader.getResources(basePackage);

                    // 3. scan java class file in base package for get TableMeta.
                    while (enumeration.hasMoreElements()) {
                        url = enumeration.nextElement();
                        protocol = url.getProtocol();
                        try (Stream<ByteBuffer> stream = createJavaClassByteStream(protocol, url)) {
                            stream.map(buffer -> readJavaClassFile(classFile, buffer, schemaMeta)) // read java class file and get class name if match.
                                    .filter(_StringUtils::hasText) // if empty string ,not domain class
                                    .map(_TableMetaFactory::getOrCreateTableMeta)// get or create table meta
                                    .forEach(tableMeta -> {
                                        final Class<?> domainClass = tableMeta.javaType();
                                        tableMetaMap.put(domainClass, tableMeta);

                                        if (loadStaticModel) {
                                            final TableMeta<?> t;
                                            t = loadDomainMetaHolder(domainClass);
                                            if (t != tableMeta) {
                                                String m = String.format("%s of %s create occur error", TableMeta.class.getSimpleName(), domainClass.getName());
                                                throw new MetaException(m);
                                            }
                                        } // loadStaticModel

                                        if (consumer != null) {
                                            consumer.accept(tableMeta);
                                        }
                                    });
                        } // try
                    } // while
                    url = null;
                }
                return Map.copyOf(tableMetaMap);
            } catch (TableMetaLoadException e) {
                throw e;
            } catch (Exception e) {
                String m;
                if (url == null) {
                    m = e.getMessage();
                } else {
                    m = String.format("url[%s] scan occur error: %s .", url, e.getMessage());
                }
                throw new TableMetaLoadException(m, e);
            } finally {
                TableMetaUtils.clearCache();
            }

        } // synchronized

    }


    private static Stream<ByteBuffer> createJavaClassByteStream(String protocol, URL url) throws IOException {
        final Stream<ByteBuffer> stream;
        final String jarProtocol = "jar", fileProtocol = "file";
        if (jarProtocol.equals(protocol)) {
            stream = scanJavaJarForJavaClassFile(url);
        } else if (fileProtocol.equals(protocol)) {
            stream = Files.find(Paths.get(url.getPath()), Integer.MAX_VALUE, _TableMetaFactory::isJavaClassFile)
                    .map(_TableMetaFactory::readJavaClassFileBytes);
        } else {
            String m = String.format("url[%s] unsupported", url);
            throw new IllegalArgumentException(m);
        }
        return stream;
    }


    public static Set<FieldMeta<?>> codecFieldMetaSet() {
        return TableFieldMeta.codecFieldMetaSet();
    }

    public static IllegalStateException tableFiledSizeError(Class<?> domainClass, int fieldSize) {
        String m = String.format("Domain[%s] field count[%s] error,please check you whether create(delete) field or not,if yes then you must recompile.",
                domainClass.getName(), fieldSize);
        return new IllegalStateException(m);
    }


    /*################################## blow private method ##################################*/

    /**
     * @see #getTableMetaMap(SchemaMeta, List, boolean, Consumer, ClassLoader)
     */
    private static Stream<ByteBuffer> scanJavaJarForJavaClassFile(final URL url) {
        try {
            final URLConnection conn = url.openConnection();
            if (!(conn instanceof JarURLConnection jarConn)) {
                String m = String.format("url[%s] can' open %s .", url, JarURLConnection.class.getName());
                throw new IllegalArgumentException(m);
            }
            final String rootEntryName = jarConn.getEntryName();
            final JarFile jarFile = jarConn.getJarFile();
            return jarFile
                    .stream()
                    .filter(entry -> isJavaClassEntry(rootEntryName, entry))
                    .map(entry -> readJavaClassEntryBytes(jarFile, entry));
        } catch (IOException e) {
            String m = String.format("jar[%s] scan occur error:%s", url, e.getMessage());
            throw new TableMetaLoadException(m, e);
        }
    }


    /**
     * @see #getTableMetaMap(SchemaMeta, List, boolean, Consumer, ClassLoader)
     */
    private static ByteBuffer readJavaClassFileBytes(final Path classFilePath) {
        try (FileChannel channel = FileChannel.open(classFilePath, StandardOpenOption.READ)) {
            final long fileSize;
            fileSize = channel.size();
            if (fileSize > (Integer.MAX_VALUE - 32)) {
                String m = String.format("Class file[%s] too large,don't support read.", classFilePath);
                throw new IllegalArgumentException(m);
            }
            final ByteBuffer buffer = ByteBuffer.wrap(new byte[(int) fileSize]);
            if (channel.read(buffer) < 10) {
                throw classFileFormatError();
            }
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            String m = String.format("class file[%s] read occur error:%s", classFilePath, e.getMessage());
            throw new TableMetaLoadException(m, e);
        }
    }

    /**
     * @throws TableMetaLoadException when not found table meta holder class of domainClass.
     * @see #getTableMetaMap(SchemaMeta, List, boolean, Consumer, ClassLoader)
     */
    private static TableMeta<?> loadDomainMetaHolder(final Class<?> domainClass) {

        final Class<?> holderClass;
        try {
            holderClass = Class.forName(domainClass.getName() + _MetaBridge.META_CLASS_NAME_SUFFIX);
        } catch (ClassNotFoundException e) {
            String m = String.format("You compile %s without %s", domainClass.getName()
                    , ArmyMetaModelDomainProcessor.class.getName());
            throw new TableMetaLoadException(m, e);
        }

        try {
            final Field field;
            field = holderClass.getDeclaredField(_MetaBridge.TABLE_META);
            final int modifier = field.getModifiers();
            final Object value;
            if (!Modifier.isPublic(modifier)
                    || !Modifier.isStatic(modifier)
                    || !Modifier.isFinal(modifier)
                    || !((value = field.get(null)) instanceof TableMeta<?>)) {
                String m = String.format("static model class[%s] error", holderClass.getName());
                throw new MetaException(m);
            }

            return (TableMeta<?>) value;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            String m = String.format("static model class[%s] error", holderClass.getName());
            throw new MetaException(m);
        }

    }

    /**
     * @throws TableMetaLoadException when occur {@link IOException}
     * @see #scanJavaJarForJavaClassFile(URL)
     */
    private static ByteBuffer readJavaClassEntryBytes(final JarFile jarFile, final JarEntry entry) {
        final long entrySize = entry.getSize();
        if (entrySize > (Integer.MAX_VALUE - 32)) {
            String m = String.format("Class file[%s] too large,don't support read.", entry.getName());
            throw new IllegalArgumentException(m);
        }
        try (InputStream in = jarFile.getInputStream(entry);
             ByteArrayOutputStream out = new ByteArrayOutputStream((int) entrySize)) {
            final byte[] bufferArray = new byte[(int) Math.min(2048, entrySize)];
            int length;
            while ((length = in.read(bufferArray)) > 0) {
                out.write(bufferArray, 0, length);
            }
            return ByteBuffer.wrap(out.toByteArray());
        } catch (IOException e) {
            String m = String.format("Jar class file[%s] read occur error:%s", entry.getName(), e.getMessage());
            throw new TableMetaLoadException(m, e);
        }
    }

    /**
     * @see #scanJavaJarForJavaClassFile(URL)
     */
    private static boolean isJavaClassEntry(final String rootEntryName, final JarEntry entry) {
        final String entryName = entry.getName();
        return entryName.startsWith(rootEntryName)
                && !entry.isDirectory()
                && entryName.endsWith(".class");
    }


    /**
     * @see #getTableMetaMap(SchemaMeta, List, boolean, Consumer, ClassLoader)
     */
    private static boolean isJavaClassFile(final Path path, BasicFileAttributes attributes) {
        return !Files.isDirectory(path)
                && Files.isReadable(path)
                && path.getFileName().toString().endsWith(".class");
    }

    /**
     * @see #getTableMetaMap(SchemaMeta, List, boolean, Consumer, ClassLoader)
     */
    private static <T> TableMeta<T> getOrCreateTableMeta(final String className) {
        try {
            final Class<?> clazz;
            clazz = Class.forName(className);
            @SuppressWarnings("unchecked")
            Class<T> domainClass = (Class<T>) clazz;
            return DefaultTableMeta.getTableMeta(domainClass);
        } catch (ClassNotFoundException e) {
            // no bug,never here.
            String m = String.format("Domain class[%s] not found.", className);
            throw new TableMetaLoadException(m, e);
        }

    }


    private static String readJavaClassFile(ClassFile classFile, final ByteBuffer buffer, final SchemaMeta schemaMeta) {
        final byte[] classBytes;
        if (buffer.isReadOnly()) {
            classBytes = new byte[buffer.remaining()];
            buffer.get(classBytes, 0, classBytes.length);
        } else {
            classBytes = buffer.array();
        }

        final String tableAnnoName = Table.class.getName();
        final String targetCatalog = schemaMeta.catalog(), targetSchema = schemaMeta.schema();

        final ClassModel classModel;
        classModel = classFile.parse(classBytes);

        String annotationName, className = "";
        top:
        for (Attribute<?> attribute : classModel.attributes()) {
            if (!(attribute instanceof RuntimeVisibleAnnotationsAttribute attr)) {
                continue;
            }
            for (Annotation annotation : attr.annotations()) {
                annotationName = annotation.className().stringValue();
                annotationName = annotationName.substring(1, annotationName.length() - 1).replace('/', '.');
                if (!tableAnnoName.equals(annotationName)) {
                    continue;
                }
                String catalog = null, schema = null;
                for (AnnotationElement element : annotation.elements()) {
                    if ("catalog".equals(element.name().stringValue())) {
                        catalog = element.value().toString();
                    } else if ("schema".equals(element.name().stringValue())) {
                        schema = element.value().toString();
                    }
                    if (catalog != null && schema != null) {
                        break;
                    }
                } // for (AnnotationElement element : annotation.elements())

                final boolean catalogMatch, schemaMatch;
                catalogMatch = (catalog == null && targetCatalog.isEmpty())
                        || targetCatalog.equals(_StringUtils.toLowerCaseIfNonNull(catalog));
                schemaMatch = (schema == null && targetSchema.isEmpty())
                        || targetSchema.equals(_StringUtils.toLowerCaseIfNonNull(schema));
                if (catalogMatch && schemaMatch) {
                    className = classModel.thisClass().name().stringValue();
                    className = className.replace('/', '.');
                }
                break top;

            } // for (Annotation annotation : attr.annotations())
        } // for (Attribute<?> attribute : classFile.parse(classBytes).attributes())

        return className;
    }


    private static IllegalArgumentException classFileFormatError() {
        return new IllegalArgumentException("class file format error");
    }


}
