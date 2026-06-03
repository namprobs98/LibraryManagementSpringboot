package com.librarymanagement.dto;

import jakarta.validation.constraints.NotBlank;

public class BorrowRequest {

    @NotBlank(message = "Member ID is required")
    private String memberId;

    @NotBlank(message = "Book ID is required")
    private String bookId;

    public BorrowRequest() {}

    public String getMemberId()           { return memberId; }
    public void setMemberId(String mid)   { this.memberId = mid; }
    public String getBookId()             { return bookId; }
    public void setBookId(String bid)     { this.bookId = bid; }
}
