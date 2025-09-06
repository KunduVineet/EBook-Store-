package com.ebook.ebookstore.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookDTO {
    private Long id;

    @NotBlank(message = "Book name is required")
    @Size(max = 100, message = "Book name must not exceed 100 characters")
    private String name;

    private String code;
    private String category;
    private String subcategory;

    @NotBlank(message = "Author is required")
    private String author;

    private String description;
    private String downloadUrl;

    // Constructors
    public BookDTO() {
    }

    public BookDTO(Long id, String name, String code, String category, String subcategory,
                   String author, String description, String downloadUrl) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.category = category;
        this.subcategory = subcategory;
        this.author = author;
        this.description = description;
        this.downloadUrl = downloadUrl;
    }
}