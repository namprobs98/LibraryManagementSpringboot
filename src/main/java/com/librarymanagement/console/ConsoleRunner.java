package com.librarymanagement.console;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.entity.Member;
import com.librarymanagement.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

/**
 * ConsoleRunner — Giao diện console.
 *
 * Các hàm listXxx() giờ chỉ lấy trang đầu tiên (DEFAULT_PAGE_SIZE records)
 * thay vì dump toàn bộ bảng ra terminal. Nếu còn nhiều trang, console
 * thông báo để người dùng biết dùng REST API để lấy thêm.
 */
@Component
public class ConsoleRunner implements CommandLineRunner {

    private final BookService bookService;
    private final MemberService memberService;
    private final BorrowService borrowService;
    private final StorageService storageService;
    private final Scanner scanner;

    public ConsoleRunner(BookService bookService,
                         MemberService memberService,
                         BorrowService borrowService,
                         StorageService storageService) {
        this.bookService    = bookService;
        this.memberService  = memberService;
        this.borrowService  = borrowService;
        this.storageService = storageService;
        this.scanner        = new Scanner(System.in);
    }

    @Override
    public void run(String... args) {
        int choice = 0;
        while (choice != 5) {
            System.out.println("\n=== Library Management (" + storageService.getCurrentMode() + ") ===");
            System.out.println("1. Manage books");
            System.out.println("2. Manage members");
            System.out.println("3. Manage borrowing");
            System.out.println("4. Change storage format");
            System.out.println("5. Exit");
            choice = readInt(1, 5);

            switch (choice) {
                case 1 -> manageBooks();
                case 2 -> manageMembers();
                case 3 -> manageBorrowing();
                case 4 -> chooseStorage();
                case 5 -> System.out.println("Goodbye!");
            }
        }
    }

    // ──────────────────── BOOKS ────────────────────

    private void manageBooks() {
        System.out.println("\n--- Manage Books ---");
        System.out.println("1. Add book");
        System.out.println("2. Search book");
        System.out.println("3. List all books");
        System.out.println("4. Update book");
        System.out.println("5. Delete book");
        System.out.println("6. Back");
        int choice = readInt(1, 6);

        switch (choice) {
            case 1 -> addBook();
            case 2 -> searchBooks();
            case 3 -> listBooks();
            case 4 -> updateBook();
            case 5 -> deleteBook();
        }
    }

