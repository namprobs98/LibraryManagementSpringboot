package com.librarymanagement.repository;

import com.librarymanagement.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    // ── Tìm kiếm fulltext ────────────────────────────────────────────────────

    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title)  LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.genre)  LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> search(@Param("query") String query, Pageable pageable);

    // ── Tìm kiếm + lọc kết hợp (tất cả filter đều optional qua JPQL) ─────────

    /**
     * Query tổng hợp: search + genre filter (optional) + availability filter (optional).
     *
     * Cách dùng:
     *  - genre = null  → bỏ qua điều kiện genre
     *  - availableOnly = true  → copies - borrowed > 0
     *  - availableOnly = false → copies - borrowed = 0  (đã mượn hết)
     *  - availableOnly = null  → không lọc availability
     */
    @Query("SELECT b FROM Book b WHERE " +
           // fulltext search
           "(LOWER(b.title)  LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(b.genre)  LIKE LOWER(CONCAT('%', :query, '%'))) " +
           // genre filter — bỏ qua khi :genre IS NULL
           "AND (:genre IS NULL OR LOWER(b.genre) = LOWER(:genre)) " +
           // availability filter — bỏ qua khi :availableOnly IS NULL
           "AND (:availableOnly IS NULL OR " +
           "     (:availableOnly = TRUE  AND (b.copies - b.borrowed) > 0) OR " +
           "     (:availableOnly = FALSE AND (b.copies - b.borrowed) <= 0))")
    Page<Book> searchWithFilters(@Param("query")         String  query,
                                 @Param("genre")         String  genre,
                                 @Param("availableOnly") Boolean availableOnly,
                                 Pageable pageable);

    // ── Distinct genres trong kết quả search ─────────────────────────────────

    @Query("SELECT DISTINCT b.genre FROM Book b WHERE " +
           "(LOWER(b.title)  LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(b.genre)  LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND b.genre IS NOT NULL " +
           "ORDER BY b.genre")
    List<String> findDistinctGenresByQuery(@Param("query") String query);

    // ── Filter đơn lẻ ─────────────────────────────────────────────────────────

    @Query("SELECT b FROM Book b WHERE LOWER(b.genre) = LOWER(:genre)")
    Page<Book> findByGenre(@Param("genre") String genre, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE LOWER(b.author) = LOWER(:author)")
    Page<Book> findByAuthor(@Param("author") String author, Pageable pageable);
}
