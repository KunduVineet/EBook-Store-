package com.ebook.ebookstore.ServicesImpl;

import com.ebook.ebookstore.DTO.UserDTO;
import com.ebook.ebookstore.Model.User;
import com.ebook.ebookstore.Repository.UserRepository;
import com.ebook.ebookstore.Services.UserServices;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserServices {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(UserDTO userDTO) {
        if(userRepository.existsByName(userDTO.getUsername())){
            throw new IllegalArgumentException("Username already exists");
        }
        if(userRepository.existsByEmail(userDTO.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setName(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        return userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User updateUser(Integer userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("User with id " + userId + " not found"));

        // Fixed logic: should check if NOT blank, not if blank
        if(userDTO.getUsername() != null && !userDTO.getUsername().isBlank()){
            user.setName(userDTO.getUsername());
        }

        if(userDTO.getEmail() != null && !userDTO.getEmail().isBlank()){
            if(userRepository.existsByEmailAndIdNot(userDTO.getEmail(), userId)){
                throw new IllegalArgumentException("Email is already in use by another user");
            }
            user.setEmail(userDTO.getEmail());
        }

        if(userDTO.getPassword() != null && !userDTO.getPassword().isBlank()){
            user.setPassword(userDTO.getPassword());
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id " + userId + " not found");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByName(username);
    }

    @Override
    public boolean authenticate(String email, String password) {
        User user = userRepository.findByEmail(email);
        return user != null && user.getPassword().equals(password);
    }

    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }
}