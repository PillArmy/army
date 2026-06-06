package io.army.example.type.domain;

import io.army.annotation.Column;
import io.army.struct.DefinedType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@DefinedType(name = "product_info",
        fieldOrder = {"productId", "productName", "price", "available", "releaseDate"})
public final class ProductInfo {

    @Column
    private Long productId;

    @Column
    private String productName;

    @Column
    private BigDecimal price;

    @Column
    private Boolean available;

    @Column
    private LocalDate releaseDate;


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
