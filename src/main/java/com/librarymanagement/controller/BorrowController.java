package com.librarymanagement.controller;

import com.librarymanagement.dto.ApiResponse;
import com.librarymanagement.dto.BorrowRequest;
import com.librarymanagement.dto.PageResponse;
import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.service.BorrowService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * BorrowController — REST API cho quản lý mượn / trả sách.
 *
 * Base URL: /api/borrow
 *
 * Tất cả các endpoint trả danh sách đều hỗ trợ phân trang qua query params:
 *   ?page=0&size=20&sort=borrowDate&asc=false
 */
@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * GET /api/borrow                          - Lịch sử mượn sách (phân trang)
     * GET /api/borrow?page=0&size=50&sort=borrowDate&asc=false
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BorrowRecord>>> getAllRecords(
            @RequestParam(defaultValue = "0")           int     page,
            @RequestParam(defaultValue = "20")          int     size,
            @RequestParam(defaultValue = "borrowDate")  String  sort,
            @RequestParam(defaultValue = "false")       boolean asc) {

        Page<BorrowRecord> recordPage = borrowService.getRecords(page, size, sort, asc);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(recordPage)));
    }

    /**
     * POST /api/borrow - Mượn sách
     * Body: { "memberId":"M001", "bookId":"B001" }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> borrowBook(@Valid @RequestBody BorrowRequest req) {
        String result = borrowService.borrowBook(req.getMemberId(), req.getBookId());
        if (!result.equals("Borrowed successfully.")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(result));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(result, null));
    }

    /**
     * POST /api/borrow/return - Trả sách
     * Body: { "memberId":"M001", "bookId":"B001" }
     */
    @PostMapping("/return")
    public ResponseEntity<ApiResponse<Void>> returnBook(@Valid @RequestBody BorrowRequest req) {
        String result = borrowService.returnBook(req.getMemberId(), req.getBookId());
        if (!result.equals("Returned successfully.")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(result));
        }
        return ResponseEntity.ok(ApiResponse.ok(result, null));
    }
}
