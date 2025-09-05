package com.ebook.ebookstore.Repository;

import com.ebook.ebookstore.Model.Books;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Books, Long> {

    List<Books> findByAuthorContainingIgnoreCase(String author);

    List<Books> findByNameContainingIgnoreCase(String name);

    List<Books> findByCategoryIgnoreCase(String category);

    List<Books> findBySubcategoryIgnoreCase(String subcategory);

    Optional<Books> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT b FROM Books b WHERE " +
            "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:category IS NULL OR LOWER(b.category) = LOWER(:category)) AND " +
            "(:subcategory IS NULL OR LOWER(b.subcategory) = LOWER(:subcategory))")
    List<Books> findBooksWithFilters(@Param("author") String author,
                                     @Param("category") String category,
                                     @Param("subcategory") String subcategory);
}