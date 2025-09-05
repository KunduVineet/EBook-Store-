package com.ebook.ebookstore.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private Integer id;  // Changed from long to Integer to match your repository
    private String username;
    private String email;
    private String password;

    // Constructor that matches what you're using in the controller
    public UserDTO(Integer id, String name, String email, String password) {
        this.id = id;
        this.username = name;
        this.email = email;
        this.password = password;
    }
}