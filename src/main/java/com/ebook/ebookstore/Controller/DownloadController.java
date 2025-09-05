package com.ebook.ebookstore.Controller;

import com.ebook.ebookstore.DTO.CreateDownloadDTO;
import com.ebook.ebookstore.DTO.DownloadDTO;
import com.ebook.ebookstore.Services.DownloadServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/downloads")
@CrossOrigin(origins = "*")
public class DownloadController {

    private final DownloadServices downloadServices;

    @Autowired
    public DownloadController(DownloadServices downloadServices) {
        this.downloadServices = downloadServices;
    }

    @PostMapping("/capture")
    public ResponseEntity<DownloadDTO> captureDownload(@Valid @RequestBody CreateDownloadDTO createDownloadDTO) {
        try {
            DownloadDTO download = downloadServices.captureDownload(createDownloadDTO);
            final ResponseEntity<DownloadDTO> body;
            body = status(HttpStatus.CREATED).body(download);
            return body;
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/file/{downloadId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long downloadId) {
        try {
            Resource file = downloadServices.getDownloadFile(downloadId);
            String filename = downloadServices.getFilename(downloadId);

            return ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(file);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/secure/{bookCode}")
    public ResponseEntity<?> getSecureDownloadInfo(@PathVariable String bookCode) {
        try {
            var downloadInfo = downloadServices.getDownloadInfo(bookCode);
            return ResponseEntity.ok(downloadInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        }
    }

    @GetMapping("/leads")
    public ResponseEntity<List<DownloadDTO>> getAllDownloads() {
        List<DownloadDTO> downloads = downloadServices.getAllDownloads();
        return ok(downloads);
    }

    @GetMapping("/leads/book/{bookId}")
    public ResponseEntity<List<DownloadDTO>> getDownloadsByBook(@PathVariable Long bookId) {
        List<DownloadDTO> downloads = downloadServices.getDownloadsByBook(bookId);
        return ResponseEntity.ok(downloads);
    }

    @GetMapping("/leads/email/{email}")
    public ResponseEntity<List<DownloadDTO>> getDownloadsByEmail(@PathVariable String email) {
        List<DownloadDTO> downloads = downloadServices.getDownloadsByEmail(email);
        return ResponseEntity.ok(downloads);
    }

    @GetMapping("/leads/export/csv")
    public ResponseEntity<Resource> exportLeadsCSV(@RequestParam(required = false) Long bookId,
                                                   @RequestParam(required = false) String startDate,
                                                   @RequestParam(required = false) String endDate) {
        try {
            Resource csvFile = downloadServices.exportLeadsCSV(bookId, startDate, endDate);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"leads_export.csv\"")
                    .body(csvFile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getDownloadStats() {
        try {
            var stats = downloadServices.getDownloadStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching stats: " + e.getMessage());
        }
    }
}
