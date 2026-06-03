package com.librarymanagement.service;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.entity.Member;

import java.util.List;

public record LibrarySnapshot(List<Book> books, List<Member> members, List<BorrowRecord> records) {
}
