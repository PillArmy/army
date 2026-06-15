package io.army.sqltype;


/// This interface representing user-defined type or unrecognized database build-in type.
public interface CustomType extends DataType {


    boolean isComponentCreateDdl();

    @Override
    _TypeDefCharacterSetSpec parens(long precision);


    static Builder builder() {
        return CustomTypeFactory.builder();
    }


    interface Builder {

        /// Required
        Builder typeName(String typeName);

        /// At least one of {@link #elementInstance(CustomType)} and this method must be provided.
        Builder componentType(ArmyType armyType);

        /// At least one of {@link #componentType(ArmyType)} and this method must be provided.
        Builder elementInstance(CustomType customType);

        /// Required
        Builder javaType(Class<?> javaType);

        /// Optional
        Builder componentCreateDdl(boolean yes);

        /// Optional
        Builder safeTypeAlias(String safeTypeAlias);


        /// Optional,
        ///
        /// When java type is {@link java.util.List} type , Required
        Builder listElementJavaType(Class<?> elementJavaType);


        CustomType build();

    }

}
