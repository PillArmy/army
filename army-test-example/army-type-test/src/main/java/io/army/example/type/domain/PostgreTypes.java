package io.army.example.type.domain;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import io.army.annotation.*;
import io.army.pojo.FieldAccessPojo;

import java.math.BigDecimal;
import java.time.*;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;


@Table(name = "postgre_types", comment = "postgre types for army example")
public class PostgreTypes implements FieldAccessPojo {


    @Generator(type = GeneratorType.POST)
    @Column
    public Long id;

    @Column
    public LocalDateTime createTime;

    @Column
    public LocalDateTime updateTime;

    // ------------- 数值类型 -------------
    @Column(comment = "boolean type")
    public Boolean bool;

    @Column(comment = "smallint type")
    public Short smallint;

    @Column(comment = "integer type")
    public Integer integer;

    @Column(comment = "bigint type")
    public Long bigint;

    @Column(comment = "decimal type")
    public BigDecimal decimal;

    @Column(comment = "real type")
    public Float real;

    @Column(comment = "double precision type")
    public Double doublePrecision;

    // ------------- 位串类型 -------------
    @Mapping("io.army.mapping.BitSetType")
    @Column(precision = 64, comment = "bit(64) type")
    public BitSet bit64;

    @Mapping("io.army.mapping.BitSetType")
    @Column(precision = 20, comment = "varbit type")
    public BitSet varbit;

    // ------------- 日期时间类型 -------------
    @Column(comment = "time type")
    public LocalTime time;

    @Column(comment = "date type")
    public LocalDate date;

    @Column(comment = "timestamp type")
    public LocalDateTime timestamp;

    @Column(comment = "timetz type")
    public OffsetTime timetz;

    @Column(comment = "timestamptz type")
    public OffsetDateTime timestamptz;

    @Mapping("io.army.mapping.optional.IntervalType")
    // @Column(comment = "interval type")
    public String interval;

    // ------------- 二进制类型 -------------
    @Column(comment = "bytea type")
    public byte[] bytea;

    @Mapping("io.army.mapping.BinaryType")
    @Column(comment = "bytea type")
    public byte[] binary;

    @Mapping("io.army.mapping.TinyBlobType")
    @Column(comment = "bytea type")
    public byte[] tinyBlob;

    @Mapping("io.army.mapping.BlobType")
    @Column(comment = "bytea type")
    public byte[] blob;

    @Mapping("io.army.mapping.MediumBlobType")
    @Column(comment = "bytea type")
    public byte[] mediumBlob;


    @Column(comment = "bytea type")
    @Mapping("io.army.mapping.array.VarBinaryArrayType")
    public byte[][] byteaArray;

    @Mapping("io.army.mapping.array.BinaryArrayType")
    @Column(comment = "bytea type")
    public byte[][] binaryArray;

    @Mapping("io.army.mapping.array.BlobArrayType")
    @Column(comment = "bytea type")
    public byte[][] blobArray;


    // ------------- 字符串类型 -------------
    @Mapping("io.army.mapping.SqlCharType")
    @Column(precision = 100, comment = "char type")
    public String charType;

    @Column(precision = 200, comment = "varchar type")
    public String varchar;

    @Column(comment = "text type")
    public String text;

    @Column(comment = "money type")
    public String money;

    // ------------- JSON/XML 类型 -------------
    @Mapping("io.army.mapping.JsonType")
    @Column(comment = "json type")
    public String json;

    @Mapping("io.army.mapping.JsonbType")
    @Column(comment = "jsonb type")
    public String jsonb;

    @Mapping("io.army.mapping.XmlType")
    @Column(comment = "xml type")
    public String xml;

    @Mapping("io.army.mapping.XmlType")
    @Column(comment = "xml type")
    public Map<String, String> xmlMap;

    @Mapping("io.army.mapping.array.XmlArrayType")
    @Column(comment = "xml type")
    public Map<String, String>[] xmlMapArray;

    // ------------- UUID 类型 -------------
    @Column(comment = "uuid type")
    public UUID uuid;

    // ------------- 几何类型 -------------
    @Column(comment = "point type")
    public String point;

    @Column(comment = "line type")
    public String line;

    @Column(comment = "lseg type")
    public String lseg;

    @Column(comment = "box type")
    public String box;

    @Column(comment = "path type")
    public String path;

    @Column(comment = "polygon type")
    public String polygon;

    @Column(comment = "circle type")
    public String circle;

    // ------------- 网络地址类型 -------------
    @Column(comment = "cidr type")
    public String cidr;

    @Column(comment = "inet type")
    public String inet;

    @Column(comment = "macaddr type")
    public String macaddr;

    @Column(comment = "macaddr8 type")
    public String macaddr8;

    // ------------- 范围类型 -------------

    @Mapping("io.army.mapping.guava.GuavaRangeType")
    @Column(comment = "int4range type")
    public Range<Integer> int4RangeGuava;

