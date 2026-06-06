package io.army.meta;

import io.army.mapping.MappingType;

public interface FieldObject extends DatabaseObject {


    String fieldName();


    String columnName();


    MappingType mappingType();


    String collation();

    /// (Optional) The columnSize for a decimal (exact numeric)
    /// column. (Applies only if a decimal column is used.)
    /// Value must be set by developer if used when generating
    /// the DDL for the column.
    int precision();

    /// (Optional) The scale for a decimal (exact numeric) column.
    /// (Applies only if a decimal column is used.)
    int scale();


}
