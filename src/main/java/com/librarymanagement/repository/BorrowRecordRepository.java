package com.librarymanagement.repository;

import com.librarymanagement.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, String> {

    /**
     * Lấy tất cả borrow records CÓ PHÂN TRANG.
     * bookTitle và memberName sẽ được set ở tầng Service sau khi fetch batch.
     */
    Page<BorrowRecord> findAll(Pageable pageable);

    /**
     * Tìm borrow records chưa được trả (returnDate null hoặc rỗng)
     * của một member và book cụ thể.
     */
    @Query("SELECT br FROM BorrowRecord br " +
           "WHERE br.memberId = :memberId AND br.bookId = :bookId " +
           "AND (br.returnDate IS NULL OR br.returnDate = '')")
    List<BorrowRecord> findActiveRecord(@Param("memberId") String memberId,
                                        @Param("bookId") String bookId);

    /**
     * Tìm tất cả borrow records của một member — CÓ PHÂN TRANG.
     */
    Page<BorrowRecord> findByMemberId(String memberId, Pageable pageable);

    /**
     * Tìm tất cả borrow records của một book — CÓ PHÂN TRANG.
     */
    Page<BorrowRecord> findByBookId(String bookId, Pageable pageable);

    /**
     * Lấy danh sách ID của books xuất hiện trong một trang records.
     * Dùng để batch-load book titles tránh N+1 query.
     */
    @Query("SELECT DISTINCT br.bookId FROM BorrowRecord br WHERE br.id IN :ids")
    List<String> findDistinctBookIdsByIds(@Param("ids") List<String> ids);

    /**
     * Lấy danh sách ID của members xuất hiện trong một trang records.
     * Dùng để batch-load member names tránh N+1 query.
     */
    @Query("SELECT DISTINCT br.memberId FROM BorrowRecord br WHERE br.id IN :ids")
    List<String> findDistinctMemberIdsByIds(@Param("ids") List<String> ids);
}
