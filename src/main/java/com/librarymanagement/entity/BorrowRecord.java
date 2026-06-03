package com.librarymanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "borrow_records")
public class BorrowRecord {

    @Id
    @Column(name = "id", length = 255)
    private String id;

    @Column(name = "member_id", length = 255)
    private String memberId;

    @Column(name = "book_id", length = 255)
    private String bookId;

    @Column(name = "borrow_date", length = 255)
    private String borrowDate;

    @Column(name = "return_date", length = 255)
    private String returnDate;

    @Column(name = "due_date", length = 255)
    private String dueDate;

    // For display purposes - populated when fetching records
    @Transient
    private String bookTitle;

    @Transient
    private String memberName;

    public BorrowRecord() {
    }

    public BorrowRecord(String id, String memberId, String bookId, String borrowDate, String returnDate) {
        this.id = id;
        this.memberId = memberId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public boolean isReturned() {
        return returnDate != null && !returnDate.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("BorrowRecord[id=%s, memberId=%s, bookId=%s, borrowDate=%s, returnDate=%s]",
                id, memberId, bookId, borrowDate, returnDate);
    }
}
