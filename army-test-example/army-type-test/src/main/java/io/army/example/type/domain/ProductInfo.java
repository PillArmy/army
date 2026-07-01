package io.army.example.type.domain;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;
import io.army.pojo.FieldAccessPojo;
import io.army.struct.DefinedType;
import io.army.util._StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

@MappedSuperclass
@DefinedType(name = "PRODUCT_INFO",
        fieldOrder = {"productId", "productName", "price", "available", "releaseDate", "intArray", "textArray", "managerInfo"})
public class ProductInfo implements FieldAccessPojo {

    @Column
    public Long productId;

    @Column
    public String productName;

    @Column
    public BigDecimal price;

    @Column
    public Boolean available = Boolean.TRUE; // default value for testing null

    @Column
    public LocalDate releaseDate;

    @Column
    public int[] intArray;

    @Column
    public String[] textArray;

    @Column
    public ManagerInfo managerInfo;


    public Long getProductId() {
        return productId;
    }

    public ProductInfo setProductId(Long productId) {
        this.productId = productId;
        return this;
    }

    public String getProductName() {
        return productName;
    }

    public ProductInfo setProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ProductInfo setPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public Boolean getAvailable() {
        return available;
    }

    public ProductInfo setAvailable(Boolean available) {
        this.available = available;
        return this;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public ProductInfo setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public ProductInfo setIntArray(int[] intArray) {
        this.intArray = intArray;
        return this;
    }

    public String[] getTextArray() {
        return textArray;
    }

    public ProductInfo setTextArray(String[] textArray) {
        this.textArray = textArray;
        return this;
    }

    public ManagerInfo getManagerInfo() {
        return managerInfo;
    }

    public ProductInfo setManagerInfo(ManagerInfo managerInfo) {
        this.managerInfo = managerInfo;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.productId,
                this.productName,
                this.price,
                this.available,
                this.releaseDate,
                this.intArray,
                this.textArray,
                this.managerInfo
        );
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof ProductInfo o) {
            match = obj.getClass() == this.getClass()
                    && Objects.equals(o.productId, this.productId)
                    && Objects.equals(o.productName, this.productName)
                    && Objects.equals(o.price, this.price)
                    && Objects.equals(o.available, this.available)
                    && Objects.equals(o.releaseDate, this.releaseDate)
                    && Arrays.equals(o.intArray, this.intArray)
                    && Arrays.equals(o.textArray, this.textArray)
                    && Objects.equals(o.managerInfo, this.managerInfo);
        } else {
            match = false;
        }
        return match;
    }

    @Override
    public String toString() {
        final String priceText;
        if (this.price == null) {
            priceText = "null";
        } else {
            priceText = this.price.toPlainString();
        }
        return _StringUtils.builder(60)
                .append(getClass().getName())
                .append('[')
                .append("productId:")
                .append(this.productId)
                .append(",productName:")
                .append(this.productName)
                .append(",price:")
                .append(priceText)
                .append(",available:")
                .append(this.available)
                .append(",releaseDate:")
                .append(this.releaseDate)
                .append(']')
                .toString();
    }


}
