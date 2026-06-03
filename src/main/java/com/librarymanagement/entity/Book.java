package com.librarymanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @Column(name = "id", length = 255)
    private String id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "author", length = 255)
    private String author;

    @Column(name = "genre", length = 255)
    private String genre;

    @Column(name = "copies")
    private int copies;

    @Column(name = "borrowed")
    private int borrowed;

    public Book() {
    }

    public Book(String id, String title, String author, String genre, int copies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.copies = copies;
        this.borrowed = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getCopies() { return copies; }
    public void setCopies(int copies) { this.copies = copies; }
    public int getBorrowed() { return borrowed; }
    public void setBorrowed(int borrowed) { this.borrowed = borrowed; }

    @Override
    public String toString() {
        return String.format("Book[id=%s, title=%s, author=%s, genre=%s, copies=%d, borrowed=%d]",
                id, title, author, genre, copies, borrowed);
    }
}
