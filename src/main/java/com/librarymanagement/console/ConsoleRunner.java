package com.librarymanagement.console;

import java.util.List;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.entity.Member;
import com.librarymanagement.service.BookService;
import com.librarymanagement.service.BorrowService;
import com.librarymanagement.service.MemberService;
import com.librarymanagement.service.StorageMode;
import com.librarymanagement.service.StorageService;

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
            System.out.println("1. Quản lý sách");
            System.out.println("2. Quản lý thành viên");
            System.out.println("3. Quản lý mượn/trả");
            System.out.println("4. Thay đổi định dạng lưu trữ");
            System.out.println("5. Thoát");
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
        System.out.println("\n--- Quản lý sách ---");
        System.out.println("1. Thêm sách");
        System.out.println("2. Tìm sách");
        System.out.println("3. Danh sách sách");
        System.out.println("4. Cập nhật sách");
        System.out.println("5. Xóa sách");
        System.out.println("6. Quay lại");
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
        System.out.print("ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Tiêu đề: ");
        String title = scanner.nextLine().trim();
        System.out.print("Tác giả: ");
        String author = scanner.nextLine().trim();
        System.out.print("Thể loại: ");
        String genre = scanner.nextLine().trim();
        System.out.print("Số lượng: ");
        int copies = readInt();

        Book book = new Book(id, title, author, genre, copies);
        boolean success = bookService.addBook(book);
        System.out.println(success ? "Sách đã được thêm thành công!" : "ID sách đã tồn tại!");
    }

    private void searchBooks() {
        System.out.print("Tìm kiếm (tiêu đề/tác giả/thể loại): ");
        String query = scanner.nextLine().trim();
        if (query.isEmpty()) {
            System.out.println("Vui lòng nhập từ khóa tìm kiếm.");
            return;
        }

        // ── Kiểm tra có kết quả không trước khi cho chọn filter ───────────────
        List<Book> initial = bookService.searchBooks(query);
        if (initial.isEmpty()) {
            System.out.println("Không tìm thấy sách nào phù hợp với: \"" + query + "\"");
            return;
        }

        // ── Bước 1: Lọc theo Genre ────────────────────────────────────────────
        String selectedGenre = chooseGenreFilter(query);
        if (selectedGenre == null) return; // user bấm Exit

        // ── Bước 2: Lọc theo Availability ────────────────────────────────────
        java.util.Optional<Boolean> availFilter = chooseAvailabilityFilterOpt();
        if (availFilter == null) return; // user bấm Exit

        // ── Hiện kết quả cuối sau cả 2 bước filter ───────────────────────────
        Boolean avail = availFilter.orElse(null); // empty Optional = không lọc
        List<Book> results = bookService.searchWithFilters(query, selectedGenre, avail);

        // Build label mô tả filter đã chọn
        String genreLabel  = (selectedGenre != null) ? "[" + selectedGenre + "]" : "[All genres]";
        String availLabel  = (avail == null)  ? "[All]"
                           : (avail)          ? "[Available]" : "[Fully borrowed]";

        System.out.println("\n─────────────────────────────────────────────────────────");
        System.out.println(" Results for \"" + query + "\"  Genre: " + genreLabel +
                           "  Availability: " + availLabel);
        System.out.println("─────────────────────────────────────────────────────────");

        if (results.isEmpty()) {
            System.out.println(" Không tìm thấy sách nào phù hợp.");
        } else {
            TablePrinter.printBooks(results);
        }
    }

    /**
     * Hiện menu chọn genre.
     * @return genre được chọn, hoặc "" nếu không lọc, hoặc null nếu Exit
     */
    private String chooseGenreFilter(String query) {
        List<String> genres = bookService.getGenresFromSearch(query);

        System.out.println("\n┌─ Bước 1 trên 2: Lọc theo Thể loại───────────────────────┐");
        if (genres.isEmpty()) {
            System.out.println("│  (Không có thể loại nào phù hợp)                       │");
            System.out.println("└─────────────────────────────────────────────────────────┘");
            return ""; // không có genre → bỏ qua bước này
        }

        for (int i = 0; i < genres.size(); i++) {
            System.out.printf("│  %2d. %-51s│%n", i + 1, genres.get(i));
        }
        System.out.printf("│  %2d. %-51s│%n", genres.size() + 1, "Không lọc)");
        System.out.printf("│  %2d. %-51s│%n", genres.size() + 2, "Thoát");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.print("Choose: ");

        int choice = readInt(1, genres.size() + 2);
        if (choice == genres.size() + 2) return null;            // Exit
        if (choice == genres.size() + 1) return "";              // No filter
        return genres.get(choice - 1);                           // Genre cụ thể
    }

    /**
     * Hiện menu chọn availability.
     * @return Optional.of(true) = còn sách, Optional.of(false) = hết sách,
     *         Optional.empty() = không lọc, null = Exit
     */
    private java.util.Optional<Boolean> chooseAvailabilityFilterOpt() {
        System.out.println("\n┌─ Bước 2 trên 2: Lọc theo Tình trạng  ──────────────────┐");
        System.out.println("│   1. Còn                                               │");
        System.out.println("│   2. Đã mượn hết                                       │");
        System.out.println("│   3. Không lọc                                         │");
        System.out.println("│   4. Thoát                                             │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.print("Choose: ");

        int choice = readInt(1, 4);
        return switch (choice) {
            case 1 -> java.util.Optional.of(true);
            case 2 -> java.util.Optional.of(false);
            case 3 -> java.util.Optional.empty();
            default -> null; // Exit
        };
    }



    private void listBooks() {
        // Chỉ lấy trang đầu — không dump toàn bộ bảng ra terminal
        Page<Book> page = bookService.getFirstPage();
        if (page.isEmpty()) {
            System.out.println("Không tìm thấy sách nào.");
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
        System.out.print("Id sách cần cập nhật: ");
        String id = scanner.nextLine().trim();
        System.out.print("Cập nhật tiêu đề: ");
        String title = scanner.nextLine().trim();
        System.out.print("Cập nhật tác giả: ");
        String author = scanner.nextLine().trim();
        System.out.print("Cập nhật thể loại: ");
        String genre = scanner.nextLine().trim();
        System.out.print("Cập nhật số lượng bản sao: ");
        int copies = readInt();

        boolean success = bookService.updateBook(id, title, author, genre, copies);
        System.out.println(success ? "Book updated successfully!" : "Book not found!");
    }

    private void deleteBook() {
        System.out.print("Id sách cần xóa: ");
        String id = scanner.nextLine().trim();
        boolean success = bookService.deleteBook(id);
        System.out.println(success ? "Book deleted successfully!" : "Book not found!");
    }

    // ──────────────────── MEMBERS ────────────────────

    private void manageMembers() {
        System.out.println("\n--- Quản lý thành viên ---");
        System.out.println("1. Thêm thành viên");
        System.out.println("2. Danh sách thành viên");
        System.out.println("3. Cập nhật thành viên");
        System.out.println("4. Xóa thành viên");
        System.out.println("5. Quay lại");
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
        System.out.println(success ? "Thành viên đã được thêm thành công!" : "ID thành viên đã tồn tại!");
    }

    private void listMembers() {
        Page<Member> page = memberService.getFirstPage();
        if (page.isEmpty()) {
            System.out.println("Không tìm thấy thành viên nào.");
            return;
        }
        System.out.println("\n--- Danh sách thành viên (Trang 1/" + page.getTotalPages() +
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
        System.out.println(success ? "Thành viên đã được cập nhật thành công!" : "Thành viên không tồn tại!");
    }

    private void deleteMember() {
        System.out.print("ID thành viên cần xóa: ");
        String id = scanner.nextLine().trim();
        boolean success = memberService.deleteMember(id);
        System.out.println(success ? "Thành viên đã được xóa thành công!" : "Thành viên không tồn tại!");
    }

    // ──────────────────── BORROWING ────────────────────

    private void manageBorrowing() {
        System.out.println("\n--- Quản lý mượn sách ---");
        System.out.println("1. Mượn sách");
        System.out.println("2. Trả sách");
        System.out.println("3. Xem tất cả các bản ghi mượn");
        System.out.println("4. Quay lại");
        int choice = readInt(1, 4);

        switch (choice) {
            case 1 -> borrowBook();
            case 2 -> returnBook();
            case 3 -> listBorrowRecords();
        }
    }

    private void borrowBook() {
        System.out.print("Id thành viên: ");
        String memberId = scanner.nextLine().trim();
        System.out.print("Id sách: ");
        String bookId = scanner.nextLine().trim();
        System.out.println(borrowService.borrowBook(memberId, bookId));
    }

    private void returnBook() {
        System.out.print("Id thành viên: ");
        String memberId = scanner.nextLine().trim();
        System.out.print("Id sách: ");
        String bookId = scanner.nextLine().trim();
        System.out.println(borrowService.returnBook(memberId, bookId));
    }

    private void listBorrowRecords() {
        Page<BorrowRecord> page = borrowService.getFirstPage();
        if (page.isEmpty()) {
            System.out.println("Không tìm thấy bản ghi mượn nào.");
            return;
        }
        System.out.println("\n--- Bản ghi mượn (Trang 1/" + page.getTotalPages() +
                ", total: " + page.getTotalElements() + ") ---");
        TablePrinter.printBorrowRecords(page.getContent());
        if (page.hasNext()) {
            System.out.println("[Hint] Use GET /api/borrow?page=1&size=20 to see more.");
        }
    }

    // ──────────────────── STORAGE ────────────────────

    private void chooseStorage() {
        System.out.println("\n--- Chọn định dạng lưu trữ ---");
        System.out.println("1. DATABASE (PostgreSQL)");
        System.out.println("2. MEMORY");
        System.out.println("3. TXT");
        System.out.println("4. EXCEL");
        System.out.println("5. Quay lại");
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
            System.out.println("Đã chuyển đổi định dạng lưu trữ sang: " + mode);
        }
    }

    // ──────────────────── HELPERS ────────────────────

    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Số không hợp lệ. Vui lòng thử lại: ");
            }
        }
    }

    private int readInt(int min, int max) {
        while (true) {
            int value = readInt();
            if (value >= min && value <= max) return value;
            System.out.print("Vui lòng nhập số giữa " + min + " và " + max + ": ");
        }
    }
}
