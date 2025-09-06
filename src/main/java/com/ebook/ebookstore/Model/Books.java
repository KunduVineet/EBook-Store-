package com.ebook.ebookstore.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@AllArgsConstructor
@Data
@Getter
@Setter
public class Books {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Book Name is required")
    @Size(max = 100, message = "Book Name must not exceed 100 characters")
    @Column(name = "book_name", nullable = false)
    private String name;

    @Column(name = "book_code", unique = true)
    private String code;

    @Column(name = "category")
    private String category;

    @Column(name = "subcategory")
    private String subcategory;

    @NotBlank(message = "Author is required")
    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "download_url")
    private String downloadUrl;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Download> downloads = new ArrayList<>();

    public Books() {}

    public Books(String name, String code, String category, String subcategory,
                 String author, String description, String downloadUrl) {
        this.name = name;
        this.code = code;
        this.category = category;
        this.subcategory = subcategory;
        this.author = author;
        this.description = description;
        this.downloadUrl = downloadUrl;
    }

}