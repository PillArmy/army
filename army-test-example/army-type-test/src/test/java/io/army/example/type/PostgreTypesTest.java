package io.army.example.type;

import io.army.criteria.Delete;
import io.army.criteria.Insert;
import io.army.criteria.Select;
import io.army.criteria.Update;
import io.army.criteria.impl.Postgres;
import io.army.example.type.domain.Postgre;
import io.army.example.type.domain.Postgre_;
import io.army.example.type.domain.ProductInfo;
import io.army.session.SyncLocalSession;
import io.army.type.SqlRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.BitSet;
import java.util.UUID;

import static io.army.criteria.impl.SQLs.AS;

@Test(dataProvider = "localSessionProvider")
public class PostgreTypesTest extends TypeTestSupport {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    @Test
    public void testInsertAndQueryAllTypes(final SyncLocalSession session) {
        final Postgre postgre = createFullPostgre();
        LOG.info("Inserting Postgre: {}", postgre);

        final Insert insertStmt = Postgres.singleInsert()
                .insertInto(Postgre_.T)
                .values(postgre)
                .asInsert();

        final int count = session.update(insertStmt);
        Assert.assertEquals(count, 1);
        Assert.assertNotNull(postgre.id);
        LOG.info("Inserted Postgre with ID: {}", postgre.id);

        final Select selectStmt = Postgres.singleQuery()
                .select("p")
                .from(Postgre_.T, AS, "p")
                .where(Postgre_.id.equal(postgre.id))
                .asQuery();

        final Postgre queried = session.queryOne(selectStmt, Postgre::new);
        Assert.assertNotNull(queried);
        LOG.info("Queried Postgre: {}", queried);

        assertPostgresEqual(postgre, queried);
        LOG.info("All types match!");
    }

    @Test(dependsOnMethods = "testInsertAndQueryAllTypes")
    public void testUpdateAllTypes(final SyncLocalSession session) {
        final Select selectFirstStmt = Postgres.singleQuery()
                .select("p")
                .from(Postgre_.T, AS, "p")
                .orderBy(Postgre_.id)
                .limit(1)
                .asQuery();

        final Postgre first = session.queryOne(selectFirstStmt, Postgre::new);
        Assert.assertNotNull(first);
        LOG.info("Found Postgre to update: {}", first);

        updatePostgreForTest(first);
        LOG.info("Updated Postgre values: {}", first);

        final Update updateStmt = Postgres.singleUpdate()
                .update(Postgre_.T, AS, "p")
                .set(first)
                .where(Postgre_.id.equal(first.id))
                .asUpdate();

        final int count = session.update(updateStmt);
        Assert.assertEquals(count, 1);
        LOG.info("Updated successfully");

        final Select selectAfterStmt = Postgres.singleQuery()
                .select("p")
                .from(Postgre_.T, AS, "p")
                .where(Postgre_.id.equal(first.id))
                .asQuery();

        final Postgre afterUpdate = session.queryOne(selectAfterStmt, Postgre::new);
        Assert.assertNotNull(afterUpdate);
        LOG.info("After update: {}", afterUpdate);

        assertPostgresEqual(first, afterUpdate);
        LOG.info("Update test passed!");
    }

    @Test
    @Transactional
    public void testArrayTypes(final SyncLocalSession session) {
        final Postgre postgre = createFullPostgre();
        LOG.info("Testing array types: {}", postgre);

        final Insert insertStmt = Postgres.singleInsert()
                .insertInto(Postgre_.T)
                .values(postgre)
                .asInsert();

        session.update(insertStmt);

        final Select selectStmt = Postgres.singleQuery()
                .select("p")
                .from(Postgre_.T, AS, "p")
                .where(Postgre_.id.equal(postgre.id))
                .asQuery();

        final Postgre queried = session.queryOne(selectStmt, Postgre::new);
        Assert.assertNotNull(queried);

        assertArrayEqual(postgre.boolArray, queried.boolArray);
        assertArrayEqual(postgre.intArray, queried.intArray);
        assertArrayEqual(postgre.bigintArray, queried.bigintArray);
        assertArrayEqual(postgre.textArray, queried.textArray);

        LOG.info("Array types test passed!");
    }

    @Test
    @Transactional
    public void testCompositeType(final SyncLocalSession session) {
        final Postgre postgre = createFullPostgre();
        LOG.info("Testing composite type: {}", postgre.productInfo);

        final Insert insertStmt = Postgres.singleInsert()
                .insertInto(Postgre_.T)
                .values(postgre)
                .asInsert();

        session.update(insertStmt);

        final Select selectStmt = Postgres.singleQuery()
                .select("p")
                .from(Postgre_.T, AS, "p")
                .where(Postgre_.id.equal(postgre.id))
                .asQuery();

        final Postgre queried = session.queryOne(selectStmt, Postgre::new);
        Assert.assertNotNull(queried);
        Assert.assertNotNull(queried.productInfo);
        LOG.info("Queried composite type: {}", queried.productInfo);

        Assert.assertEquals(queried.productInfo.getProductId(), postgre.productInfo.getProductId());
        Assert.assertEquals(queried.productInfo.getProductName(), postgre.productInfo.getProductName());
        Assert.assertEquals(queried.productInfo.getPrice(), postgre.productInfo.getPrice());
        Assert.assertEquals(queried.productInfo.getAvailable(), postgre.productInfo.getAvailable());
        Assert.assertEquals(queried.productInfo.getReleaseDate(), postgre.productInfo.getReleaseDate());

        LOG.info("Composite type test passed!");
    }

