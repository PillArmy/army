package io.army.example.type;

import io.army.example.type.domain.Postgre;
import io.army.example.type.domain.ProductInfo;
import io.army.example.type.service.PostgreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TypeApplication.class)
@ActiveProfiles("test")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class})
@Transactional
public class PostgreServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreServiceTest.class);

    @Autowired
    private PostgreService postgreService;

    private Long testRecordId;

    @BeforeEach
    void setup() {
        LOG.info("=== Starting PostgreServiceTest ===");
    }

    @Test
    public void testInsertAndQuery() {
        LOG.info("=== testInsertAndQuery ===");
        Postgre postgre = createTestPostgre();
        Postgre saved = postgreService.insert(postgre);

        assertNotNull(saved);
        assertNotNull(saved.id);
        testRecordId = saved.id;
        LOG.info("Inserted record with ID: {}", testRecordId);

        Postgre queried = postgreService.findById(testRecordId);
        assertNotNull(queried);
        LOG.info("Queried record: {}", queried);

        assertEquals(saved.bool, queried.bool);
        assertEquals(saved.integer, queried.integer);
        assertEquals(saved.decimal, queried.decimal);
        assertEquals(saved.varchar, queried.varchar);
        assertEquals(saved.uuid, queried.uuid);
        LOG.info("Insert and Query test passed!");
    }

    @Test
    public void testUpdate() {
        LOG.info("=== testUpdate ===");
        Postgre postgre = createTestPostgre();
        Postgre saved = postgreService.insert(postgre);
        assertNotNull(saved);

        saved.varchar = "Updated value";
        saved.bool = false;
        Postgre updated = postgreService.update(saved);

        assertNotNull(updated);
        assertEquals(saved.id, updated.id);
        assertEquals("Updated value", updated.varchar);
        assertEquals(false, updated.bool);
        LOG.info("Update test passed!");
    }

    @Test
    public void testCompositeType() {
        LOG.info("=== testCompositeType ===");
        Postgre postgre = createTestPostgre();
        ProductInfo productInfo = new ProductInfo(1L, "Test Product", new BigDecimal("99.99"), true, LocalDate.of(2024, 1, 1));
        postgre.productInfo = productInfo;

        Postgre saved = postgreService.insert(postgre);
        Postgre queried = postgreService.findById(saved.id);

        assertNotNull(queried);
        assertNotNull(queried.productInfo);
        assertEquals(productInfo.getProductId(), queried.productInfo.getProductId());
        assertEquals(productInfo.getProductName(), queried.productInfo.getProductName());
        assertEquals(productInfo.getPrice(), queried.productInfo.getPrice());
        assertEquals(productInfo.getAvailable(), queried.productInfo.getAvailable());
        LOG.info("Composite type test passed!");
    }

    @Test
    public void testFindFirst() {
        LOG.info("=== testFindFirst ===");
        Postgre postgre1 = createTestPostgre();
        Postgre postgre2 = createTestPostgre();
        postgreService.insert(postgre1);
        postgreService.insert(postgre2);

        Postgre first = postgreService.findFirst();
        assertNotNull(first);
        LOG.info("Found first record with ID: {}", first.id);
    }

    private Postgre createTestPostgre() {
        Postgre postgre = new Postgre();
        postgre.bool = true;
        postgre.smallint = (short) 1234;
        postgre.integer = 56789;
        postgre.bigint = 987654321L;
        postgre.decimal = new BigDecimal("123.456");
        postgre.real = 123.45f;
        postgre.doublePrecision = 678.90;

        postgre.charField = "T";
        postgre.varchar = "Test value";
        postgre.text = "This is a test text";

        postgre.time = LocalTime.of(14, 30, 45);
        postgre.date = LocalDate.of(2024, 6, 7);
        postgre.timestamp = LocalDateTime.of(2024, 6, 7, 14, 30, 45);

        postgre.uuid = UUID.randomUUID();
        postgre.json = "{\"name\": \"test\", \"value\": 123}";
        postgre.jsonb = "{\"type\": \"product\", \"price\": 123.45}";

        return postgre;
    }
}
