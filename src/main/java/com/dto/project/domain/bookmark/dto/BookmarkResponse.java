package com.dto.project.domain.bookmark.dto;

import com.dto.project.domain.bookmark.entity.Bookmark;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkResponse {

    private Long bookmarkId;
    private Long productId;
    private String productName;
    private Integer price;

    public static BookmarkResponse from(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .bookmarkId(bookmark.getId())
                .productId(bookmark.getProduct().getId())
                .productName(bookmark.getProduct().getName())
                .price(bookmark.getProduct().getPrice())
                .build();
    }
}