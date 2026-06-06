package io.army.example.type;

import io.army.annotation.GeneratorType;
import io.army.generator.FieldGenerator;
import io.army.generator.FieldGeneratorFactory;
import io.army.generator.snowflake.Snowflake8Generator;
import io.army.util._Exceptions;

public class TypeFieldGeneratorFactory implements FieldGeneratorFactory {

    private final Snowflake8Generator snowflake8;

    public TypeFieldGeneratorFactory() {
        this.snowflake8 = new Snowflake8Generator(1779201880496L);
    }

    @Override
    public FieldGenerator getGenerator(GeneratorType type) {
        final FieldGenerator generator;
        switch (type) {
            case SNOWFLAKE8:
                generator = this.snowflake8;
                break;
            default:
                throw _Exceptions.unexpectedEnum(type);
        }
        return generator;
    }

}
