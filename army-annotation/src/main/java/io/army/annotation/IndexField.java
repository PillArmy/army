package io.army.annotation;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({})
@Retention(RUNTIME)
public @interface IndexField {

    /// java field name
    String name();

    /// see {@code io.army.criteria.impl.TableMetaUtils#parseIndexColumnProperty(java.lang.Class, java.lang.String, int, java.lang.String, io.army.criteria.impl.MetaContext, java.lang.String)}
    String collation() default "";

    /// see {@code io.army.criteria.impl.TableMetaUtils#parseIndexColumnProperty(java.lang.Class, java.lang.String, int, java.lang.String, io.army.criteria.impl.MetaContext, java.lang.String)}
    String opclass() default "";

    SortOrder order() default SortOrder.DEFAULT;

    NullsOrder nulls() default NullsOrder.DEFAULT;

}
