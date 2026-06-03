package com.librarymanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.librarymanagement.entity.Book;
import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.specification.BookSpecifications;

@Service
public class BookService {

    /** Kích thước trang mặc định. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final BookRepository bookRepository;
    private final StorageService storageService;

    public BookService(BookRepository bookRepository, StorageService storageService) {
        this.bookRepository = bookRepository;
        this.storageService = storageService;
    }

    // ── Phân trang — REST API ─────────────────────────────────────────────────

    public Page<Book> getAllBooks(int page, int size, String sort, boolean asc) {
        size = Math.min(size, 100);
        Sort.Direction dir = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        return bookRepository.findAll(pageable);
    }

    public Page<Book> getAllBooks(int page, int size) {
        return getAllBooks(page, size, "id", true);
    }

    // ── Console helpers ───────────────────────────────────────────────────────

    /** Trang đầu tiên — dùng cho Console list. */
    public Page<Book> getFirstPage() {
        return getAllBooks(0, DEFAULT_PAGE_SIZE, "id", true);
    }

    // ── Export — CHỈ dùng nội bộ (StorageService) ────────────────────────────

    /** ⚠️ INTERNAL USE ONLY */
    public List<Book> findAllForExport() {
        return bookRepository.findAll(Sort.by("id"));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

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

    // ── Tìm kiếm ─────────────────────────────────────────────────────────────

    /**
     * Tìm kiếm theo từ khóa — CÓ PHÂN TRANG (REST API).
     */
    public Page<Book> searchBooks(String query, int page, int size) {
        if (query == null || query.isBlank()) return Page.empty();
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("title"));
        Specification<Book> spec = Specification.where(BookSpecifications.fullTextContains(query.trim()));
        return bookRepository.findAll(spec, pageable);
    }

    /**
     * Tìm kiếm theo từ khóa — trả List trang đầu (Console).
     */
    public List<Book> searchBooks(String query) {
        if (query == null || query.isBlank()) return List.of();
        Specification<Book> spec = Specification.where(BookSpecifications.fullTextContains(query.trim()));
        return bookRepository.findAll(spec, Sort.by("title")).stream().limit(DEFAULT_PAGE_SIZE).toList();
    }

    /**
     * Tìm kiếm với bộ lọc kết hợp — trả List trang đầu (Console).
     *
     * @param query         từ khóa tìm kiếm
     * @param genre         genre cần lọc, null = không lọc genre
     * @param availableOnly true = còn sách, false = hết sách, null = không lọc availability
     */
    public List<Book> searchWithFilters(String query, String genre, Boolean availableOnly) {
        if (query == null || query.isBlank()) return List.of();
        Specification<Book> spec = Specification.where(BookSpecifications.fullTextContains(query.trim()))
                .and(BookSpecifications.genreEqualsIgnoreCase(genre))
                .and(BookSpecifications.availabilityEquals(availableOnly));
        return bookRepository.findAll(spec, Sort.by("title")).stream().limit(DEFAULT_PAGE_SIZE).toList();
    }

    /**
     * Lấy danh sách genre phân biệt có trong kết quả search.
     * Dùng để build menu filter genre cho Console.
     */
    public List<String> getGenresFromSearch(String query) {
        if (query == null || query.isBlank()) return List.of();
        Specification<Book> spec = Specification.where(BookSpecifications.fullTextContains(query.trim()));
        return bookRepository.findAll(spec, Sort.by("genre"))
                .stream()
                .map(Book::getGenre)
                .filter(g -> g != null && !g.isBlank())
                .map(String::strip)
                .distinct()
                .sorted()
                .toList();
    }
}
