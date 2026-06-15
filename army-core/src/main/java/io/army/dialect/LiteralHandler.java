package io.army.dialect;

import io.army.lang.Nullable;
import io.army.meta.TypeMeta;
import io.army.sqltype.DataType;

@FunctionalInterface
public interface LiteralHandler {

    void safeLiteral(TypeMeta typeMeta, @Nullable Object value, final boolean typeName,
                     final StringBuilder sqlBuilder, @Nullable DataType container);

}
