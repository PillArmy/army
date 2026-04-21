package io.army.example.stock.domain;

import io.army.generator.snowflake.SnowflakeGenerator;

public abstract class BaseDomain {

    protected static final String SNOWFLAKE = "io.army.generator.snowflake.SnowflakeGenerator";

    protected static final String SNOW_START_TIME = SnowflakeGenerator.START_TIME;
}
