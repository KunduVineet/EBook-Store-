package com.ebook.ebookstore.ServicesImpl;

import com.ebook.ebookstore.DTO.BookDTO;
import com.ebook.ebookstore.DTO.CreateBookDTO;
import com.ebook.ebookstore.Model.Books;
import com.ebook.ebookstore.Repository.BookRepository;
import com.ebook.ebookstore.Services.BookServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BooksServicesImpl implements BookServices {

    private final BookRepository bookRepository;

    @Autowired
    public BooksServicesImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookDTO getBookById(Long id) {
        Books book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        return convertToDTO(book);
    }

    @Override
    public List<BookDTO> getBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> getBooksByName(String name) {
        return bookRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> getBooksByCategory(String category) {
        return bookRepository.findByCategoryIgnoreCase(category)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> getBooksBySubcategory(String subcategory) {
        return bookRepository.findBySubcategoryIgnoreCase(subcategory)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookDTO getBookByCode(String code) {
        Books book = bookRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Book not found with code: " + code));
        return convertToDTO(book);
    }

    @Override
    public BookDTO createBook(CreateBookDTO createBookDTO) {
        if (bookRepository.existsByCode(createBookDTO.getCode())) {
            throw new RuntimeException("Book with code already exists: " + createBookDTO.getCode());
        }

        Books book = convertToEntity(createBookDTO);
        Books savedBook = bookRepository.save(book);
        return convertToDTO(savedBook);
    }

    @Override
    public BookDTO updateBook(Long id, CreateBookDTO createBookDTO) {
        Books existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        // Check if code is being changed and if new code already exists
        if (!existingBook.getCode().equals(createBookDTO.getCode()) &&
                bookRepository.existsByCode(createBookDTO.getCode())) {
            throw new RuntimeException("Book with code already exists: " + createBookDTO.getCode());
        }

        updateBookFromDTO(existingBook, createBookDTO);
        Books savedBook = bookRepository.save(existingBook);
        return convertToDTO(savedBook);
    }

    @Override
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    @Override
    public boolean existsBookById(Long id) {
        return bookRepository.existsById(id);
    }

    @Override
    public boolean existsBookByCode(String code) {
        return bookRepository.existsByCode(code);
    }

    @Override
    public List<BookDTO> searchBooks(String author, String category, String subcategory) {
        return bookRepository.findBooksWithFilters(author, category, subcategory)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods for conversion
    private BookDTO convertToDTO(Books book) {
        return new BookDTO(
                book.getId(),
                book.getName(),
                book.getCode(),
                book.getCategory(),
                book.getSubcategory(),
                book.getAuthor(),
                book.getDescription(),
                book.getDownloadUrl()
        );
    }

    private Books convertToEntity(CreateBookDTO createBookDTO) {
        return new Books(
                createBookDTO.getName(),
                createBookDTO.getCode(),
                createBookDTO.getCategory(),
                createBookDTO.getSubcategory(),
                createBookDTO.getAuthor(),
                createBookDTO.getDescription(),
                createBookDTO.getDownloadUrl()
        );
    }

    private void updateBookFromDTO(Books book, CreateBookDTO createBookDTO) {
        book.setName(createBookDTO.getName());
        book.setCode(createBookDTO.getCode());
        book.setCategory(createBookDTO.getCategory());
        book.setSubcategory(createBookDTO.getSubcategory());
        book.setAuthor(createBookDTO.getAuthor());
        book.setDescription(createBookDTO.getDescription());
        book.setDownloadUrl(createBookDTO.getDownloadUrl());
    }
}
