package com.librarymanagement.console;

import java.util.List;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.entity.Member;

/**
 * TablePrinter - In dữ liệu dạng bảng ra console.
 * Copy nguyên từ project Java gốc.
 */
public class TablePrinter {

    public static void printBooks(List<?> books) {
        if (books == null || books.isEmpty()) {
            System.out.println("No data found.");
            return;
        }

        String[] headers = { "ID", "Title", "Author", "Genre", "Copies", "Borrowed" };
        int[]    widths  = { 12, 30, 25, 15, 10, 10 };

        printTableHeader(headers, widths);
        for (Object obj : books) printRow(getBookRow(obj), widths);
        printTableFooter(widths, books.size());
    }

    public static void printMembers(List<?> members) {
        if (members == null || members.isEmpty()) {
            System.out.println("No data found.");
            return;
        }

        String[] headers = { "ID", "Name", "Email", "Phone", "Join Date" };
        int[]    widths  = { 12, 25, 35, 15, 15 };

        printTableHeader(headers, widths);
        for (Object obj : members) printRow(getMemberRow(obj), widths);
        printTableFooter(widths, members.size());
    }

    public static void printBorrowRecords(List<?> records) {
        if (records == null || records.isEmpty()) {
            System.out.println("No data found.");
            return;
        }

        String[] headers = {
            "Record ID", "Book ID", "Book Title", "Member ID",
            "Member Name", "Borrow Date", "Due Date", "Return Date", "Status"
        };
        int[] widths = { 12, 12, 25, 12, 20, 12, 12, 12, 12 };

        printTableHeader(headers, widths);
        for (Object obj : records) printRow(getBorrowRecordRow(obj), widths);
        printTableFooter(widths, records.size());
    }

    // ── Private helpers ──────────────────────────────

    private static void printTableHeader(String[] headers, int[] widths) {
        printLine(widths);
        printRow(headers, widths);
        printLine(widths);
    }

    private static void printTableFooter(int[] widths, int totalRecords) {
        printLine(widths);
        System.out.println("Tổng: " + totalRecords + " bản ghi.");
    }

    private static String[] getBookRow(Object obj) {
        try {
            Book b = (Book) obj;
            return new String[]{
                safe(b.getId()), safe(b.getTitle()), safe(b.getAuthor()),
                safe(b.getGenre()), String.valueOf(b.getCopies()), String.valueOf(b.getBorrowed())
            };
        } catch (Exception e) { return errorRow(6); }
    }

    private static String[] getMemberRow(Object obj) {
        try {
            Member m = (Member) obj;
            return new String[]{
                safe(m.getId()), safe(m.getName()), safe(m.getEmail()),
                safe(m.getPhone()), safe(m.getJoinedDate())
            };
        } catch (Exception e) { return errorRow(5); }
    }

    private static String[] getBorrowRecordRow(Object obj) {
        try {
            BorrowRecord r = (BorrowRecord) obj;
            String status = r.isReturned() ? "RETURNED" : "BORROWED";
            return new String[]{
                safe(r.getId()), safe(r.getBookId()), safe(r.getBookTitle()),
                safe(r.getMemberId()), safe(r.getMemberName()),
                dateToString(r.getBorrowDate()), dateToString(r.getDueDate()),
                dateToString(r.getReturnDate()), status
            };
        } catch (Exception e) { return errorRow(9); }
    }

    private static void printRow(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < values.length; i++) sb.append(formatCell(values[i], widths[i]));
        System.out.println(sb);
    }

    private static String formatCell(String value, int width) {
        return String.format(" %-" + width + "s |", truncate(value, width));
    }

    private static void printLine(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : widths) sb.append("-".repeat(width + 2)).append("+");
        System.out.println(sb);
    }

    private static String truncate(String str, int maxLength) {
        if (str == null) return "N/A";
        return str.length() <= maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }

    private static String dateToString(Object date) {
        if (date == null) return "N/A";
        String text = date.toString();
        return text.length() >= 10 ? text.substring(0, 10) : text;
    }

    private static String safe(Object value) {
        return value == null ? "N/A" : value.toString();
    }

    private static String[] errorRow(int size) {
        String[] row = new String[size];
        for (int i = 0; i < size; i++) row[i] = "N/A";
        return row;
    }
}
