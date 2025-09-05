package com.ebook.ebookstore.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// DownloadDTO for response
@Setter
@Getter
public class DownloadDTO {
    // Getters and Setters
    private Long id;
    private Long ebookId;
    private String ebookName;
    private String ebookCode;
    private String userName;
    private String contactNumber;
    private String email;
    private LocalDateTime downloadTime;

    public DownloadDTO() {}

    public DownloadDTO(Long id, Long ebookId, String ebookName, String ebookCode,
                       String userName, String contactNumber, String email, LocalDateTime downloadTime) {
        this.id = id;
        this.ebookId = ebookId;
        this.ebookName = ebookName;
        this.ebookCode = ebookCode;
        this.userName = userName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.downloadTime = downloadTime;
    }

}
