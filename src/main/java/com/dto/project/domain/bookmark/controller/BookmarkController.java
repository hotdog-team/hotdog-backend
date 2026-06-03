package com.dto.project.domain.bookmark.controller;

import com.dto.project.domain.bookmark.dto.BookmarkResponse;
import com.dto.project.domain.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{productId}")
    public String addBookmark(
            @RequestParam Long memberId,
            @PathVariable Long productId
    ) {
        bookmarkService.addBookmark(memberId, productId);
        return "찜 추가 완료";
    }

    @DeleteMapping("/{productId}")
    public String removeBookmark(
            @RequestParam Long memberId,
            @PathVariable Long productId
    ) {
        bookmarkService.removeBookmark(memberId, productId);
        return "찜 삭제 완료";
    }

    @GetMapping
    public Page<BookmarkResponse> getBookmarks(
            @RequestParam Long memberId,
            Pageable pageable
    ) {
        return bookmarkService.getBookmarks(memberId, pageable);
    }
}