package com.librarymanagement.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.entity.Member;
import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.BorrowRecordRepository;
import com.librarymanagement.repository.MemberRepository;
import com.librarymanagement.service.services.BorrowService;
import com.librarymanagement.service.services.StorageService;

@Service
@Primary
public class BorrowServiceImpl implements BorrowService {

       /** Kích thước trang mặc định. */
       public static final int DEFAULT_PAGE_SIZE = 20;

       private final BookRepository bookRepository;
       private final MemberRepository memberRepository;
       private final BorrowRecordRepository borrowRecordRepository;
       private final StorageService storageService;

       public BorrowServiceImpl(BookRepository bookRepository,
                                MemberRepository memberRepository,
                                BorrowRecordRepository borrowRecordRepository,
                                StorageService storageService) {
           this.bookRepository = bookRepository;
           this.memberRepository = memberRepository;
           this.borrowRecordRepository = borrowRecordRepository;
           this.storageService = storageService;
       }

       // ── Mượn / Trả ────────────────────────────────────────────────────────────

       @Override
       @Transactional
       public String borrowBook(String memberId, String bookId) {
           if (!memberRepository.existsById(memberId)) return "Member ID not found.";
           Book book = bookRepository.findById(bookId).orElse(null);
           if (book == null) return "Book ID not found.";
           if (book.getBorrowed() >= book.getCopies()) return "All copies are currently borrowed.";

           book.setBorrowed(book.getBorrowed() + 1);
           bookRepository.save(book);

           long count = borrowRecordRepository.count();
           String id = "BR" + (count + 1);
           BorrowRecord record = new BorrowRecord(id, memberId, bookId,
                   LocalDate.now().toString(), null);
           borrowRecordRepository.save(record);

           storageService.persistCurrentIfNeeded();
           return "Borrowed successfully.";
       }

       @Override
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

       @Override
       public Page<BorrowRecord> getRecords(int page, int size, String sort, boolean asc) {
           size = Math.min(size, 100);
           Sort.Direction dir = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
           Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

           Page<BorrowRecord> recordPage = borrowRecordRepository.findAll(pageable);
           enrichWithNames(recordPage.getContent()); // batch-load, không N+1
           return recordPage;
       }

       @Override
       public Page<BorrowRecord> getRecords(int page, int size) {
           return getRecords(page, size, "id", true);
       }

       // ── Console helper ────────────────────────────────────────────────────────

       @Override
       public Page<BorrowRecord> getFirstPage() {
           return getRecords(0, DEFAULT_PAGE_SIZE, "id", true);
       }

       // ── Batch enrichment (tránh N+1) ─────────────────────────────────────────

       private void enrichWithNames(List<BorrowRecord> records) {
           if (records.isEmpty()) return;

           List<String> bookIds = records.stream()
                   .map(BorrowRecord::getBookId)
                   .distinct()
                   .collect(Collectors.toList());

           List<String> memberIds = records.stream()
                   .map(BorrowRecord::getMemberId)
                   .distinct()
                   .collect(Collectors.toList());

           Map<String, String> bookTitleMap = bookRepository.findAllById(bookIds)
                   .stream()
                   .collect(Collectors.toMap(Book::getId, Book::getTitle));

           Map<String, String> memberNameMap = memberRepository.findAllById(memberIds)
                   .stream()
                   .collect(Collectors.toMap(Member::getId, Member::getName));

           records.forEach(r -> {
               r.setBookTitle(bookTitleMap.getOrDefault(r.getBookId(), r.getBookId()));
               r.setMemberName(memberNameMap.getOrDefault(r.getMemberId(), r.getMemberId()));
           });
       }
   }
