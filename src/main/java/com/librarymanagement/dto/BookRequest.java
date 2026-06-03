package com.librarymanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookRequest {

    @NotBlank(message = "Book ID is required")
    private String id;

    @NotBlank(message = "Title is required")
    private String title;

    private String author;
    private String genre;

    @NotNull(message = "Copies is required")
    @Min(value = 0, message = "Copies must be >= 0")
    private Integer copies;

    public BookRequest() {}

    public String getId()             { return id; }
    public void setId(String id)      { this.id = id; }
    public String getTitle()          { return title; }
    public void setTitle(String t)    { this.title = t; }
    public String getAuthor()         { return author; }
    public void setAuthor(String a)   { this.author = a; }
    public String getGenre()          { return genre; }
    public void setGenre(String g)    { this.genre = g; }
    public Integer getCopies()        { return copies; }
    public void setCopies(Integer c)  { this.copies = c; }
}
