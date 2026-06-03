package com.librarymanagement.controller;

import com.librarymanagement.dto.ApiResponse;
import com.librarymanagement.dto.BookRequest;
import com.librarymanagement.dto.PageResponse;
import com.librarymanagement.entity.Book;
import com.librarymanagement.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * BookController — REST API cho quản lý sách.
 *
 * Base URL: /api/books
 *
 * Tất cả các endpoint trả danh sách đều hỗ trợ phân trang qua query params:
 *   ?page=0&size=20&sort=title&asc=true
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * GET /api/books                 - Danh sách sách (phân trang)
     * GET /api/books?q=java          - Tìm kiếm sách theo từ khóa (phân trang)
     * GET /api/books?page=1&size=50  - Lấy trang 2, 50 records
     * GET /api/books?sort=title&asc=false - Sắp xếp theo title giảm dần
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Book>>> getBooks(
            @RequestParam(required = false)              String  q,
            @RequestParam(defaultValue = "0")            int     page,
            @RequestParam(defaultValue = "20")           int     size,
            @RequestParam(defaultValue = "id")           String  sort,
            @RequestParam(defaultValue = "true")         boolean asc) {

        if (q != null && !q.isBlank()) {
            Page<Book> results = bookService.searchBooks(q.trim(), page, size);
            return ResponseEntity.ok(ApiResponse.ok(
                    "Search results for: " + q + " — " + results.getTotalElements() + " found",
                    PageResponse.of(results)));
        }

        Page<Book> bookPage = bookService.getAllBooks(page, size, sort, asc);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(bookPage)));
    }

    /**
     * GET /api/books/{id} - Lấy sách theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Book>> getBookById(@PathVariable String id) {
        Optional<Book> book = bookService.getBookById(id);
        if (book.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Book not found: " + id));
        }
        return ResponseEntity.ok(ApiResponse.ok(book.get()));
    }

    /**
     * POST /api/books - Thêm sách mới
     * Body: { "id":"B004", "title":"...", "author":"...", "genre":"...", "copies":3 }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Book>> addBook(@Valid @RequestBody BookRequest req) {
        Book book = new Book(req.getId(), req.getTitle(), req.getAuthor(),
                             req.getGenre(), req.getCopies());
        boolean success = bookService.addBook(book);
        if (!success) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Book ID already exists: " + req.getId()));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book added successfully!", book));
    }

    /**
     * PUT /api/books/{id} - Cập nhật sách
     * Body: { "title":"...", "author":"...", "genre":"...", "copies":5 }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateBook(
            @PathVariable String id,
            @RequestBody BookRequest req) {
        boolean success = bookService.updateBook(
                id, req.getTitle(), req.getAuthor(),
                req.getGenre(), req.getCopies() != null ? req.getCopies() : 0);
        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Book not found: " + id));
        }
        return ResponseEntity.ok(ApiResponse.ok("Book updated successfully!", null));
    }

    /**
     * DELETE /api/books/{id} - Xóa sách
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable String id) {
        boolean success = bookService.deleteBook(id);
        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Book not found: " + id));
        }
        return ResponseEntity.ok(ApiResponse.ok("Book deleted successfully!", null));
    }

    /**
     * GET /api/books/search?q=keyword&page=0&size=20 - Tìm kiếm (endpoint riêng)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<Book>>> searchBooks(
            @RequestParam                      String  q,
            @RequestParam(defaultValue = "0")  int     page,
            @RequestParam(defaultValue = "20") int     size) {
        Page<Book> results = bookService.searchBooks(q.trim(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                "Found " + results.getTotalElements() + " book(s)",
                PageResponse.of(results)));
    }
}
