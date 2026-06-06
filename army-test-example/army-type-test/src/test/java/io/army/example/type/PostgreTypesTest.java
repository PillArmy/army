package io.army.example.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

@Test(dataProvider = "localSessionProvider")
public class PostgreTypesTest extends TypeTestSupport {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());


}
