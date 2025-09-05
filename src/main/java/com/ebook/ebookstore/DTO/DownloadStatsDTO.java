package com.ebook.ebookstore.DTO;

import lombok.Getter;
import lombok.Setter;

// DownloadStatsDTO for dashboard stats
@Setter
@Getter
public class DownloadStatsDTO {
    // Getters and Setters
    private Long totalDownloads;
    private Long totalBooks;
    private Long uniqueUsers;
    private Long downloadsToday;
    private Long downloadsThisWeek;
    private Long downloadsThisMonth;

    public DownloadStatsDTO() {}

    public DownloadStatsDTO(Long totalDownloads, Long totalBooks, Long uniqueUsers,
                            Long downloadsToday, Long downloadsThisWeek, Long downloadsThisMonth) {
        this.totalDownloads = totalDownloads;
        this.totalBooks = totalBooks;
        this.uniqueUsers = uniqueUsers;
        this.downloadsToday = downloadsToday;
        this.downloadsThisWeek = downloadsThisWeek;
        this.downloadsThisMonth = downloadsThisMonth;
    }

}