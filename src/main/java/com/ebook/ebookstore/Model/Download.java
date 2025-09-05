package com.ebook.ebookstore.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "downloads")
@Getter
@Setter
@RequiredArgsConstructor
public class Download {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ebook_id", nullable = false)
    private Long ebookId;

    @NotBlank(message = "User name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be exactly 10 digits")
    @Column(name = "contact_number", nullable = false, length = 15)
    private String contactNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "download_time", nullable = false)
    private LocalDateTime downloadTime;

    // Foreign key relationship with Book entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebook_id", insertable = false, updatable = false)
    private Books book;

    @Override
    public String toString() {
        return "Download{" +
                "id=" + id +
                ", ebookId=" + ebookId +
                ", userName='" + userName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", email='" + email + '\'' +
                ", downloadTime=" + downloadTime +
                '}';
    }

    // equals and hashCode methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Download download = (Download) obj;
        return id != null && id.equals(download.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
