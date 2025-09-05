// Download Repository
package com.ebook.ebookstore.Repository;

import com.ebook.ebookstore.Model.Download;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DownloadRepository extends JpaRepository<Download, Long> {

    List<Download> findByEbookId(Long ebookId);

    List<Download> findByEmail(String email);

    List<Download> findByContactNumber(String contactNumber);

    List<Download> findByUserName(String userName);

    @Query("SELECT COUNT(DISTINCT d.email) FROM Download d")
    Long countDistinctByEmail();

    Long countByDownloadTimeAfter(LocalDateTime dateTime);

    @Query("SELECT d FROM Download d WHERE d.downloadTime BETWEEN :startDate AND :endDate")
    List<Download> findByDownloadTimeBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT d FROM Download d WHERE d.ebookId = :ebookId AND d.downloadTime BETWEEN :startDate AND :endDate")
    List<Download> findByEbookIdAndDownloadTimeBetween(
            @Param("ebookId") Long ebookId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(d) FROM Download d WHERE d.ebookId = :ebookId")
    Long countByEbookId(@Param("ebookId") Long ebookId);

    @Query("SELECT d FROM Download d ORDER BY d.downloadTime DESC")
    List<Download> findAllOrderByDownloadTimeDesc();
}