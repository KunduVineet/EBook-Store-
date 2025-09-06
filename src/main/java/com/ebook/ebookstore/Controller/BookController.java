package com.ebook.ebookstore.Controller;

import com.ebook.ebookstore.DTO.BookDTO;
import com.ebook.ebookstore.DTO.CreateBookDTO;
import com.ebook.ebookstore.Services.BookServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookServices bookServices;

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);


    @Autowired
    public BookController(BookServices bookServices) {
        this.bookServices = bookServices;
    }

    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> books = bookServices.getAllBooks();
        return ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        try {
            BookDTO book = bookServices.getBookById(id);
            return ok(book);
        } catch (RuntimeException e) {
            return notFound().build();
        }
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<BookDTO> getBookByCode(@PathVariable String code) {
        try {
            BookDTO book = bookServices.getBookByCode(code);
            return ok(book);
        } catch (RuntimeException e) {
            return notFound().build();
        }
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<List<BookDTO>> getBooksByAuthor(@PathVariable String author) {
        List<BookDTO> books = bookServices.getBooksByAuthor(author);
        return ok(books);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<BookDTO>> getBooksByName(@PathVariable String name) {
        List<BookDTO> books = bookServices.getBooksByName(name);
        return ok(books);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<BookDTO>> getBooksByCategory(@PathVariable String category) {
        List<BookDTO> books = bookServices.getBooksByCategory(category);
        return ok(books);
    }

    @GetMapping("/subcategory/{subcategory}")
    public ResponseEntity<List<BookDTO>> getBooksBySubcategory(@PathVariable String subcategory) {
        List<BookDTO> books = bookServices.getBooksBySubcategory(subcategory);
        return ok(books);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory) {
        List<BookDTO> books = bookServices.searchBooks(author, category, subcategory);
        return ok(books);
    }

    @PostMapping(value = "/createBook", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody CreateBookDTO createBookDTO) {
        logger.info("Received createBook request: {}", createBookDTO);
        try {
            BookDTO createdBook = bookServices.createBook(createBookDTO);
            logger.info("Book created successfully: {}", createdBook);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
        } catch (RuntimeException e) {
            logger.error("Error creating book", e);
            return ResponseEntity.badRequest().build();
        }
    }



    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id,
                                              @Valid @RequestBody CreateBookDTO createBookDTO) {
        try {
            BookDTO updatedBook = bookServices.updateBook(id, createBookDTO);
            return ok(updatedBook);
        } catch (RuntimeException e) {
            return notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        try {
            bookServices.deleteBook(id);
            return noContent().build();
        } catch (RuntimeException e) {
            return notFound().build();
        }
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> existsBookById(@PathVariable Long id) {
        boolean exists = bookServices.existsBookById(id);
        return ok(exists);
    }

    @GetMapping("/exists/code/{code}")
    public ResponseEntity<Boolean> existsBookByCode(@PathVariable String code) {
        boolean exists = bookServices.existsBookByCode(code);
        return ok(exists);
    }
}
