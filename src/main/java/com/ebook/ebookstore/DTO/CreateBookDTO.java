package com.ebook.ebookstore.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CreateBookDTO {
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
    public CreateBookDTO() {
    }
}