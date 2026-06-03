package com.librarymanagement.service;

import com.librarymanagement.entity.Book;
import com.librarymanagement.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    /** Kích thước trang mặc định — đủ nhỏ để RAM an toàn, đủ lớn để UX tốt. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final BookRepository bookRepository;
    private final StorageService storageService;

    public BookService(BookRepository bookRepository, StorageService storageService) {
        this.bookRepository = bookRepository;
        this.storageService = storageService;
    }

    // ── Phân trang — dùng cho REST API ───────────────────────────────────────

    /**
     * Lấy danh sách sách CÓ PHÂN TRANG — an toàn với mọi số lượng bản ghi.
     *
     * @param page trang hiện tại (0-indexed)
     * @param size số bản ghi mỗi trang (tối đa 100 để bảo vệ server)
     * @param sort tên cột sắp xếp (id | title | author | genre | copies)
     * @param asc  true = tăng dần, false = giảm dần
     */
    public Page<Book> getAllBooks(int page, int size, String sort, boolean asc) {
        size = Math.min(size, 100); // hard cap — không cho client kéo quá 100 record/request
        Sort.Direction dir = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        return bookRepository.findAll(pageable);
    }

    /**
     * Shorthand với default sort theo id tăng dần.
     */
    public Page<Book> getAllBooks(int page, int size) {
        return getAllBooks(page, size, "id", true);
    }

    // ── Console helper — lấy trang đầu để hiển thị ───────────────────────────

    /**
     * Dùng cho ConsoleRunner: chỉ lấy trang đầu tiên (DEFAULT_PAGE_SIZE records).
     * Console không nên hiển thị hàng triệu dòng ra terminal.
     */
    public Page<Book> getFirstPage() {
        return getAllBooks(0, DEFAULT_PAGE_SIZE, "id", true);
    }

    // ── Export — CHỈ dùng nội bộ (StorageService) ────────────────────────────

    /**
     * ⚠️ INTERNAL USE ONLY — chỉ gọi từ StorageService khi export TXT/Excel.
     * Không expose ra Controller hay Console.
     */
    public List<Book> findAllForExport() {
        return bookRepository.findAll(Sort.by("id"));
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @Transactional
    public boolean addBook(Book book) {
        if (bookRepository.existsById(book.getId())) return false;
        bookRepository.save(book);
        storageService.persistCurrentIfNeeded();
        return true;
    }

    public Optional<Book> getBookById(String id) {
        return bookRepository.findById(id);
    }

    @Transactional
    public boolean updateBook(String id, String title, String author, String genre, int copies) {
        Optional<Book> found = bookRepository.findById(id);
        if (found.isEmpty()) return false;
        Book book = found.get();
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setCopies(copies);
        bookRepository.save(book);
        storageService.persistCurrentIfNeeded();
        return true;
    }

    @Transactional
    public boolean deleteBook(String id) {
        if (!bookRepository.existsById(id)) return false;
        bookRepository.deleteById(id);
        storageService.persistCurrentIfNeeded();
        return true;
    }

    // ── Tìm kiếm CÓ PHÂN TRANG ───────────────────────────────────────────────

    /**
     * Tìm kiếm sách CÓ PHÂN TRANG — dùng cho REST API.
     */
    public Page<Book> searchBooks(String query, int page, int size) {
        if (query == null || query.isBlank()) return Page.empty();
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("title"));
        return bookRepository.search(query.trim(), pageable);
    }

    /**
     * Shorthand: lấy trang đầu với DEFAULT_PAGE_SIZE kết quả — dùng cho Console.
     */
    public List<Book> searchBooks(String query) {
        if (query == null || query.isBlank()) return List.of();
        return bookRepository.search(
                query.trim(),
                PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by("title"))
        ).getContent();
    }
}
