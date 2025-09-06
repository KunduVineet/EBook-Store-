package com.ebook.ebookstore.Repository;

import com.ebook.ebookstore.Model.Admin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(@NotBlank(message = "Email is required") @Email(message = "Email must be valid") @Size(max = 100, message = "Email must not exceed 100 characters") String email, Long id);

    Admin findByEmail(String email);

    Admin findByName(String name);
}
