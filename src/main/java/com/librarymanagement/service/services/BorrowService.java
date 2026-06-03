package com.librarymanagement.service.services;

import org.springframework.data.domain.Page;

import com.librarymanagement.entity.BorrowRecord;

public interface BorrowService {

    String borrowBook(String memberId, String bookId);

    String returnBook(String memberId, String bookId);

    Page<BorrowRecord> getRecords(int page, int size, String sort, boolean asc);

    Page<BorrowRecord> getRecords(int page, int size);

    Page<BorrowRecord> getFirstPage();
}
