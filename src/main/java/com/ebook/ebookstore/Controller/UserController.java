package com.ebook.ebookstore.Controller;

import com.ebook.ebookstore.DTO.UserDTO;
import com.ebook.ebookstore.DTO.UserLoginDTO;
import com.ebook.ebookstore.Model.User;
import com.ebook.ebookstore.ServicesImpl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userService;

    @Autowired
    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO, Errors errors) {
        // Check for validation errors
        if (errors.hasErrors()) {
            List<String> errorMessages = errors.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }
        try {
            User createdUser = userService.createUser(userDTO);
            // Don't return the User entity with password, create a DTO instead
            UserDTO responseDTO = new UserDTO(
                    Math.toIntExact(createdUser.getId()),
                    createdUser.getName(),
                    createdUser.getEmail(),
                    null // Exclude password
            );
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginDTO userLoginDTO,
                                       Errors errors, HttpSession session) {
        // Check for validation errors
        if (errors.hasErrors()) {
            List<String> errorMessages = errors.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }

        // Authenticate user
        if (userService.authenticate(userLoginDTO.getEmail(), userLoginDTO.getPassword())) {
            // Retrieve user details
            User user = userService.getUserByEmail(userLoginDTO.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("User not found after authentication");
            }

            // Store user ID in session
            session.setAttribute("userId", user.getId());

            // Map User entity to UserDTO, excluding password
            UserDTO userDTO = new UserDTO(
                    Math.toIntExact(user.getId()),
                    user.getName(),
                    user.getEmail(),
                    null // Exclude password
            );

            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @GetMapping("/home")
    public ResponseEntity<?> home(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in");
        }

        Optional<User> userOptional = userService.getUserById(userId);
        if (userOptional.isEmpty()) { // Fixed: handle Optional properly
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        return ResponseEntity.ok("Welcome, " + userOptional.get().getName());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok("No active session to logout");
        }
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Integer userId,
                                        @Valid @RequestBody UserDTO userDTO,
                                        Errors errors, HttpSession session) {
        // Check for validation errors
        if (errors.hasErrors()) {
            List<String> errorMessages = errors.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }

        // Authorization check
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized to update this user");
        }

        try {
            User updatedUser = userService.updateUser(userId, userDTO);
            // Return DTO instead of User entity
            UserDTO responseDTO = new UserDTO(
                    Math.toIntExact(updatedUser.getId()),
                    updatedUser.getName(),
                    updatedUser.getEmail(),
                    null // Exclude password
            );
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId, HttpSession session) {
        // Authorization check
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized to delete this user");
        }

        try {
            userService.deleteUser(userId);
            // Invalidate session after deleting user
            session.invalidate();
            return ResponseEntity.ok("User deleted successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user: " + e.getMessage());
        }
    }
}