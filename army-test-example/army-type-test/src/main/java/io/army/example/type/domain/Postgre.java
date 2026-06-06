package io.army.example.type.domain;

import io.army.annotation.*;
import io.army.generator.snowflake.Snowflake8Generator;
import io.army.mapping.optional.SqlRecordType;
import io.army.pojo.FieldAccessPojo;
import io.army.type.SqlRecord;

import java.math.BigDecimal;
import java.time.*;
import java.util.BitSet;
import java.util.UUID;


@Table(name = "postgre_types", comment = "postgre types for army example")
public class Postgre extends TypeBaseDomain<Postgre> implements FieldAccessPojo {

    public static final long SNOWFLAKE_START_TIME = 1779201880496L;

    @Generator(value = SNOWFLAKE8, params = @Param(name = Snowflake8Generator.START_TIME, value = "" + SNOWFLAKE_START_TIME))
    @Column
    public Long id;

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
    @Column(comment = "interval type")
    public String interval;

    // ------------- 二进制类型 -------------
    @Column(comment = "bytea type")
    public byte[] bytea;

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
    @Column(comment = "int4range type")
    public String int4range;

    @Column(comment = "int8range type")
    public String int8range;

    @Column(comment = "numrange type")
    public String numrange;

    @Column(comment = "tsrange type")
    public String tsrange;

    @Column(comment = "tstzrange type")
    public String tstzrange;

    @Column(comment = "daterange type")
    public String daterange;

    // ------------- 多范围类型 -------------
    @Column(comment = "int4multirange type")
    public String int4multirange;

    @Column(comment = "int8multirange type")
    public String int8multirange;

    @Column(comment = "nummultirange type")
    public String nummultirange;

    @Column(comment = "tsmultirange type")
    public String tsmultirange;

    @Column(comment = "tstzmultirange type")
    public String tstzmultirange;

    @Column(comment = "datemultirange type")
    public String datemultirange;

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

    @Mapping("io.army.mapping.optional.SqlRecordType")
    @Column(comment = "record type")
    public SqlRecord record;

    @Column(comment = "aclitem type")
    public String aclitem;

    @Mapping(io.army.mapping.CompositeType.class)
    @Column(comment = "product info composite type")
    public ProductInfo productInfo;

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
    @Column(comment = "interval array type")
    public String[] intervalArray;

    @Mapping("io.army.mapping.array.BlobArrayType")
    @Column(comment = "bytea array type")
    public byte[][] byteaArray;

    @Mapping("io.army.mapping.array.SqlCharArrayType")
    @Column(comment = "char array type")
    public String[] charArray;

    @Mapping("io.army.mapping.array.StringArrayType")
    @Column(comment = "varchar array type")
    public String[] varcharArray;

    @Mapping("io.army.mapping.array.TextArrayType")
    @Column(comment = "text array type")
    public String[] textArray;

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

    @Mapping("io.army.mapping.array.SqlRecordArrayType")
    @Column(comment = "record array type")
    public SqlRecord[] recordArray;

    @Mapping("io.army.mapping.array.TextArrayType")
    @Column(comment = "aclitem array type")
    public String[] aclitemArray;

    @Mapping(io.army.mapping.array.CompositeArrayType.class)
    @Column(comment = "product info composite array type")
    public ProductInfo[] productInfoArray;

    @Mapping("io.army.mapping.array.VectorArrayType")
    @Column(comment = "vector array type")
    public float[][] vectorArray;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Postgre setId(Long id) {
        this.id = id;
        return this;
    }

    public Boolean getBool() {
        return bool;
    }

    public Postgre setBool(Boolean bool) {
        this.bool = bool;
        return this;
    }

    public Short getSmallint() {
        return smallint;
    }

    public Postgre setSmallint(Short smallint) {
        this.smallint = smallint;
        return this;
    }

    public Integer getInteger() {
        return integer;
    }

    public Postgre setInteger(Integer integer) {
        this.integer = integer;
        return this;
    }

    public Long getBigint() {
        return bigint;
    }

    public Postgre setBigint(Long bigint) {
        this.bigint = bigint;
        return this;
    }

    public BigDecimal getDecimal() {
        return decimal;
    }

