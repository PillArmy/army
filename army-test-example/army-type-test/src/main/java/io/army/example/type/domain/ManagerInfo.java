package io.army.example.type.domain;

import io.army.annotation.Column;
import io.army.pojo.FieldAccessPojo;
import io.army.struct.DefinedType;

import java.util.Objects;

@DefinedType(name = "manager_info",
        fieldOrder = {"id"})
public class ManagerInfo implements FieldAccessPojo {

    @Column
    public Long id;


    public Long getId() {
        return id;
    }

    public ManagerInfo setId(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof ManagerInfo o) {
            match = Objects.equals(o.id, this.id);
        } else {
            match = false;
        }
        return match;
    }
}
