package io.army.bean;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ObjectAccessorFactoryTests {

    @Test
    public void getterAndSetter() {
        final ObjectAccessor accessor;
        accessor = ObjectAccessorFactory.forBean(MyBean.class);

        Assert.assertEquals(accessor.getAccessedType(), MyBean.class);


        Assert.assertEquals(accessor.getJavaType("id"), int.class);

        Assert.assertTrue(accessor.isReadable("id"));
        Assert.assertTrue(accessor.isWritable("id"));
        Assert.assertTrue(accessor.isWritable("id", int.class));
        Assert.assertTrue(accessor.isWritable("id", Integer.class));

        Assert.assertTrue(accessor.getIndex("id") > -1);


        Assert.assertEquals(accessor.getJavaType("visible"), boolean.class);
        Assert.assertTrue(accessor.isReadable("visible"));
        Assert.assertTrue(accessor.isWritable("visible", boolean.class));
        Assert.assertTrue(accessor.isWritable("visible", Boolean.class));
        Assert.assertTrue(accessor.getIndex("visible") > -1);


        Assert.assertEquals(accessor.getJavaType("aB"), boolean.class);
        Assert.assertTrue(accessor.isReadable("aB"));
        Assert.assertTrue(accessor.isWritable("aB", boolean.class));
        Assert.assertTrue(accessor.isWritable("aB", Boolean.class));

        Assert.assertEquals(accessor.getJavaType("bD"), String.class);
        Assert.assertTrue(accessor.isReadable("bD"));
        Assert.assertTrue(accessor.isWritable("bD", String.class));

        Assert.assertEquals(accessor.getJavaType("a"), LocalDate.class);
        Assert.assertTrue(accessor.isReadable("a"));
        Assert.assertTrue(accessor.isWritable("a", LocalDate.class));


        Assert.assertEquals(accessor.getJavaType("publicInt"), Integer.class);
        Assert.assertTrue(accessor.isReadable("publicInt"));
        Assert.assertTrue(accessor.isWritable("publicInt", Integer.class));


        Assert.assertEquals(accessor.getJavaType("number"), Integer.class);
        Assert.assertTrue(accessor.isReadable("number"));
        Assert.assertTrue(accessor.isWritable("number", Integer.class));

        Assert.assertEquals(accessor.getJavaType("URL"), String.class);
        Assert.assertTrue(accessor.isReadable("URL"));
        Assert.assertTrue(accessor.isWritable("URL", String.class));
        Assert.assertTrue(accessor.getIndex("URL") > -1);


        Assert.assertFalse(accessor.isReadable("B"));
        Assert.assertFalse(accessor.isWritable("B"));
        Assert.assertFalse(accessor.isWritable("B", LocalDate.class));
        Assert.assertTrue(accessor.getIndex("B") < 0);

        Assert.assertFalse(accessor.isReadable("noGetterSetterField"));
        Assert.assertFalse(accessor.isWritable("noGetterSetterField"));
        Assert.assertFalse(accessor.isWritable("noGetterSetterField", boolean.class));
        Assert.assertFalse(accessor.isWritable("noGetterSetterField", Boolean.class));

        Assert.assertTrue(accessor.getIndex("noGetterSetterField") < 0);

        Assert.assertFalse(accessor.isReadable("Da"));
        Assert.assertFalse(accessor.isWritable("Da"));
        Assert.assertFalse(accessor.isWritable("Da", String.class));
        Assert.assertTrue(accessor.getIndex("Da") < 0);


    }


    @Test
    public void fieldAccess() {
        final ObjectAccessor accessor;
        accessor = ObjectAccessorFactory.forBean(FieldBean.class);

        Assert.assertEquals(accessor.getJavaType("id"), int.class);

        Assert.assertTrue(accessor.isReadable("id"));
        Assert.assertTrue(accessor.isWritable("id"));
        Assert.assertTrue(accessor.isWritable("id", int.class));
        Assert.assertTrue(accessor.isWritable("id", Integer.class));
        Assert.assertTrue(accessor.getIndex("id") > -1);


        Assert.assertEquals(accessor.getJavaType("name"), String.class);
        Assert.assertTrue(accessor.isReadable("name"));
        Assert.assertTrue(accessor.isWritable("name", String.class));
        Assert.assertFalse(accessor.isWritable("name", Integer.class));
        Assert.assertTrue(accessor.getIndex("name") > -1);

        Assert.assertFalse(accessor.isReadable("privateBool"));
        Assert.assertFalse(accessor.isWritable("privateBool"));
        Assert.assertFalse(accessor.isWritable("privateBool", boolean.class));
        Assert.assertFalse(accessor.isWritable("privateBool", Boolean.class));

        Assert.assertEquals(accessor.getJavaType("date"), LocalDate.class);
        Assert.assertTrue(accessor.isReadable("date"));
        Assert.assertTrue(accessor.isWritable("date", LocalDate.class));
        Assert.assertFalse(accessor.isWritable("date", Integer.class));


        Assert.assertEquals(accessor.getJavaType("createTime"), LocalDateTime.class);
        Assert.assertTrue(accessor.isReadable("createTime"));
        Assert.assertTrue(accessor.isWritable("createTime", LocalDateTime.class));
        Assert.assertFalse(accessor.isWritable("createTime", LocalDate.class));

        Assert.assertFalse(accessor.isReadable("propInt"));
        Assert.assertFalse(accessor.isWritable("propInt"));
        Assert.assertFalse(accessor.isWritable("propInt", int.class));
        Assert.assertFalse(accessor.isWritable("propInt", Integer.class));
        Assert.assertTrue(accessor.getIndex("propInt") < 0);


        Assert.assertFalse(accessor.isReadable("visible"));
        Assert.assertFalse(accessor.isWritable("visible"));
        Assert.assertFalse(accessor.isWritable("visible", boolean.class));
        Assert.assertFalse(accessor.isWritable("visible", Boolean.class));
        Assert.assertTrue(accessor.getIndex("visible") < 0);


    }

}
