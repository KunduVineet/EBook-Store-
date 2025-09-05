package com.ebook.ebookstore.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateDownloadDTO {
    // Getters and Setters
    @NotBlank(message = "Book ID is required")
    private Long ebookId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String userName;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be exactly 10 digits")
    private String contactNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    public CreateDownloadDTO() {}

    public CreateDownloadDTO(Long ebookId, String userName, String contactNumber, String email) {
        this.ebookId = ebookId;
        this.userName = userName;
        this.contactNumber = contactNumber;
        this.email = email;
    }

}
