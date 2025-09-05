package com.ebook.ebookstore.Services;

import com.ebook.ebookstore.DTO.UserDTO;
import com.ebook.ebookstore.Model.User;

public interface UserServices {
    User createUser(UserDTO userDTO);
    User getUserByEmail(String email);
    User updateUser(Integer userId, UserDTO userDTO);
    void deleteUser(Integer userId);
    User getUserByUsername(String username);
    boolean authenticate(String email, String password);
}
