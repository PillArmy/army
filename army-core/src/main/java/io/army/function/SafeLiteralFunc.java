package io.army.function;

import io.army.lang.Nullable;
import io.army.meta.TypeMeta;

@FunctionalInterface
public interface SafeLiteralFunc {

    void safeLiteral(TypeMeta typeMeta, @Nullable Object value, final boolean typeName,
                     final StringBuilder sqlBuilder);

}