    @Test
    @Transactional
    public void testRecordType(final SyncLocalSession session) {
        final Postgre postgre = createFullPostgre();
        LOG.info("Testing record type: {}", postgre.record);

        final Insert insertStmt = Postgres.singleInsert()
                .insertInto(Postgre_.T)
                .values(postgre)
                .asInsert();

        session.update(insertStmt);

        final Select selectStmt = Postgres.singleQuery()
                .select("p")
                .from(Postgre_.T, AS, "p")
                .where(Postgre_.id.equal(postgre.id))
                .asQuery();

        final Postgre queried = session.queryOne(selectStmt, Postgre::new);
        Assert.assertNotNull(queried);
        Assert.assertNotNull(queried.record);
        LOG.info("Queried record type: {}", queried.record);

        LOG.info("Record type test passed!");
    }

    @Test
    @Transactional
    public void testCleanUp(final SyncLocalSession session) {
        final Delete deleteStmt = Postgres.singleDelete()
                .deleteFrom(Postgre_.T)
                .asDelete();

        final int count = session.update(deleteStmt);
        LOG.info("Cleaned up {} rows", count);
    }

    private Postgre createFullPostgre() {
        final Postgre postgre = new Postgre();

        postgre.bool = true;
        postgre.smallint = (short) 12345;
        postgre.integer = 123456789;
        postgre.bigint = 987654321L;
        postgre.decimal = new BigDecimal("12345.6789");
        postgre.real = 123.45f;
        postgre.doublePrecision = 12345.6789;
        postgre.money = "¥12,345.67";

        postgre.charField = "C";
        postgre.varchar = "Test varchar";
        postgre.text = "This is a test text field with multiple characters.";

        postgre.time = LocalTime.of(12, 34, 56);
        postgre.timetz = OffsetTime.of(LocalTime.of(12, 34, 56), ZoneOffset.ofHours(8));
        postgre.date = LocalDate.of(2024, 6, 7);
        postgre.timestamp = LocalDateTime.of(2024, 6, 7, 12, 34, 56);
        postgre.timestamptz = ZonedDateTime.of(LocalDateTime.of(2024, 6, 7, 12, 34, 56), ZoneId.of("Asia/Shanghai"));

        final BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(2);
        postgre.bit = bitSet;
        postgre.bytea = "test binary data".getBytes();
        postgre.uuid = UUID.randomUUID();

        postgre.json = "{\"name\": \"test\", \"value\": 123}";
        postgre.jsonb = "{\"type\": \"product\", \"price\": 123.45}";
        postgre.xml = "<root><item>test</item></root>";

        final SqlRecord record = SqlRecord.newInstance();
        record.put("id", 1);
        record.put("name", "Test Record");
        postgre.record = record;

        postgre.aclitem = "postgres=rw";

        postgre.productInfo = new ProductInfo(1L, "Test Product", new BigDecimal("99.99"), true, LocalDate.of(2024, 1, 1));

        postgre.boolArray = new Boolean[]{true, false, null, true};
        postgre.smallintArray = new Short[]{(short) 1, (short) 2, (short) 3};
        postgre.intArray = new Integer[]{100, 200, 300, null};
        postgre.bigintArray = new Long[]{1000L, 2000L};
        postgre.decimalArray = new BigDecimal[]{new BigDecimal("1.1"), new BigDecimal("2.2")};
        postgre.textArray = new String[]{"hello", "world", "postgres"};
        postgre.uuidArray = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};
        postgre.recordArray = new SqlRecord[]{
                SqlRecord.newInstance().put("name", "Record 1"),
                SqlRecord.newInstance().put("name", "Record 2")
        };
        postgre.aclitemArray = new String[]{"postgres=rw", "reader=r"};

        return postgre;
    }

    private void updatePostgreForTest(Postgre postgre) {
        postgre.bool = false;
        postgre.smallint = (short) 54321;
        postgre.integer = 987654321;
        postgre.bigint = 123456789L;
        postgre.decimal = new BigDecimal("98765.4321");
        postgre.varchar = "Updated varchar";
        postgre.text = "Updated text content";
        postgre.date = LocalDate.of(2024, 12, 31);
        postgre.productInfo = new ProductInfo(2L, "Updated Product", new BigDecimal("199.99"), false, LocalDate.of(2024, 12, 1));
        postgre.intArray = new Integer[]{1000, 2000, 3000};
    }

    private void assertPostgresEqual(Postgre expected, Postgre actual) {
        Assert.assertEquals(actual.bool, expected.bool);
        Assert.assertEquals(actual.smallint, expected.smallint);
        Assert.assertEquals(actual.integer, expected.integer);
        Assert.assertEquals(actual.bigint, expected.bigint);
        Assert.assertEquals(actual.decimal, expected.decimal);
        Assert.assertEquals(actual.real, expected.real, 0.0001f);
        Assert.assertEquals(actual.doublePrecision, expected.doublePrecision, 0.0001);

        Assert.assertEquals(actual.varchar, expected.varchar);
        Assert.assertEquals(actual.text, expected.text);

        Assert.assertEquals(actual.time, expected.time);
        Assert.assertEquals(actual.date, expected.date);
        Assert.assertEquals(actual.timestamp, expected.timestamp);

        Assert.assertEquals(actual.uuid, expected.uuid);
        Assert.assertEquals(actual.json, expected.json);
        Assert.assertEquals(actual.jsonb, expected.jsonb);
    }

    private void assertArrayEqual(Object[] expected, Object[] actual) {
        if (expected == null) {
            Assert.assertNull(actual);
            return;
        }
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.length, expected.length);
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(actual[i], expected[i]);
        }
    }

}
