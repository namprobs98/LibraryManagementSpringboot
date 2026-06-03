package com.librarymanagement.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * PageResponse - Wrapper cho kết quả phân trang, thay thế Spring Page<T> để trả về JSON gọn.
 */
public class PageResponse<T> {

    private List<T> content;       // Dữ liệu trang hiện tại
    private int page;              // Trang hiện tại (0-indexed)
    private int size;              // Kích thước mỗi trang
    private long totalElements;    // Tổng số bản ghi
    private int totalPages;        // Tổng số trang
    private boolean first;         // Có phải trang đầu không
    private boolean last;          // Có phải trang cuối không
    private boolean hasNext;       // Có trang tiếp theo không
    private boolean hasPrevious;   // Có trang trước không

    public PageResponse() {}

    /** Tạo từ Spring Page<T> */
    public static <T> PageResponse<T> of(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.content       = page.getContent();
        response.page          = page.getNumber();
        response.size          = page.getSize();
        response.totalElements = page.getTotalElements();
        response.totalPages    = page.getTotalPages();
        response.first         = page.isFirst();
        response.last          = page.isLast();
        response.hasNext       = page.hasNext();
        response.hasPrevious   = page.hasPrevious();
        return response;
    }

    public List<T> getContent()         { return content; }
    public int getPage()                { return page; }
    public int getSize()                { return size; }
    public long getTotalElements()      { return totalElements; }
    public int getTotalPages()          { return totalPages; }
    public boolean isFirst()            { return first; }
    public boolean isLast()             { return last; }
    public boolean isHasNext()          { return hasNext; }
    public boolean isHasPrevious()      { return hasPrevious; }
}
