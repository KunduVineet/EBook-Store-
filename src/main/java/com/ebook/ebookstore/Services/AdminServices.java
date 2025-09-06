package com.ebook.ebookstore.Services;

import com.ebook.ebookstore.DTO.AdminDTO;
import com.ebook.ebookstore.Model.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminServices {

    Admin createAdmin(AdminDTO adminDTO);

    Admin getAdminByEmail(String email);

    Admin updateAdmin(Integer adminId, AdminDTO adminDTO);

    void deleteAdmin(Integer adminId);

    Admin getAdminByUsername(String username);

    boolean authenticate(String email, String password);

    Optional<Admin> getAdminById(Integer adminId);

    List<Admin> getAllAdmins();
}
