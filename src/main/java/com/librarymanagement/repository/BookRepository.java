package com.librarymanagement.repository;

import com.librarymanagement.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    /**
     * Tìm kiếm sách theo title, author hoặc genre (case-insensitive) — CÓ PHÂN TRANG.
     * Dùng cho REST API và Console (trang đầu).
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title)  LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.genre)  LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> search(@Param("query") String query, Pageable pageable);

    /**
     * Tìm theo genre chính xác (case-insensitive) — CÓ PHÂN TRANG.
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.genre) = LOWER(:genre)")
    Page<Book> findByGenre(@Param("genre") String genre, Pageable pageable);

    /**
     * Tìm theo author chính xác (case-insensitive) — CÓ PHÂN TRANG.
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.author) = LOWER(:author)")
    Page<Book> findByAuthor(@Param("author") String author, Pageable pageable);
}
