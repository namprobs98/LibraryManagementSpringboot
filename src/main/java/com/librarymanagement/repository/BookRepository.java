package com.librarymanagement.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.librarymanagement.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, String>, JpaSpecificationExecutor<Book> {

    // Repository intentionally left minimal — search is implemented via Specifications.

    // Keep simple helpers if needed (optional):
    default List<String> findDistinctGenresBySpecification(org.springframework.data.jpa.domain.Specification<Book> spec) {
        return findAll(spec, Sort.by("genre")).stream()
                .map(Book::getGenre)
                .filter(g -> g != null && !g.isBlank())
                .map(String::strip)
                .distinct()
                .sorted()
                .toList();
    }
}
