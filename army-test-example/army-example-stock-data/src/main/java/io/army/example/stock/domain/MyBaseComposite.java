package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;

@MappedSuperclass
public abstract class MyBaseComposite {

    @Column
    private String id;
}
