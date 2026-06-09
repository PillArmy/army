package io.army.dialect;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PostgreDialectUtilsTests {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreDialectUtilsTests.class);


    @Test
    public void decodeIdentifier() {
        String identifier;
        List<String> list;

        identifier = "my_stock";
        list = _PostgreDialectUtils.decodeIdentifier(identifier);
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("my_stock", list.getFirst());

        identifier = "my_stock.\"_vector\"";
        list = _PostgreDialectUtils.decodeIdentifier(identifier);
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals("my_stock", list.getFirst());
        Assertions.assertEquals("_vector", list.getLast());

        identifier = "\"my_stock\"";
        list = _PostgreDialectUtils.decodeIdentifier(identifier);
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("my_stock", list.getFirst());

        identifier = "\"my_stock\".\"_vector\"";
        list = _PostgreDialectUtils.decodeIdentifier(identifier);
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals("my_stock", list.getFirst());
        Assertions.assertEquals("_vector", list.getLast());

        identifier = "\"my_st\"\"ock\"\"\"";
        list = _PostgreDialectUtils.decodeIdentifier(identifier);
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("my_st\"ock\"", list.getFirst());


        identifier = "\"my_st\"\"ock\".\"_vector\"";
        list = _PostgreDialectUtils.decodeIdentifier(identifier);
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals("my_st\"ock", list.getFirst());
        Assertions.assertEquals("_vector", list.getLast());

        identifier = "\"my_st\"\"ock\".\"_vec\"\"tor\"";
        list = _PostgreDialectUtils.decodeIdentifier(identifier);
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals("my_st\"ock", list.getFirst());
        Assertions.assertEquals("_vec\"tor", list.getLast());

        identifier = "\"my_stock\".\"_vect\"\"or\"";
        list = _PostgreDialectUtils.decodeIdentifier(identifier);
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals("my_stock", list.getFirst());
        Assertions.assertEquals("_vect\"or", list.getLast());

        identifier = "\"my_stock\".\"_vect\"\"or\"\"\"";
        list = _PostgreDialectUtils.decodeIdentifier(identifier);
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals("my_stock", list.getFirst());
        Assertions.assertEquals("_vect\"or\"", list.getLast());

    }

    @Test
    public void decodeIdentifierError() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            _PostgreDialectUtils.decodeIdentifier("my_st\"ock");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            _PostgreDialectUtils.decodeIdentifier("my_st\"\"ock.\"_vector");
        });
    }


}