    @Mapping("io.army.mapping.guava.GuavaRangeType")
    @Column(comment = "int8range type")
    public Range<Long> int8range;

    @Mapping("io.army.mapping.guava.GuavaRangeType")
    @Column(comment = "numrange type")
    public Range<BigDecimal> numrange;

    @Mapping("io.army.mapping.guava.GuavaRangeType")
    @Column(comment = "tsrange type")
    public Range<LocalDateTime> tsrange;

    @Mapping("io.army.mapping.guava.GuavaRangeType")
    @Column(comment = "tstzrange type")
    public Range<OffsetDateTime> tstzrange;

    @Mapping("io.army.mapping.guava.GuavaRangeType")
    @Column(comment = "daterange type")
    public Range<LocalDate> daterange;


    @Mapping("io.army.mapping.guava.array.GuavaRangeArrayType")
    @Column(comment = "int4range type")
    public Range<Integer>[] int4RangeArray;

    @Mapping("io.army.mapping.guava.array.GuavaRangeArrayType")
    @Column(comment = "int8range type")
    public Range<Long>[] int8rangeArray;

    @Mapping("io.army.mapping.guava.array.GuavaRangeArrayType")
    @Column(comment = "numrange type")
    public Range<BigDecimal>[] numrangeArray;

    @Mapping("io.army.mapping.guava.array.GuavaRangeArrayType")
    @Column(comment = "tsrange type")
    public Range<LocalDateTime>[] tsrangeArray;

    @Mapping("io.army.mapping.guava.array.GuavaRangeArrayType")
    @Column(comment = "tstzrange type")
    public Range<OffsetDateTime>[] tstzrangeArray;

    @Mapping("io.army.mapping.guava.array.GuavaRangeArrayType")
    @Column(comment = "daterange type")
    public Range<LocalDate>[] daterangeArray;


    @Mapping("io.army.mapping.guava.GuavaRangeSetType")
    @Column(comment = "INT4MULTIRANGE type")
    public RangeSet<Integer> int4RangeSetGuava;

    @Mapping("io.army.mapping.guava.GuavaRangeSetType")
    @Column(comment = "int8multirange type")
    public RangeSet<Long> int8multirange;

    @Mapping("io.army.mapping.guava.GuavaRangeSetType")
    @Column(comment = "nummultirange type")
    public RangeSet<BigDecimal> nummultirange;

    @Mapping("io.army.mapping.guava.GuavaRangeSetType")
    @Column(comment = "tsmultirange type")
    public RangeSet<LocalDateTime> tsmultirange;

    @Mapping("io.army.mapping.guava.GuavaRangeSetType")
    @Column(comment = "tstzmultirange type")
    public RangeSet<OffsetDateTime> tstzmultirange;

    @Mapping("io.army.mapping.guava.GuavaRangeSetType")
    @Column(comment = "datemultirange type")
    public RangeSet<LocalDate> datemultirange;


    @Mapping("io.army.mapping.guava.array.GuavaRangeSetArrayType")
    @Column(comment = "INT4MULTIRANGE type")
    public RangeSet<Integer>[] int4RangeSetGuavaArray;

    @Mapping("io.army.mapping.guava.array.GuavaRangeSetArrayType")
    @Column(comment = "int8multirange type")
    public RangeSet<Long>[] int8multirangeArray;

    @Mapping("io.army.mapping.guava.array.GuavaRangeSetArrayType")
    @Column(comment = "nummultirange type")
    public RangeSet<BigDecimal>[] nummultirangeArray;

    @Mapping("io.army.mapping.guava.array.GuavaRangeSetArrayType")
    @Column(comment = "tsmultirange type")
    public RangeSet<LocalDateTime>[] tsmultirangeArray;

    @Mapping("io.army.mapping.guava.array.GuavaRangeSetArrayType")
    @Column(comment = "tstzmultirange type")
    public RangeSet<OffsetDateTime>[] tstzmultirangeArray;

    @Mapping("io.army.mapping.guava.array.GuavaRangeSetArrayType")
    @Column(comment = "datemultirange type")
    public RangeSet<LocalDate>[] datemultirangeArray;


    @Mapping("io.army.mapping.postgre.PgHstoreType")
    @Column(comment = "hstore type")
    public Map<String, String> hstore;

    @Mapping("io.army.mapping.postgre.PgHstoreType")
    @Column(comment = "hstore type")
    public EnumMap<DayOfWeek, String> dayOfWeekStringEnumMap;

    @Mapping("io.army.mapping.postgre.PgHstoreType")
    @Column(comment = "hstore type")
    public HstorePojo hstorePojo;


    @Mapping("io.army.mapping.postgre.array.PgHstoreArrayType")
    @Column(comment = "hstore type")
    public Map<String, String>[] hstoreArray;

    @Mapping("io.army.mapping.postgre.array.PgHstoreArrayType")
    @Column(comment = "hstore type")
    public EnumMap<DayOfWeek, String>[] dayOfWeekStringEnumMapArray;