    private void addBook() {
        System.out.print("Book ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("Genre: ");
        String genre = scanner.nextLine().trim();
        System.out.print("Copies: ");
        int copies = readInt();

        Book book = new Book(id, title, author, genre, copies);
        boolean success = bookService.addBook(book);
        System.out.println(success ? "Book added successfully!" : "Book ID already exists!");
    }

    private void searchBooks() {
        System.out.print("Search (title/author/genre): ");
        String query = scanner.nextLine().trim();
        if (query.isEmpty()) {
            System.out.println("Please enter a search term.");
            return;
        }

        // searchBooks(query) đã giới hạn trang đầu, DEFAULT_PAGE_SIZE records
        List<Book> results = bookService.searchBooks(query);
        if (results.isEmpty()) {
            System.out.println("No books found matching: " + query);
            return;
        }
        System.out.println("\n--- Search Results (showing up to " +
                BookService.DEFAULT_PAGE_SIZE + " results) ---");
        TablePrinter.printBooks(results);
    }

    private void listBooks() {
        // Chỉ lấy trang đầu — không dump toàn bộ bảng ra terminal
        Page<Book> page = bookService.getFirstPage();
        if (page.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.println("\n--- Books (page 1/" + page.getTotalPages() +
                ", total: " + page.getTotalElements() + ") ---");
        TablePrinter.printBooks(page.getContent());
        if (page.hasNext()) {
            System.out.println("[Hint] Use GET /api/books?page=1&size=20 to see more.");
        }
    }

    private void updateBook() {
        System.out.print("Book ID to update: ");
        String id = scanner.nextLine().trim();
        System.out.print("New Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("New Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("New Genre: ");
        String genre = scanner.nextLine().trim();
        System.out.print("New Copies: ");
        int copies = readInt();

        boolean success = bookService.updateBook(id, title, author, genre, copies);
        System.out.println(success ? "Book updated successfully!" : "Book not found!");
    }

    private void deleteBook() {
        System.out.print("Book ID to delete: ");
        String id = scanner.nextLine().trim();
        boolean success = bookService.deleteBook(id);
        System.out.println(success ? "Book deleted successfully!" : "Book not found!");
    }

    // ──────────────────── MEMBERS ────────────────────

    private void manageMembers() {
        System.out.println("\n--- Manage Members ---");
        System.out.println("1. Add member");
        System.out.println("2. List all members");
        System.out.println("3. Update member");
        System.out.println("4. Delete member");
        System.out.println("5. Back");
        int choice = readInt(1, 5);

        switch (choice) {
            case 1 -> addMember();
            case 2 -> listMembers();
            case 3 -> updateMember();
            case 4 -> deleteMember();
        }
    }

    private void addMember() {
        System.out.print("Member ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();

        Member member = new Member(id, name, email, phone);
        boolean success = memberService.addMember(member);
        System.out.println(success ? "Member added successfully!" : "Member ID already exists!");
    }

    private void listMembers() {
        Page<Member> page = memberService.getFirstPage();
        if (page.isEmpty()) {
            System.out.println("No members found.");
            return;
        }
        System.out.println("\n--- Members (page 1/" + page.getTotalPages() +
                ", total: " + page.getTotalElements() + ") ---");
        TablePrinter.printMembers(page.getContent());
        if (page.hasNext()) {
            System.out.println("[Hint] Use GET /api/members?page=1&size=20 to see more.");
        }
    }

    private void updateMember() {
        System.out.print("Member ID to update: ");
        String id = scanner.nextLine().trim();
        System.out.print("New Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("New Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("New Phone: ");
        String phone = scanner.nextLine().trim();

        boolean success = memberService.updateMember(id, name, email, phone);
        System.out.println(success ? "Member updated successfully!" : "Member not found!");
    }

    private void deleteMember() {
        System.out.print("Member ID to delete: ");
        String id = scanner.nextLine().trim();
        boolean success = memberService.deleteMember(id);
        System.out.println(success ? "Member deleted successfully!" : "Member not found!");
    }

    // ──────────────────── BORROWING ────────────────────

    private void manageBorrowing() {
        System.out.println("\n--- Manage Borrowing ---");
        System.out.println("1. Borrow book");
        System.out.println("2. Return book");
        System.out.println("3. List all borrow records");
        System.out.println("4. Back");
        int choice = readInt(1, 4);

        switch (choice) {
            case 1 -> borrowBook();
            case 2 -> returnBook();
            case 3 -> listBorrowRecords();
        }
    }

    private void borrowBook() {
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();
        System.out.print("Book ID: ");
        String bookId = scanner.nextLine().trim();
        System.out.println(borrowService.borrowBook(memberId, bookId));
    }

    private void returnBook() {
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();
        System.out.print("Book ID: ");
        String bookId = scanner.nextLine().trim();
        System.out.println(borrowService.returnBook(memberId, bookId));
    }

    private void listBorrowRecords() {
        Page<BorrowRecord> page = borrowService.getFirstPage();
        if (page.isEmpty()) {
            System.out.println("No borrow records found.");
            return;
        }
        System.out.println("\n--- Borrow Records (page 1/" + page.getTotalPages() +
                ", total: " + page.getTotalElements() + ") ---");
        TablePrinter.printBorrowRecords(page.getContent());
        if (page.hasNext()) {
            System.out.println("[Hint] Use GET /api/borrow?page=1&size=20 to see more.");
        }
    }

    // ──────────────────── STORAGE ────────────────────

    private void chooseStorage() {
        System.out.println("\n--- Change Storage Format ---");
        System.out.println("1. DATABASE (PostgreSQL)");
        System.out.println("2. MEMORY");
        System.out.println("3. TXT");
        System.out.println("4. EXCEL");
        System.out.println("5. Back");
        int choice = readInt(1, 5);

        StorageMode mode = switch (choice) {
            case 1 -> StorageMode.DATABASE;
            case 2 -> StorageMode.MEMORY;
            case 3 -> StorageMode.TXT;
            case 4 -> StorageMode.EXCEL;
            default -> null;
        };

        if (mode != null) {
            storageService.switchMode(mode);
            System.out.println("Storage mode changed to: " + mode);
        }
    }

    // ──────────────────── HELPERS ────────────────────

    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
    }

    private int readInt(int min, int max) {
        while (true) {
            int value = readInt();
            if (value >= min && value <= max) return value;
            System.out.print("Please enter between " + min + " and " + max + ": ");
        }
    }
}
