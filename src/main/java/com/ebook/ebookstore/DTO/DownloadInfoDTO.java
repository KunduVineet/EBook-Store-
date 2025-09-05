package com.ebook.ebookstore.DTO;

import lombok.Getter;
import lombok.Setter;

// DownloadInfoDTO for secure download info
@Setter
@Getter
public class DownloadInfoDTO {
    // Getters and Setters
    private Long bookId;
    private String bookName;
    private String bookCode;
    private String author;
    private boolean downloadAllowed;

    public DownloadInfoDTO() {}

    public DownloadInfoDTO(Long bookId, String bookName, String bookCode, String author, boolean downloadAllowed) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.bookCode = bookCode;
        this.author = author;
        this.downloadAllowed = downloadAllowed;
    }

}