    public Postgre setDecimal(BigDecimal decimal) {
        this.decimal = decimal;
        return this;
    }

    public Float getReal() {
        return real;
    }

    public Postgre setReal(Float real) {
        this.real = real;
        return this;
    }

    public Double getDoublePrecision() {
        return doublePrecision;
    }

    public Postgre setDoublePrecision(Double doublePrecision) {
        this.doublePrecision = doublePrecision;
        return this;
    }

    public BitSet getBit64() {
        return bit64;
    }

    public Postgre setBit64(BitSet bit64) {
        this.bit64 = bit64;
        return this;
    }

    public BitSet getVarbit() {
        return varbit;
    }

    public Postgre setVarbit(BitSet varbit) {
        this.varbit = varbit;
        return this;
    }

    public LocalTime getTime() {
        return time;
    }

    public Postgre setTime(LocalTime time) {
        this.time = time;
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public Postgre setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Postgre setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public OffsetTime getTimetz() {
        return timetz;
    }

    public Postgre setTimetz(OffsetTime timetz) {
        this.timetz = timetz;
        return this;
    }

    public OffsetDateTime getTimestamptz() {
        return timestamptz;
    }

    public Postgre setTimestamptz(OffsetDateTime timestamptz) {
        this.timestamptz = timestamptz;
        return this;
    }

    public String getInterval() {
        return interval;
    }

    public Postgre setInterval(String interval) {
        this.interval = interval;
        return this;
    }

    public byte[] getBytea() {
        return bytea;
    }

    public Postgre setBytea(byte[] bytea) {
        this.bytea = bytea;
        return this;
    }

    public String getCharType() {
        return charType;
    }

    public Postgre setCharType(String charType) {
        this.charType = charType;
        return this;
    }

    public String getVarchar() {
        return varchar;
    }

    public Postgre setVarchar(String varchar) {
        this.varchar = varchar;
        return this;
    }

    public String getText() {
        return text;
    }

    public Postgre setText(String text) {
        this.text = text;
        return this;
    }

    public String getMoney() {
        return money;
    }

    public Postgre setMoney(String money) {
        this.money = money;
        return this;
    }

    public String getJson() {
        return json;
    }

    public Postgre setJson(String json) {
        this.json = json;
        return this;
    }

    public String getJsonb() {
        return jsonb;
    }

    public Postgre setJsonb(String jsonb) {
        this.jsonb = jsonb;
        return this;
    }

    public String getXml() {
        return xml;
    }

    public Postgre setXml(String xml) {
        this.xml = xml;
        return this;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Postgre setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getPoint() {
        return point;
    }

    public Postgre setPoint(String point) {
        this.point = point;
        return this;
    }

    public String getLine() {
        return line;
    }

    public Postgre setLine(String line) {
        this.line = line;
        return this;
    }

    public String getLseg() {
        return lseg;
    }

    public Postgre setLseg(String lseg) {
        this.lseg = lseg;
        return this;
    }

    public String getBox() {
        return box;
    }

    public Postgre setBox(String box) {
        this.box = box;
        return this;
    }

    public String getPath() {
        return path;
    }

    public Postgre setPath(String path) {
        this.path = path;
        return this;
    }

    public String getPolygon() {
        return polygon;
    }

    public Postgre setPolygon(String polygon) {
        this.polygon = polygon;
        return this;
    }

    public String getCircle() {
        return circle;
    }

    public Postgre setCircle(String circle) {
        this.circle = circle;
        return this;
    }

    public String getCidr() {
        return cidr;
    }

    public Postgre setCidr(String cidr) {
        this.cidr = cidr;
        return this;
    }

    public String getInet() {
        return inet;
    }

    public Postgre setInet(String inet) {
        this.inet = inet;
        return this;
    }

    public String getMacaddr() {
        return macaddr;
    }

    public Postgre setMacaddr(String macaddr) {
        this.macaddr = macaddr;
        return this;
    }

    public String getMacaddr8() {
        return macaddr8;
    }

    public Postgre setMacaddr8(String macaddr8) {
        this.macaddr8 = macaddr8;
        return this;
    }

    public String getInt4range() {
        return int4range;
    }

    public Postgre setInt4range(String int4range) {
        this.int4range = int4range;
        return this;
    }

    public String getInt8range() {
        return int8range;
    }

    public Postgre setInt8range(String int8range) {
        this.int8range = int8range;
        return this;
    }

    public String getNumrange() {
        return numrange;
    }

    public Postgre setNumrange(String numrange) {
        this.numrange = numrange;
        return this;
    }

    public String getTsrange() {
        return tsrange;
    }

    public Postgre setTsrange(String tsrange) {
        this.tsrange = tsrange;
        return this;
    }

    public String getTstzrange() {
        return tstzrange;
    }

    public Postgre setTstzrange(String tstzrange) {
        this.tstzrange = tstzrange;
        return this;
    }

    public String getDaterange() {
        return daterange;
    }

    public Postgre setDaterange(String daterange) {
        this.daterange = daterange;
        return this;
    }

    public String getInt4multirange() {
        return int4multirange;
    }

    public Postgre setInt4multirange(String int4multirange) {
        this.int4multirange = int4multirange;
        return this;
    }

    public String getInt8multirange() {
        return int8multirange;
    }

    public Postgre setInt8multirange(String int8multirange) {
        this.int8multirange = int8multirange;
        return this;
    }

    public String getNummultirange() {
        return nummultirange;
    }

    public Postgre setNummultirange(String nummultirange) {
        this.nummultirange = nummultirange;
        return this;
    }

    public String getTsmultirange() {
        return tsmultirange;
    }

    public Postgre setTsmultirange(String tsmultirange) {
        this.tsmultirange = tsmultirange;
        return this;
    }

    public String getTstzmultirange() {
        return tstzmultirange;
    }

    public Postgre setTstzmultirange(String tstzmultirange) {
        this.tstzmultirange = tstzmultirange;
        return this;
    }

    public String getDatemultirange() {
        return datemultirange;
    }

    public Postgre setDatemultirange(String datemultirange) {
        this.datemultirange = datemultirange;
        return this;
    }

    public String getTsvector() {
        return tsvector;
    }

    public Postgre setTsvector(String tsvector) {
        this.tsvector = tsvector;
        return this;
    }

    public String getTsquery() {
        return tsquery;
    }

    public Postgre setTsquery(String tsquery) {
        this.tsquery = tsquery;
        return this;
    }

    public String getPgLsn() {
        return pgLsn;
    }

    public Postgre setPgLsn(String pgLsn) {
        this.pgLsn = pgLsn;
        return this;
    }

    public String getPgSnapshot() {
        return pgSnapshot;
    }

    public Postgre setPgSnapshot(String pgSnapshot) {
        this.pgSnapshot = pgSnapshot;
        return this;
    }

    public String getJsonpath() {
        return jsonpath;
    }

    public Postgre setJsonpath(String jsonpath) {
        this.jsonpath = jsonpath;
        return this;
    }

    public String getRefcursor() {
        return refcursor;
    }

    public Postgre setRefcursor(String refcursor) {
        this.refcursor = refcursor;
        return this;
    }

    public SqlRecord getRecord() {
        return record;
    }

    public Postgre setRecord(SqlRecord record) {
        this.record = record;
        return this;
    }

    public String getAclitem() {
        return aclitem;
    }

    public Postgre setAclitem(String aclitem) {
        this.aclitem = aclitem;
        return this;
    }

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public Postgre setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
        return this;
    }

    public Boolean[] getBoolArray() {
        return boolArray;
    }

    public Postgre setBoolArray(Boolean[] boolArray) {
        this.boolArray = boolArray;
        return this;
    }

    public Short[] getSmallintArray() {
        return smallintArray;
    }

    public Postgre setSmallintArray(Short[] smallintArray) {
        this.smallintArray = smallintArray;
        return this;
    }

    public Integer[] getIntegerArray() {
        return integerArray;
    }

    public Postgre setIntegerArray(Integer[] integerArray) {
        this.integerArray = integerArray;
        return this;
    }

    public Long[] getBigintArray() {
        return bigintArray;
    }

    public Postgre setBigintArray(Long[] bigintArray) {
        this.bigintArray = bigintArray;
        return this;
    }

    public BigDecimal[] getDecimalArray() {
        return decimalArray;
    }

    public Postgre setDecimalArray(BigDecimal[] decimalArray) {
        this.decimalArray = decimalArray;
        return this;
    }

    public Float[] getRealArray() {
        return realArray;
    }

    public Postgre setRealArray(Float[] realArray) {
        this.realArray = realArray;
        return this;
    }

    public Double[] getDoubleArray() {
        return doubleArray;
    }

    public Postgre setDoubleArray(Double[] doubleArray) {
        this.doubleArray = doubleArray;
        return this;
    }

    public BitSet[] getBitArray() {
        return bitArray;
    }

    public Postgre setBitArray(BitSet[] bitArray) {
        this.bitArray = bitArray;
        return this;
    }

    public LocalTime[] getTimeArray() {
        return timeArray;
    }

    public Postgre setTimeArray(LocalTime[] timeArray) {
        this.timeArray = timeArray;
        return this;
    }

    public LocalDate[] getDateArray() {
        return dateArray;
    }

    public Postgre setDateArray(LocalDate[] dateArray) {
        this.dateArray = dateArray;
        return this;
    }

    public LocalDateTime[] getTimestampArray() {
        return timestampArray;
    }

    public Postgre setTimestampArray(LocalDateTime[] timestampArray) {
        this.timestampArray = timestampArray;
        return this;
    }

    public OffsetTime[] getTimetzArray() {
        return timetzArray;
    }

    public Postgre setTimetzArray(OffsetTime[] timetzArray) {
        this.timetzArray = timetzArray;
        return this;
    }

    public OffsetDateTime[] getTimestamptzArray() {
        return timestamptzArray;
    }

    public Postgre setTimestamptzArray(OffsetDateTime[] timestamptzArray) {
        this.timestamptzArray = timestamptzArray;
        return this;
    }

    public String[] getIntervalArray() {
        return intervalArray;
    }

    public Postgre setIntervalArray(String[] intervalArray) {
        this.intervalArray = intervalArray;
        return this;
    }

    public byte[][] getByteaArray() {
        return byteaArray;
    }

    public Postgre setByteaArray(byte[][] byteaArray) {
        this.byteaArray = byteaArray;
        return this;
    }

    public String[] getCharArray() {
        return charArray;
    }

    public Postgre setCharArray(String[] charArray) {
        this.charArray = charArray;
        return this;
    }

    public String[] getVarcharArray() {
        return varcharArray;
    }

    public Postgre setVarcharArray(String[] varcharArray) {
        this.varcharArray = varcharArray;
        return this;
    }

    public String[] getTextArray() {
        return textArray;
    }

    public Postgre setTextArray(String[] textArray) {
        this.textArray = textArray;
        return this;
    }

    public String[] getJsonArray() {
        return jsonArray;
    }

    public Postgre setJsonArray(String[] jsonArray) {
        this.jsonArray = jsonArray;
        return this;
    }

    public String[] getJsonbArray() {
        return jsonbArray;
    }

    public Postgre setJsonbArray(String[] jsonbArray) {
        this.jsonbArray = jsonbArray;
        return this;
    }

    public String[] getXmlArray() {
        return xmlArray;
    }

    public Postgre setXmlArray(String[] xmlArray) {
        this.xmlArray = xmlArray;
        return this;
    }

    public UUID[] getUuidArray() {
        return uuidArray;
    }

    public Postgre setUuidArray(UUID[] uuidArray) {
        this.uuidArray = uuidArray;
        return this;
    }

    public SqlRecord[] getRecordArray() {
        return recordArray;
    }

    public Postgre setRecordArray(SqlRecord[] recordArray) {
        this.recordArray = recordArray;
        return this;
    }

    public String[] getAclitemArray() {
        return aclitemArray;
    }

    public Postgre setAclitemArray(String[] aclitemArray) {
        this.aclitemArray = aclitemArray;
        return this;
    }

    public ProductInfo[] getProductInfoArray() {
        return productInfoArray;
    }

    public Postgre setProductInfoArray(ProductInfo[] productInfoArray) {
        this.productInfoArray = productInfoArray;
        return this;
    }

    public float[][] getVectorArray() {
        return vectorArray;
    }

    public Postgre setVectorArray(float[][] vectorArray) {
        this.vectorArray = vectorArray;
        return this;
    }
}
