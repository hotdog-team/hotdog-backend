package com.dto.project.domain.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_images")
@Getter
@Setter
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_main")
    private String isMain;

    public boolean isMain() {
        return "Y".equalsIgnoreCase(this.isMain);
    }

    public void setMain(boolean main) {
        this.isMain = main ? "Y" : "N";
    }
}