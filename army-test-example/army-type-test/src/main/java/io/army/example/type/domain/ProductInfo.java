package io.army.example.type.domain;

import io.army.annotation.Column;
import io.army.struct.DefinedType;
import io.army.struct.TypeCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@DefinedType(name = "product_info", category = TypeCategory.COMPOSITE, fieldOrder = {"productId", "productName", "price", "available", "releaseDate"})
public final class ProductInfo {

    @Column
    private final Long productId;

    @Column
    private final String productName;

    @Column
    private final BigDecimal price;

    @Column
    private final Boolean available;

    @Column
    private final LocalDate releaseDate;


    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Boolean getAvailable() {
        return available;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductInfo that = (ProductInfo) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(productName, that.productName) &&
                Objects.equals(price, that.price) &&
                Objects.equals(available, that.available) &&
                Objects.equals(releaseDate, that.releaseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, price, available, releaseDate);
    }

    @Override
    public String toString() {
        return "ProductInfo{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", price=" + price +
                ", available=" + available +
                ", releaseDate=" + releaseDate +
                '}';
    }
}
