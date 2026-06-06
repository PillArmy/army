package io.army.example.type.web.controller;

import io.army.example.type.domain.Postgre;
import io.army.example.type.service.PostgreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/postgre-types")
public class PostgreTypeController {

    private final PostgreService postgreService;

    @Autowired
    public PostgreTypeController(PostgreService postgreService) {
        this.postgreService = postgreService;
    }

    @PostMapping
    public Postgre create(@RequestBody Postgre postgre) {
        return postgreService.insert(postgre);
    }

    @PutMapping
    public Postgre update(@RequestBody Postgre postgre) {
        return postgreService.update(postgre);
    }

    @GetMapping("/{id}")
    public Postgre getById(@PathVariable Long id) {
        return postgreService.findById(id);
    }

    @GetMapping("/first")
    public Postgre getFirst() {
        return postgreService.findFirst();
    }

    @PostMapping("/demo")
    public Postgre createDemo() {
        Postgre postgre = new Postgre();
        postgre.setBool(true);
        postgre.setSmallint((short) 1234);
        postgre.setInteger(56789);
        postgre.setBigint(987654321L);
        postgre.setDecimal(new BigDecimal("123.456"));
        postgre.setReal(123.45f);
        postgre.setDoublePrecision(678.90);
        postgre.setMoney("¥999.99");

        postgre.setCharField("X");
        postgre.setVarchar("Test varchar");
        postgre.setText("This is a test text content");

        postgre.setTime(LocalTime.of(14, 30, 45));
        postgre.setDate(LocalDate.of(2026, 6, 7));
        postgre.setTimestamp(LocalDateTime.of(2026, 6, 7, 14, 30, 45));

        postgre.setUuid(UUID.randomUUID());
        postgre.setJson("{\"name\":\"demo\",\"value\":123}");
        postgre.setJsonb("{\"type\":\"test\",\"id\":456}");
        postgre.setXml("<root><item>Demo</item></root>");

        return postgreService.insert(postgre);
    }

}
