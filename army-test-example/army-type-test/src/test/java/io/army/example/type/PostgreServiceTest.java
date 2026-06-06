package io.army.example.type;

import io.army.example.type.service.PostgreService;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

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

}
