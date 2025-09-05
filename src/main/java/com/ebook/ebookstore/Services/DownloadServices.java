package com.ebook.ebookstore.Services;

import com.ebook.ebookstore.DTO.DownloadDTO;
import com.ebook.ebookstore.DTO.CreateDownloadDTO;
import com.ebook.ebookstore.DTO.DownloadInfoDTO;
import com.ebook.ebookstore.DTO.DownloadStatsDTO;
import org.springframework.core.io.Resource;

import java.util.List;

public interface DownloadServices {
    DownloadDTO captureDownload(CreateDownloadDTO createDownloadDTO);
    Resource getDownloadFile(Long downloadId);
    String getFilename(Long downloadId);
    DownloadInfoDTO getDownloadInfo(String bookCode);
    List<DownloadDTO> getAllDownloads();
    List<DownloadDTO> getDownloadsByBook(Long bookId);
    List<DownloadDTO> getDownloadsByEmail(String email);
    Resource exportLeadsCSV(Long bookId, String startDate, String endDate);
    DownloadStatsDTO getDownloadStats();
}