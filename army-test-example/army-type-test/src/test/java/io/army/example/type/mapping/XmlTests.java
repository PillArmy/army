package io.army.example.type.mapping;


import io.army.codec.XmlCodec;
import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.annotation.NewPostgreTypesId;
import io.army.example.type.domain.PostgreTypes_;
import io.army.mapping.MappingEnv;
import io.army.mapping.XmlType;
import io.army.mapping.array.XmlArrayType;
import io.army.session.SyncSession;
import io.army.sqltype.DataType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS)
@Transactional
@Rollback
public class XmlTests {

    @Test
    public void xmlMap(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final XmlType type = (XmlType) PostgreTypes_.xmlMap.mappingType();

        final DataType dataType = type.map(env.serverMeta());

        final List<Map<String, String>> list = new ArrayList<>();

        final XmlCodec codec = session.xmlCodec();

        list.add(codec.decodeMap(pomFile(null), String.class, String.class));
        list.add(codec.decodeMap(pomFile("army-core"), String.class, String.class));
        list.add(codec.decodeMap(pomFile("army-postgre"), String.class, String.class));
        list.add(codec.decodeMap(pomFile("army-jdbc"), String.class, String.class));


        Object bindValue, afterGetValue;


        for (Map<String, String> source : list) {
            bindValue = type.beforeBind(dataType, env, source);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, source);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.xmlMap, source);

        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void xmlMapArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final XmlArrayType type = (XmlArrayType) PostgreTypes_.xmlMapArray.mappingType();

        final DataType dataType = type.map(env.serverMeta());

        final List<Map<String, String>[]> list = new ArrayList<>();

        final XmlCodec codec = session.xmlCodec();

        list.add(new Map[]{codec.decodeMap(pomFile(null), String.class, String.class)});
        list.add(new Map[]{codec.decodeMap(pomFile("army-core"), String.class, String.class)});
        list.add(new Map[]{codec.decodeMap(pomFile("army-postgre"), String.class, String.class)});
        list.add(new Map[]{codec.decodeMap(pomFile("army-jdbc"), String.class, String.class)});


        Object bindValue, afterGetValue;


        for (Map<String, String>[] source : list) {
            bindValue = type.beforeBind(dataType, env, source);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, source);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.xmlMapArray, source);

        }

    }


    private static Path projectRootPath() {
        return Path.of(System.getProperty("user.dir")).getParent().getParent();
    }


    private static String pomFile(@Nullable String module) {
        final Path rootPath = projectRootPath();
        final Path path;
        if (module == null) {
            path = Path.of(rootPath.toAbsolutePath().toString(), "pom.xml");
        } else {
            path = Path.of(rootPath.toAbsolutePath().toString(), module, "pom.xml");
        }

        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}



