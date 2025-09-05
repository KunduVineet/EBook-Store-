package com.ebook.ebookstore.ServicesImpl;

import com.ebook.ebookstore.DTO.*;
import com.ebook.ebookstore.Model.Download;
import com.ebook.ebookstore.Model.Books;
import com.ebook.ebookstore.Repository.DownloadRepository;
import com.ebook.ebookstore.Repository.BookRepository;
import com.ebook.ebookstore.Services.DownloadServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DownloadServiceImpl implements DownloadServices {

    private final DownloadRepository downloadRepository;
    private final BookRepository bookRepository;

    @Autowired
    public DownloadServiceImpl(DownloadRepository downloadRepository, BookRepository bookRepository) {
        this.downloadRepository = downloadRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public DownloadDTO captureDownload(CreateDownloadDTO createDownloadDTO) {
        // Validate book exists
        Optional<Books> bookOptional = bookRepository.findById(createDownloadDTO.getEbookId());
        if (bookOptional.isEmpty()) {
            throw new RuntimeException("Book not found with ID: " + createDownloadDTO.getEbookId());
        }

        Books book = bookOptional.get();

        // Create download record
        Download download = new Download();
        download.setEbookId(createDownloadDTO.getEbookId());
        download.setUserName(createDownloadDTO.getUserName());
        download.setContactNumber(createDownloadDTO.getContactNumber());
        download.setEmail(createDownloadDTO.getEmail());
        download.setDownloadTime(LocalDateTime.now());

        Download savedDownload = downloadRepository.save(download);

        // Convert to DTO
        return new DownloadDTO(
                savedDownload.getId(),
                savedDownload.getEbookId(),
                book.getName(),
                book.getCode(),
                savedDownload.getUserName(),
                savedDownload.getContactNumber(),
                savedDownload.getEmail(),
                savedDownload.getDownloadTime()
        );
    }

    @Override
    public Resource getDownloadFile(Long downloadId) {
        Optional<Download> downloadOptional = downloadRepository.findById(downloadId);
        if (downloadOptional.isEmpty()) {
            throw new RuntimeException("Download not found with ID: " + downloadId);
        }

        Download download = downloadOptional.get();
        Optional<Books> bookOptional = bookRepository.findById(download.getEbookId());

        if (bookOptional.isEmpty()) {
            throw new RuntimeException("Book not found");
        }

        Books book = bookOptional.get();

        try {
            Path filePath = Paths.get(book.getDownloadUrl());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + book.getDownloadUrl());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Could not read file: " + book.getDownloadUrl(), ex);
        }
    }

    @Override
    public String getFilename(Long downloadId) {
        Optional<Download> downloadOptional = downloadRepository.findById(downloadId);
        if (downloadOptional.isEmpty()) {
            throw new RuntimeException("Download not found with ID: " + downloadId);
        }

        Download download = downloadOptional.get();
        Optional<Books> bookOptional = bookRepository.findById(download.getEbookId());

        if (bookOptional.isEmpty()) {
            throw new RuntimeException("Book not found");
        }

        Books book = bookOptional.get();
        return book.getName() + ".pdf"; // Assuming PDF format, adjust as needed
    }

    @Override
    public DownloadInfoDTO getDownloadInfo(String bookCode) {
        Optional<Books> bookOptional = bookRepository.findByCode(bookCode);
        if (bookOptional.isEmpty()) {
            throw new RuntimeException("Book not found with code: " + bookCode);
        }

        Books book = bookOptional.get();
        return new DownloadInfoDTO(
                book.getId(),
                book.getName(),
                book.getCode(),
                book.getAuthor(),
                true // downloadAllowed - you can add logic here
        );
    }

    @Override
    public List<DownloadDTO> getAllDownloads() {
        List<Download> downloads = downloadRepository.findAll();
        return downloads.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<DownloadDTO> getDownloadsByBook(Long bookId) {
        List<Download> downloads = downloadRepository.findByEbookId(bookId);
        return downloads.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<DownloadDTO> getDownloadsByEmail(String email) {
        List<Download> downloads = downloadRepository.findByEmail(email);
        return downloads.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Resource exportLeadsCSV(Long bookId, String startDate, String endDate) {
        List<Download> downloads;

        if (bookId != null) {
            downloads = downloadRepository.findByEbookId(bookId);
        } else {
            downloads = downloadRepository.findAll();
        }

        // Filter by date if provided
        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            downloads = downloads.stream()
                    .filter(d -> d.getDownloadTime().isAfter(start) && d.getDownloadTime().isBefore(end))
                    .collect(Collectors.toList());
        }

        // Generate CSV
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("ID,Book Name,Book Code,User Name,Email,Contact Number,Download Time");

        // CSV Data
        for (Download download : downloads) {
            Optional<Books> bookOptional = bookRepository.findById(download.getEbookId());
            String bookName = bookOptional.isPresent() ? bookOptional.get().getName() : "Unknown";
            String bookCode = bookOptional.isPresent() ? bookOptional.get().getCode() : "Unknown";

            writer.printf("%d,%s,%s,%s,%s,%s,%s%n",
                    download.getId(),
                    escapeCsvField(bookName),
                    escapeCsvField(bookCode),
                    escapeCsvField(download.getUserName()),
                    escapeCsvField(download.getEmail()),
                    escapeCsvField(download.getContactNumber()),
                    download.getDownloadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        }

        writer.flush();
        writer.close();

        byte[] csvBytes = outputStream.toByteArray();
        return new ByteArrayResource(csvBytes);

    }

    @Override
    public DownloadStatsDTO getDownloadStats() {
        Long totalDownloads = downloadRepository.count();
        Long totalBooks = bookRepository.count();
        Long uniqueUsers = downloadRepository.countDistinctByEmail();

        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekStart = today.minusDays(7);
        LocalDateTime monthStart = today.minusDays(30);

        Long downloadsToday = downloadRepository.countByDownloadTimeAfter(today);
        Long downloadsThisWeek = downloadRepository.countByDownloadTimeAfter(weekStart);
        Long downloadsThisMonth = downloadRepository.countByDownloadTimeAfter(monthStart);

        return new DownloadStatsDTO(
                totalDownloads,
                totalBooks,
                uniqueUsers,
                downloadsToday,
                downloadsThisWeek,
                downloadsThisMonth
        );
    }

    private DownloadDTO convertToDTO(Download download) {
        Optional<Books> bookOptional = bookRepository.findById(download.getEbookId());
        String bookName = bookOptional.isPresent() ? bookOptional.get().getName() : "Unknown";
        String bookCode = bookOptional.isPresent() ? bookOptional.get().getCode() : "Unknown";

        return new DownloadDTO(
                download.getId(),
                download.getEbookId(),
                bookName,
                bookCode,
                download.getUserName(),
                download.getContactNumber(),
                download.getEmail(),
                download.getDownloadTime()
        );
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }

        return field;
    }
}