    @Mapping("io.army.mapping.postgre.array.PgHstoreArrayType")
    @Column(comment = "hstore type")
    public HstorePojo[] hstorePojoArray;


    // ------------- 多范围类型 -------------


    // ------------- 文本搜索类型 -------------
    @Column(comment = "tsvector type")
    public String tsvector;

    @Column(comment = "tsquery type")
    public String tsquery;

    // ------------- 其他类型 -------------
    @Column(comment = "pg_lsn type")
    public String pgLsn;

    @Column(comment = "pg_snapshot type")
    public String pgSnapshot;

    @Column(comment = "jsonpath type")
    public String jsonpath;

    @Column(comment = "refcursor type")
    public String refcursor;

    @Column(comment = "aclitem type")
    public String aclitem;

    @Column(comment = "product info composite type")
    public ProductInfo productInfo;

    @Column(comment = "manager info composite type")
    public ManagerInfo managerInfo;  // only one field

    // ------------- 数组类型 -------------
    @Mapping("io.army.mapping.array.BooleanArrayType")
    @Column(comment = "boolean array type")
    public Boolean[] boolArray;

    @Mapping("io.army.mapping.array.ShortArrayType")
    @Column(comment = "smallint array type")
    public Short[] smallintArray;

    @Mapping("io.army.mapping.array.IntegerArrayType")
    @Column(comment = "integer array type")
    public Integer[] integerArray;

    @Mapping("io.army.mapping.array.IntegerArrayType")
    @Column(comment = "integer 2d array type")
    public Integer[][] integer2dArray;

    //@Mapping("io.army.mapping.array.IntegerArrayType")
    @Column(comment = "int array type")
    public int[] intArray;

    @Mapping("io.army.mapping.array.IntegerArrayType")
    @Column(comment = "int 2d array type")
    public int[][] int2dArray;

    @Mapping("io.army.mapping.array.LongArrayType")
    @Column(comment = "bigint array type")
    public Long[] bigintArray;

    @Mapping("io.army.mapping.array.BigDecimalArrayType")
    @Column(comment = "decimal array type")
    public BigDecimal[] decimalArray;

    @Mapping("io.army.mapping.array.FloatArrayType")
    @Column(comment = "real array type")
    public Float[] realArray;

    @Mapping("io.army.mapping.array.DoubleArrayType")
    @Column(comment = "double precision array type")
    public Double[] doubleArray;

    @Mapping("io.army.mapping.array.BitSetArrayType")
    @Column(comment = "bit/varbit array type")
    public BitSet[] bitArray;

    @Mapping("io.army.mapping.array.LocalTimeArrayType")
    @Column(comment = "time array type")
    public LocalTime[] timeArray;

    @Mapping("io.army.mapping.array.LocalDateArrayType")
    @Column(comment = "date array type")
    public LocalDate[] dateArray;

    @Mapping("io.army.mapping.array.LocalDateTimeArrayType")
    @Column(comment = "timestamp array type")
    public LocalDateTime[] timestampArray;

    @Mapping("io.army.mapping.array.OffsetTimeArrayType")
    @Column(comment = "timetz array type")
    public OffsetTime[] timetzArray;

    @Mapping("io.army.mapping.array.OffsetDateTimeArrayType")
    @Column(comment = "timestamptz array type")
    public OffsetDateTime[] timestamptzArray;

    @Mapping("io.army.mapping.array.IntervalArrayType")
    // @Column(comment = "interval array type")
    public String[] intervalArray;


    @Mapping("io.army.mapping.array.SqlCharArrayType")
    @Column(comment = "char array type")
    public String[] charArray;

    @Mapping("io.army.mapping.array.StringArrayType")
    @Column(comment = "varchar array type")
    public String[] varcharArray;

    @Mapping("io.army.mapping.array.TextArrayType")
    @Column(comment = "text array type")
    public String[] textArray;


    @Mapping("io.army.mapping.array.TextArrayType")
    @Column(comment = "text 2d array type")
    public String[][] text2dArray;

    @Mapping("io.army.mapping.array.JsonArrayType")
    @Column(comment = "json array type")
    public String[] jsonArray;

    @Mapping("io.army.mapping.array.JsonbArrayType")
    @Column(comment = "jsonb array type")
    public String[] jsonbArray;

    @Mapping("io.army.mapping.array.XmlArrayType")
    @Column(comment = "xml array type")
    public String[] xmlArray;

    @Mapping("io.army.mapping.array.UUIDArrayType")
    @Column(comment = "uuid array type")
    public UUID[] uuidArray;

    @Mapping("io.army.mapping.array.TextArrayType")
    @Column(comment = "aclitem array type")
    public String[] aclitemArray;

    @Mapping("io.army.mapping.array.CompositeArrayType")
    @Column(comment = "product info composite array type")
    public ProductInfo[] productInfoArray;

    @Mapping("io.army.mapping.array.VectorArrayType")
    @Column(comment = "vector array type")
    public float[][] vectorArray;

}
