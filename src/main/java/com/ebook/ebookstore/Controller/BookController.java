package com.ebook.ebookstore.Controller;

import com.ebook.ebookstore.DTO.BookDTO;
import com.ebook.ebookstore.DTO.CreateBookDTO;
import com.ebook.ebookstore.Services.BookServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookServices bookServices;

    @Autowired
    public BookController(BookServices bookServices) {
        this.bookServices = bookServices;
    }

    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> books = bookServices.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        try {
            BookDTO book = bookServices.getBookById(id);
            return ResponseEntity.ok(book);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<BookDTO> getBookByCode(@PathVariable String code) {
        try {
            BookDTO book = bookServices.getBookByCode(code);
            return ResponseEntity.ok(book);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<List<BookDTO>> getBooksByAuthor(@PathVariable String author) {
        List<BookDTO> books = bookServices.getBooksByAuthor(author);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<BookDTO>> getBooksByName(@PathVariable String name) {
        List<BookDTO> books = bookServices.getBooksByName(name);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<BookDTO>> getBooksByCategory(@PathVariable String category) {
        List<BookDTO> books = bookServices.getBooksByCategory(category);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/subcategory/{subcategory}")
    public ResponseEntity<List<BookDTO>> getBooksBySubcategory(@PathVariable String subcategory) {
        List<BookDTO> books = bookServices.getBooksBySubcategory(subcategory);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory) {
        List<BookDTO> books = bookServices.searchBooks(author, category, subcategory);
        return ok(books);
    }

    @PostMapping("/createBook")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody CreateBookDTO createBookDTO) {
        try {
            BookDTO createdBook = bookServices.createBook(createBookDTO);
            return status(HttpStatus.CREATED).body(createdBook);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id,
                                              @Valid @RequestBody CreateBookDTO createBookDTO) {
        try {
            BookDTO updatedBook = bookServices.updateBook(id, createBookDTO);
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        try {
            bookServices.deleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> existsBookById(@PathVariable Long id) {
        boolean exists = bookServices.existsBookById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/code/{code}")
    public ResponseEntity<Boolean> existsBookByCode(@PathVariable String code) {
        boolean exists = bookServices.existsBookByCode(code);
        return ResponseEntity.ok(exists);
    }
}
