package com.dto.project.domain.bookmark.controller;

import com.dto.project.domain.bookmark.dto.BookmarkResponse;
import com.dto.project.domain.bookmark.service.BookmarkService;
import com.dto.project.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final SecurityUtil securityUtil;

    @PostMapping("/{productId}")
    public String addBookmark(
            @PathVariable Long productId
    ) {
        Long memberId = securityUtil.resolveMemberId();
        bookmarkService.addBookmark(memberId, productId);
        return "찜 추가 완료";
    }

    @DeleteMapping("/{productId}")
    public String removeBookmark(
            @PathVariable Long productId
    ) {
        Long memberId = securityUtil.resolveMemberId();
        bookmarkService.removeBookmark(memberId, productId);
        return "찜 삭제 완료";
    }

    @GetMapping
    public Page<BookmarkResponse> getBookmarks(
            @RequestParam(required = false) Long categoryId,
            Pageable pageable
    ) {
        Long memberId = securityUtil.resolveMemberId();
        return bookmarkService.getBookmarks(memberId, categoryId, pageable);
    }
}