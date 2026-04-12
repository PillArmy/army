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

        final MyBean bean = new MyBean();

        Assert.assertEquals(accessor.getAccessedType(), MyBean.class);

        Assert.assertEquals(accessor.getJavaType("id"), int.class);

        Assert.assertTrue(accessor.isReadable("id"));
        Assert.assertTrue(accessor.isWritable("id"));
        Assert.assertTrue(accessor.isWritable("id", int.class));
        Assert.assertTrue(accessor.isWritable("id", Integer.class));

        Assert.assertTrue(accessor.getIndex("id") > -1);

        accessor.set(bean, "id", 0);
        Assert.assertEquals(accessor.get(bean, "id"), 0);


        Assert.assertEquals(accessor.getJavaType("visible"), boolean.class);
        Assert.assertTrue(accessor.isReadable("visible"));
        Assert.assertTrue(accessor.isWritable("visible", boolean.class));
        Assert.assertTrue(accessor.isWritable("visible", Boolean.class));
        Assert.assertTrue(accessor.getIndex("visible") > -1);

        accessor.set(bean, "visible", true);
        Assert.assertEquals(accessor.get(bean, "visible"), true);

        Assert.assertEquals(accessor.getJavaType("aB"), boolean.class);
        Assert.assertTrue(accessor.isReadable("aB"));
        Assert.assertTrue(accessor.isWritable("aB", boolean.class));
        Assert.assertTrue(accessor.isWritable("aB", Boolean.class));

        accessor.set(bean, "aB", Boolean.TRUE);
        Assert.assertEquals(accessor.get(bean, "aB"), true);

        Assert.assertEquals(accessor.getJavaType("bD"), String.class);
        Assert.assertTrue(accessor.isReadable("bD"));
        Assert.assertTrue(accessor.isWritable("bD", String.class));

        Assert.assertEquals(accessor.getJavaType("a"), LocalDate.class);
        Assert.assertTrue(accessor.isReadable("a"));
        Assert.assertTrue(accessor.isWritable("a", LocalDate.class));

        final LocalDate now = LocalDate.now();
        accessor.set(bean, "a", now);
        Assert.assertEquals(accessor.get(bean, "a"), now);

        accessor.set(bean, "a", null);
        Assert.assertNull(accessor.get(bean, "a"));

        Assert.assertEquals(accessor.getJavaType("publicInt"), Integer.class);
        Assert.assertTrue(accessor.isReadable("publicInt"));
        Assert.assertTrue(accessor.isWritable("publicInt", Integer.class));

        accessor.set(bean, "publicInt", 3);

        Assert.assertEquals(accessor.getJavaType("number"), Integer.class);
        Assert.assertTrue(accessor.isReadable("number"));
        Assert.assertTrue(accessor.isWritable("number", Integer.class));

        Assert.assertEquals(accessor.getJavaType("URL"), String.class);
        Assert.assertTrue(accessor.isReadable("URL"));
        Assert.assertTrue(accessor.isWritable("URL", String.class));
        Assert.assertTrue(accessor.getIndex("URL") > -1);

        accessor.set(bean, "URL", "234");
        Assert.assertEquals(accessor.get(bean, "URL"), "234");
        accessor.set(bean, "URL", null);
        Assert.assertNull(accessor.get(bean, "URL"));

        Assert.assertFalse(accessor.isReadable("B"));
        Assert.assertFalse(accessor.isWritable("B"));
        Assert.assertFalse(accessor.isWritable("B", LocalDate.class));
        Assert.assertTrue(accessor.getIndex("B") < 0);

        Assert.assertFalse(accessor.isReadable("noGetterSetterField")); // no setter and getter
        Assert.assertFalse(accessor.isWritable("noGetterSetterField"));
        Assert.assertFalse(accessor.isWritable("noGetterSetterField", boolean.class));
        Assert.assertFalse(accessor.isWritable("noGetterSetterField", Boolean.class));

        Assert.assertTrue(accessor.getIndex("noGetterSetterField") < 0);

        Assert.assertFalse(accessor.isReadable("Da"));
        Assert.assertFalse(accessor.isWritable("Da"));
        Assert.assertFalse(accessor.isWritable("Da", String.class));
        Assert.assertTrue(accessor.getIndex("Da") < 0);

        accessor.set(bean, "absNumber", 2342D);
        Assert.assertEquals(accessor.get(bean, "absNumber"), 2342D);

        accessor.set(bean, "absNumber", 3);
        Assert.assertEquals(accessor.get(bean, "absNumber"), 3);

    }


    @Test
    public void fieldAccess() {
        final ObjectAccessor accessor;
        accessor = ObjectAccessorFactory.forBean(FieldBean.class);

        final FieldBean bean = new FieldBean();

        Assert.assertEquals(accessor.getJavaType("id"), int.class);

        Assert.assertTrue(accessor.isReadable("id"));
        Assert.assertTrue(accessor.isWritable("id"));
        Assert.assertTrue(accessor.isWritable("id", int.class));
        Assert.assertTrue(accessor.isWritable("id", Integer.class));
        Assert.assertTrue(accessor.getIndex("id") > -1);

        accessor.set(bean, "id", 0);
        Assert.assertEquals(accessor.get(bean, "id"), 0);


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
