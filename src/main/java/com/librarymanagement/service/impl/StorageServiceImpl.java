package com.librarymanagement.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.entity.Member;
import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.BorrowRecordRepository;
import com.librarymanagement.repository.MemberRepository;
import com.librarymanagement.service.LibrarySnapshot;
import com.librarymanagement.service.StorageMode;
import com.librarymanagement.service.services.StorageService;

@Service
public class StorageServiceImpl implements StorageService {

    private static final String SEP = "|";
    private static final Path TXT_PATH = Paths.get("library_data_java.txt");
    private static final Path XLSX_PATH = Paths.get("library_data_java.xlsx");

    private StorageMode currentMode = StorageMode.DATABASE;

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public StorageServiceImpl(BookRepository bookRepository,
                              MemberRepository memberRepository,
                              BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    @Override
    public StorageMode getCurrentMode() {
        return currentMode;
    }

    @Override
    @Transactional
    public void switchMode(StorageMode mode) {
        if (mode == StorageMode.DATABASE) {
            currentMode = StorageMode.DATABASE;
            return;
        }
        if (mode == StorageMode.MEMORY) {
            currentMode = StorageMode.MEMORY;
            return;
        }
        if (mode == StorageMode.TXT) {
            if (Files.exists(TXT_PATH)) {
                loadFromTxt();
            } else {
                saveToTxt(buildSnapshot());
            }
            currentMode = StorageMode.TXT;
            return;
        }
        if (Files.exists(XLSX_PATH)) {
            loadFromExcel();
        } else {
            saveToExcel(buildSnapshot());
        }
        currentMode = StorageMode.EXCEL;
    }

    @Override
    public void persistCurrentIfNeeded() {
        LibrarySnapshot snapshot = buildSnapshot();
        if (currentMode == StorageMode.TXT) {
            saveToTxt(snapshot);
        } else if (currentMode == StorageMode.EXCEL) {
            saveToExcel(snapshot);
        }
    }

    @Override
    public LibrarySnapshot buildSnapshot() {
        return new LibrarySnapshot(
                bookRepository.findAll(),
                memberRepository.findAll(),
                borrowRecordRepository.findAll()
        );
    }

    private void saveToTxt(LibrarySnapshot snapshot) {
        Path temp = Paths.get(TXT_PATH + ".tmp");
        try (var writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            writer.write("[BOOKS]\n");
            for (Book b : snapshot.books()) {
                writer.write(escape(b.getId()) + SEP + escape(b.getTitle()) + SEP + escape(b.getAuthor()) + SEP
                        + escape(b.getGenre()) + SEP + b.getCopies() + SEP + b.getBorrowed() + "\n");
            }
            writer.write("[MEMBERS]\n");
            for (Member m : snapshot.members()) {
                writer.write(escape(m.getId()) + SEP + escape(m.getName()) + SEP + escape(m.getEmail()) + SEP
                        + escape(m.getPhone()) + "\n");
            }
            writer.write("[BORROW_RECORDS]\n");
            for (BorrowRecord r : snapshot.records()) {
                writer.write(escape(r.getId()) + SEP + escape(r.getMemberId()) + SEP + escape(r.getBookId()) + SEP
                        + escape(r.getBorrowDate()) + SEP + escape(r.getReturnDate()) + "\n");
            }
            Files.move(temp, TXT_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save TXT: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void loadFromTxt() {
        List<Book> books = new ArrayList<>();
        List<Member> members = new ArrayList<>();
        List<BorrowRecord> records = new ArrayList<>();
        String section = "";
        try {
            for (String line : Files.readAllLines(TXT_PATH, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                if (line.startsWith("[")) {
                    section = line.trim();
                    continue;
                }
                String[] parts = splitEscaped(line);
                switch (section) {
                    case "[BOOKS]" -> {
                        if (parts.length >= 6) {
                            Book b = new Book(unescape(parts[0]), unescape(parts[1]), unescape(parts[2]),
                                    unescape(parts[3]), Integer.parseInt(parts[4]));
                            b.setBorrowed(Integer.parseInt(parts[5]));
                            books.add(b);
                        }
                    }
                    case "[MEMBERS]" -> {
                        if (parts.length >= 4) {
                            members.add(new Member(unescape(parts[0]), unescape(parts[1]),
                                    unescape(parts[2]), unescape(parts[3])));
                        }
                    }
                    case "[BORROW_RECORDS]" -> {
                        if (parts.length >= 5) {
                            records.add(new BorrowRecord(unescape(parts[0]), unescape(parts[1]),
                                    unescape(parts[2]), unescape(parts[3]), unescape(parts[4])));
                        }
                    }
                }
            }
            bookRepository.deleteAll();
            bookRepository.saveAll(books);
            memberRepository.deleteAll();
            memberRepository.saveAll(members);
            borrowRecordRepository.deleteAll();
            borrowRecordRepository.saveAll(records);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load TXT: " + e.getMessage(), e);
        }
    }

    private void saveToExcel(LibrarySnapshot snapshot) {
        Path temp = Paths.get(XLSX_PATH + ".tmp");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet bookSheet = workbook.createSheet("books");
            writeBookHeader(bookSheet.createRow(0));
            int row = 1;
            for (Book b : snapshot.books()) {
                Row r = bookSheet.createRow(row++);
                r.createCell(0).setCellValue(b.getId());
                r.createCell(1).setCellValue(b.getTitle());
                r.createCell(2).setCellValue(b.getAuthor());
                r.createCell(3).setCellValue(b.getGenre());
                r.createCell(4).setCellValue(b.getCopies());
                r.createCell(5).setCellValue(b.getBorrowed());
            }

            Sheet memberSheet = workbook.createSheet("members");
            Row mh = memberSheet.createRow(0);
            mh.createCell(0).setCellValue("id");
            mh.createCell(1).setCellValue("name");
            mh.createCell(2).setCellValue("email");
            mh.createCell(3).setCellValue("phone");
            row = 1;
            for (Member m : snapshot.members()) {
                Row r = memberSheet.createRow(row++);
                r.createCell(0).setCellValue(m.getId());
                r.createCell(1).setCellValue(m.getName());
                r.createCell(2).setCellValue(m.getEmail());
                r.createCell(3).setCellValue(m.getPhone());
            }

            Sheet borrowSheet = workbook.createSheet("borrow_records");
            Row bh = borrowSheet.createRow(0);
            bh.createCell(0).setCellValue("id");
            bh.createCell(1).setCellValue("memberId");
            bh.createCell(2).setCellValue("bookId");
            bh.createCell(3).setCellValue("borrowDate");
            bh.createCell(4).setCellValue("returnDate");
            row = 1;
            for (BorrowRecord record : snapshot.records()) {
                Row r = borrowSheet.createRow(row++);
                r.createCell(0).setCellValue(record.getId());
                r.createCell(1).setCellValue(record.getMemberId());
                r.createCell(2).setCellValue(record.getBookId());
                r.createCell(3).setCellValue(record.getBorrowDate());
                r.createCell(4).setCellValue(safe(record.getReturnDate()));
            }

            try (OutputStream os = Files.newOutputStream(temp)) {
                workbook.write(os);
            }
            Files.move(temp, XLSX_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save EXCEL: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void loadFromExcel() {
        List<Book> books = new ArrayList<>();
        List<Member> members = new ArrayList<>();
        List<BorrowRecord> records = new ArrayList<>();
        try (InputStream is = Files.newInputStream(XLSX_PATH);
             Workbook workbook = new XSSFWorkbook(is)) {
            readBooks(workbook.getSheet("books"), books);
            readMembers(workbook.getSheet("members"), members);
            readRecords(workbook.getSheet("borrow_records"), records);
            bookRepository.deleteAll();
            bookRepository.saveAll(books);
            memberRepository.deleteAll();
            memberRepository.saveAll(members);
            borrowRecordRepository.deleteAll();
            borrowRecordRepository.saveAll(records);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load EXCEL: " + e.getMessage(), e);
        }
    }

    private static void writeBookHeader(Row header) {
        header.createCell(0).setCellValue("id");
        header.createCell(1).setCellValue("title");
        header.createCell(2).setCellValue("author");
        header.createCell(3).setCellValue("genre");
        header.createCell(4).setCellValue("copies");
        header.createCell(5).setCellValue("borrowed");
    }

    private static void readBooks(Sheet sheet, List<Book> out) {
        if (sheet == null) return;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Book b = new Book(text(row.getCell(0)), text(row.getCell(1)), text(row.getCell(2)),
                    text(row.getCell(3)), (int) number(row.getCell(4)));
            b.setBorrowed((int) number(row.getCell(5)));
            if (!b.getId().isBlank()) out.add(b);
        }
    }

    private static void readMembers(Sheet sheet, List<Member> out) {
        if (sheet == null) return;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Member m = new Member(text(row.getCell(0)), text(row.getCell(1)),
                    text(row.getCell(2)), text(row.getCell(3)));
            if (!m.getId().isBlank()) out.add(m);
        }
    }

    private static void readRecords(Sheet sheet, List<BorrowRecord> out) {
        if (sheet == null) return;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            BorrowRecord r = new BorrowRecord(text(row.getCell(0)), text(row.getCell(1)),
                    text(row.getCell(2)), text(row.getCell(3)), text(row.getCell(4)));
            if (!r.getId().isBlank()) out.add(r);
        }
    }

    private static String text(Cell cell) {
        return cell == null ? "" : switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private static double number(Cell cell) {
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> Double.parseDouble(cell.getStringCellValue());
            default -> 0;
        };
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String escape(String s) {
        return safe(s).replace("\\", "\\\\").replace("|", "\\|");
    }

    private static String unescape(String s) {
        StringBuilder out = new StringBuilder();
        boolean esc = false;
        for (char c : s.toCharArray()) {
            if (esc) {
                out.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String[] splitEscaped(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean esc = false;
        for (char c : line.toCharArray()) {
            if (esc) {
                cur.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == '|') {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        parts.add(cur.toString());
        return parts.toArray(String[]::new);
    }
}
