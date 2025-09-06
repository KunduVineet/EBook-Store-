package com.ebook.ebookstore.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private Integer id;
    private String name;
    private String email;
    private String password;

    // Constructor that matches what you're using in the controller
    public UserDTO(Integer id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }
}