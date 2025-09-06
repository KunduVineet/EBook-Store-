package com.ebook.ebookstore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO {
    private Integer id;
    private String name;
    private String email;
    private String password;  // Optional: Only for creation or update, never expose in responses
}
