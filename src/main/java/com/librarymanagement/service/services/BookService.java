package com.librarymanagement.service.services;

import com.librarymanagement.entity.Book;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface BookService {

    Page<Book> getAllBooks(int page, int size, String sort, boolean asc);

    Page<Book> getAllBooks(int page, int size);

    Page<Book> getFirstPage();

    List<Book> findAllForExport();

    boolean addBook(Book book);

    Optional<Book> getBookById(String id);

    boolean updateBook(String id, String title, String author, String genre, int copies);

    boolean deleteBook(String id);

    Page<Book> searchBooks(String query, int page, int size);

    List<Book> searchBooks(String query);

    List<Book> searchWithFilters(String query, String genre, Boolean availableOnly);

    List<String> getGenresFromSearch(String query);
}
