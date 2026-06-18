package com.dto.project.domain.bookmark.dto;

import com.dto.project.domain.bookmark.entity.Bookmark;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.entity.ProductImage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkResponse {

    private Long bookmarkId;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Double averageRate;
    private Integer reviewCount;
    private Integer originPrice;
    private Integer discountRate;
    private Integer salePrice;
    private Long categoryId;
    private String status;

    public static BookmarkResponse from(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .bookmarkId(bookmark.getId())
                .productId(bookmark.getProduct().getId())
                .productName(bookmark.getProduct().getName())
                .reviewCount(bookmark.getProduct().getReviewCount())
                .averageRate(bookmark.getProduct().getAverageRate())
                .originPrice(bookmark.getProduct().getPrice())
                .salePrice(bookmark.getProduct().getSalePrice())
                .discountRate(bookmark.getProduct().getDiscountRate())
                .categoryId(bookmark.getProduct().getCategoryId())
                .status(bookmark.getProduct().getStatus())
                .imageUrl(resolveImageUrl(bookmark.getProduct()))
                .build();
    }

    private static String resolveImageUrl(Product product) {
        if (product.getImages() == null) return "";
        return product.getImages().stream()
                .filter(ProductImage::isMain)
                .map(ProductImage::getImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElseGet(() -> product.getImages().stream()
                        .map(ProductImage::getImageUrl)
                        .filter(url -> url != null && !url.isBlank())
                        .findFirst()
                        .orElse(""));
    }
}