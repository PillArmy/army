package io.army.example.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.army.example.type.domain.Postgre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TypeApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class})
@Transactional
public class PostgreControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        LOG.info("=== Starting PostgreControllerTest ===");
    }

    @Test
    public void testCreateDemo() throws Exception {
        LOG.info("=== testCreateDemo ===");
        mockMvc.perform(post("/api/postgre-types/demo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.varchar").value("Test varchar"));
        LOG.info("Create demo test passed!");
    }

    @Test
    public void testCreateAndGet() throws Exception {
        LOG.info("=== testCreateAndGet ===");
        Postgre postgre = createTestPostgre();

        String response = mockMvc.perform(post("/api/postgre-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postgre)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Postgre saved = objectMapper.readValue(response, Postgre.class);
        assertNotNull(saved.id);
        LOG.info("Created record with ID: {}", saved.id);

        mockMvc.perform(get("/api/postgre-types/{id}", saved.id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.id))
                .andExpect(jsonPath("$.varchar").value("Controller test"));
        LOG.info("Get test passed!");
    }

    @Test
    public void testFindFirst() throws Exception {
        LOG.info("=== testFindFirst ===");
        Postgre postgre1 = createTestPostgre();
        Postgre postgre2 = createTestPostgre();

        mockMvc.perform(post("/api/postgre-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postgre1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/postgre-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postgre2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/postgre-types/first"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
        LOG.info("Find first test passed!");
    }

    private Postgre createTestPostgre() {
        Postgre postgre = new Postgre();
        postgre.bool = true;
        postgre.smallint = (short) 1111;
        postgre.integer = 22222;
        postgre.bigint = 333333L;
        postgre.decimal = new BigDecimal("444.44");

        postgre.charField = "C";
        postgre.varchar = "Controller test";
        postgre.text = "Controller test text";

        postgre.time = LocalTime.of(10, 0, 0);
        postgre.date = LocalDate.of(2024, 10, 1);
        postgre.timestamp = LocalDateTime.of(2024, 10, 1, 10, 0, 0);

        postgre.uuid = UUID.randomUUID();
        postgre.json = "{\"controller\": true}";
        postgre.jsonb = "{\"controller\": true}";

        return postgre;
    }
}
