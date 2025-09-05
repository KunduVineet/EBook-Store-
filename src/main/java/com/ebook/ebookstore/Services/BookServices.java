package com.ebook.ebookstore.Services;

import com.ebook.ebookstore.DTO.BookDTO;
import com.ebook.ebookstore.DTO.CreateBookDTO;

import java.util.List;

public interface BookServices {
    List<BookDTO> getAllBooks();
    BookDTO getBookById(Long id);
    List<BookDTO> getBooksByAuthor(String author);
    List<BookDTO> getBooksByName(String name);
    List<BookDTO> getBooksByCategory(String category);
    List<BookDTO> getBooksBySubcategory(String subcategory);
    BookDTO getBookByCode(String code);
    BookDTO createBook(CreateBookDTO createBookDTO);
    BookDTO updateBook(Long id, CreateBookDTO createBookDTO);
    void deleteBook(Long id);
    boolean existsBookById(Long id);
    boolean existsBookByCode(String code);
    List<BookDTO> searchBooks(String author, String category, String subcategory);
}