package com.ebook.ebookstore.ServicesImpl;

import com.ebook.ebookstore.DTO.AdminDTO;
import com.ebook.ebookstore.Model.Admin;
import com.ebook.ebookstore.Repository.AdminRepository;
import com.ebook.ebookstore.Services.AdminServices;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminServices {

    private final AdminRepository adminRepository;

    @Autowired
    public AdminServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public Admin createAdmin(AdminDTO adminDTO) {
        if (adminRepository.existsByName(adminDTO.getName())) {
            throw new IllegalArgumentException("Admin name already exists");
        }
        if (adminRepository.existsByEmail(adminDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Admin admin = new Admin();
        admin.setName(adminDTO.getName());
        admin.setEmail(adminDTO.getEmail());
        admin.setPassword(adminDTO.getPassword());

        return adminRepository.save(admin);
    }

    @Override
    public Admin getAdminByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    @Override
    public Admin updateAdmin(Integer adminId, AdminDTO adminDTO) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin with id " + adminId + " not found"));

        if (adminDTO.getName() != null && !adminDTO.getName().isBlank()) {
            admin.setName(adminDTO.getName());
        }

        if (adminDTO.getEmail() != null && !adminDTO.getEmail().isBlank()) {
            if (adminRepository.existsByEmailAndIdNot(adminDTO.getEmail(), Long.valueOf(adminId))) {
                throw new IllegalArgumentException("Email is already in use by another admin");
            }
            admin.setEmail(adminDTO.getEmail());
        }

        if (adminDTO.getPassword() != null && !adminDTO.getPassword().isBlank()) {
            admin.setPassword(adminDTO.getPassword());
        }

        return adminRepository.save(admin);
    }

    @Override
    public void deleteAdmin(Integer adminId) {
        if (!adminRepository.existsById(adminId)) {
            throw new EntityNotFoundException("Admin with id " + adminId + " not found");
        }
        adminRepository.deleteById(adminId);
    }

    @Override
    public Admin getAdminByUsername(String username) {
        return adminRepository.findByName(username);
    }

    @Override
    public boolean authenticate(String email, String password) {
        Admin admin = adminRepository.findByEmail(email);
        return admin != null && admin.getPassword().equals(password);
    }

    public Optional<Admin> getAdminById(Integer adminId) {
        return adminRepository.findById(adminId);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }
}
