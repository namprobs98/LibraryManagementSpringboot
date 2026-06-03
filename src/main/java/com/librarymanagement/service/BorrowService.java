package com.librarymanagement.service;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.entity.Member;
import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.BorrowRecordRepository;
import com.librarymanagement.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BorrowService {

    /** Kích thước trang mặc định. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final StorageService storageService;

    public BorrowService(BookRepository bookRepository,
                         MemberRepository memberRepository,
                         BorrowRecordRepository borrowRecordRepository,
                         StorageService storageService) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.storageService = storageService;
    }

    // ── Mượn / Trả ────────────────────────────────────────────────────────────

    @Transactional
    public String borrowBook(String memberId, String bookId) {
        if (!memberRepository.existsById(memberId)) return "Member ID not found.";
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return "Book ID not found.";
        if (book.getBorrowed() >= book.getCopies()) return "All copies are currently borrowed.";

        book.setBorrowed(book.getBorrowed() + 1);
        bookRepository.save(book);

        // Tạo ID dạng BR + count — dùng count() thay vì findAll().size() để tránh load bảng
        long count = borrowRecordRepository.count();
        String id = "BR" + (count + 1);
        BorrowRecord record = new BorrowRecord(id, memberId, bookId,
                LocalDate.now().toString(), null);
        borrowRecordRepository.save(record);

        storageService.persistCurrentIfNeeded();
        return "Borrowed successfully.";
    }

    @Transactional
    public String returnBook(String memberId, String bookId) {
        if (!memberRepository.existsById(memberId)) return "Member ID not found.";
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return "Book ID not found.";
        if (book.getBorrowed() <= 0) return "No copies are currently borrowed.";

        List<BorrowRecord> activeRecords = borrowRecordRepository.findActiveRecord(memberId, bookId);
        if (activeRecords.isEmpty()) return "No active borrow record found.";

        BorrowRecord open = activeRecords.get(0);
        open.setReturnDate(LocalDate.now().toString());
        borrowRecordRepository.save(open);

        book.setBorrowed(book.getBorrowed() - 1);
        bookRepository.save(book);

        storageService.persistCurrentIfNeeded();
        return "Returned successfully.";
    }

    // ── Phân trang — dùng cho REST API ───────────────────────────────────────

    /**
     * Lấy borrow records CÓ PHÂN TRANG, kèm bookTitle & memberName.
     * <p>
     * Thuật toán batch-load tránh N+1 query:
     * <ol>
     *   <li>Fetch 1 page records (1 SQL)</li>
     *   <li>Collect tất cả bookId và memberId duy nhất trong page đó</li>
     *   <li>Batch-load books theo IN clause (1 SQL)</li>
     *   <li>Batch-load members theo IN clause (1 SQL)</li>
     *   <li>Map vào record — tổng cộng chỉ 3 SQL, bất kể page size</li>
     * </ol>
     *
     * @param page trang hiện tại (0-indexed)
     * @param size số bản ghi mỗi trang (tối đa 100)
     * @param sort tên cột sắp xếp (id | memberId | bookId | borrowDate | returnDate)
     * @param asc  true = tăng dần, false = giảm dần
     */
    public Page<BorrowRecord> getRecords(int page, int size, String sort, boolean asc) {
        size = Math.min(size, 100);
        Sort.Direction dir = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<BorrowRecord> recordPage = borrowRecordRepository.findAll(pageable);
        enrichWithNames(recordPage.getContent()); // batch-load, không N+1
        return recordPage;
    }

    /**
     * Shorthand với default sort theo id tăng dần.
     */
    public Page<BorrowRecord> getRecords(int page, int size) {
        return getRecords(page, size, "id", true);
    }

    // ── Console helper ────────────────────────────────────────────────────────

    /**
     * Dùng cho ConsoleRunner: chỉ lấy trang đầu tiên.
     */
    public Page<BorrowRecord> getFirstPage() {
        return getRecords(0, DEFAULT_PAGE_SIZE, "id", true);
    }

    // ── Batch enrichment (tránh N+1) ─────────────────────────────────────────

    /**
     * Nhận 1 list records trong 1 page, batch-load book/member rồi set vào @Transient fields.
     * Chỉ tốn thêm 2 SQL bất kể list có bao nhiêu phần tử.
     */
    private void enrichWithNames(List<BorrowRecord> records) {
        if (records.isEmpty()) return;

        // Collect distinct IDs để build IN clause
        List<String> bookIds = records.stream()
                .map(BorrowRecord::getBookId)
                .distinct()
                .collect(Collectors.toList());

        List<String> memberIds = records.stream()
                .map(BorrowRecord::getMemberId)
                .distinct()
                .collect(Collectors.toList());

        // Batch load — 1 SQL mỗi loại
        Map<String, String> bookTitleMap = bookRepository.findAllById(bookIds)
                .stream()
                .collect(Collectors.toMap(Book::getId, Book::getTitle));

        Map<String, String> memberNameMap = memberRepository.findAllById(memberIds)
                .stream()
                .collect(Collectors.toMap(Member::getId, Member::getName));

        // Gán vào @Transient fields
        records.forEach(r -> {
            r.setBookTitle(bookTitleMap.getOrDefault(r.getBookId(), r.getBookId()));
            r.setMemberName(memberNameMap.getOrDefault(r.getMemberId(), r.getMemberId()));
        });
    }
}
