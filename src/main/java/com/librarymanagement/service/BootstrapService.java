package com.librarymanagement.service;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.Member;
import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.MemberRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BootstrapService - Seed dữ liệu mẫu khi khởi động app nếu DB đang trống.
 * Implements ApplicationRunner để chạy sau khi Spring context đã sẵn sàng.
 */
@Service
public class BootstrapService implements ApplicationRunner {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public BootstrapService(BookRepository bookRepository, MemberRepository memberRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedIfEmpty();
    }

    public void seedIfEmpty() {
        if (bookRepository.findAll().isEmpty()) {
            bookRepository.save(new Book("B001", "Java Basics", "John Doe", "Programming", 5));
            bookRepository.save(new Book("B002", "Python 101", "Jane Smith", "Programming", 7));
            bookRepository.save(new Book("B003", "C++ Primer", "Alex Brown", "Programming", 4));
            System.out.println("[Bootstrap] Seeded 3 sample books.");
        }
        if (memberRepository.findAll().isEmpty()) {
            memberRepository.save(new Member("M001", "Alice Johnson", "alice@example.com", "1234567890"));
            memberRepository.save(new Member("M002", "Bob Smith", "bob@example.com", "0987654321"));
            memberRepository.save(new Member("M003", "Charlie Brown", "charlie@example.com", "1122334455"));
            System.out.println("[Bootstrap] Seeded 3 sample members.");
        }
    }
}
