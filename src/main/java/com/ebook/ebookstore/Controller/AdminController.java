package com.ebook.ebookstore.Controller;

import com.ebook.ebookstore.DTO.AdminDTO;
import com.ebook.ebookstore.DTO.BookDTO;
import com.ebook.ebookstore.DTO.CreateBookDTO;
import com.ebook.ebookstore.Model.Admin;
import com.ebook.ebookstore.Services.AdminServices;
import com.ebook.ebookstore.Services.BookServices;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminServices adminServices;

    private final BookServices bookServices;

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);


    @Autowired
    public AdminController(AdminServices adminServices, BookServices bookServices) {
        this.adminServices = adminServices;
        this.bookServices = bookServices;
    }

    // Create Admin
    @PostMapping
    public ResponseEntity<AdminDTO> createAdmin(@Valid @RequestBody AdminDTO adminDTO) {
        Admin admin = adminServices.createAdmin(adminDTO);
        AdminDTO responseDTO = new AdminDTO(Math.toIntExact(admin.getId()), admin.getName(), admin.getEmail(), null);
        return ok(responseDTO);
    }

    // Get All Admins
    @GetMapping
    public ResponseEntity<List<AdminDTO>> getAllAdmins() {
        List<AdminDTO> admins = adminServices.getAllAdmins().stream()
                .map(a -> new AdminDTO(Math.toIntExact(a.getId()), a.getName(), a.getEmail(), null))
                .collect(Collectors.toList());
        return ok(admins);
    }

    // Get Admin by ID
    @GetMapping("/{id}")
    public ResponseEntity<AdminDTO> getAdminById(@PathVariable Integer id) {
        Admin admin = adminServices.getAdminById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with id " + id));
        AdminDTO responseDTO = new AdminDTO(Math.toIntExact(admin.getId()), admin.getName(), admin.getEmail(), null);
        return ok(responseDTO);
    }

    // Update Admin
    @PutMapping("/{id}")
    public ResponseEntity<AdminDTO> updateAdmin(@PathVariable Integer id, @Valid @RequestBody AdminDTO adminDTO) {
        Admin updatedAdmin = adminServices.updateAdmin(id, adminDTO);
        AdminDTO responseDTO = new AdminDTO(Math.toIntExact(updatedAdmin.getId()), updatedAdmin.getName(), updatedAdmin.getEmail(), null);
        return ok(responseDTO);
    }

    // Delete Admin
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Integer id) {
        adminServices.deleteAdmin(id);
        return ok("Admin deleted successfully with id " + id);
    }

    // Authenticate Admin (Login)
    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AdminDTO adminDTO) {
        boolean authenticated = adminServices.authenticate(adminDTO.getEmail(), adminDTO.getPassword());
        if (authenticated) {
            return ok("Authentication successful");
        } else {
            return status(401).body("Invalid email or password");
        }
    }

    @PostMapping(value = "/createBook", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody CreateBookDTO createBookDTO) {
        logger.info("Received createBook request: {}", createBookDTO);
        try {
            BookDTO createdBook = bookServices.createBook(createBookDTO);
            logger.info("Book created successfully: {}", createdBook);
            return status(HttpStatus.CREATED).body(createdBook);
        } catch (RuntimeException e) {
            logger.error("Error creating book", e);
            return badRequest().build();
        }
    }


}
