package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class MyBaseComposite {

    @Column(precision = 36)
    private String id;

    @Column
    private LocalDateTime dataTime;


}
