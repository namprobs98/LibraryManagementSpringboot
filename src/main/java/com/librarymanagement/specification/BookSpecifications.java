package com.librarymanagement.specification;

import org.springframework.data.jpa.domain.Specification;

import com.librarymanagement.entity.Book;

import jakarta.persistence.criteria.Expression;

public final class BookSpecifications {

    private BookSpecifications() {}

    public static Specification<Book> fullTextContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String q = keyword.trim().toLowerCase();
        return (root, query, cb) -> {
            String pattern = "%" + q + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("author")), pattern),
                    cb.like(cb.lower(root.get("genre")), pattern)
            );
        };
    }

    public static Specification<Book> genreEqualsIgnoreCase(String genre) {
        if (genre == null || genre.isBlank()) return null;
        String g = genre.trim().toLowerCase();
        return (root, query, cb) -> cb.equal(cb.lower(root.get("genre")), g);
    }

    public static Specification<Book> availabilityEquals(Boolean availableOnly) {
        if (availableOnly == null) return null;
        return (root, query, cb) -> {
            Expression<Integer> diff = cb.diff(root.get("copies"), root.get("borrowed"));
            if (availableOnly) {
                return cb.greaterThan(diff, 0);
            } else {
                return cb.lessThanOrEqualTo(diff, 0);
            }
        };
    }
}
