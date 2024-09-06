package com.osasea.main.service;

import com.osasea.main.models.User;
import com.osasea.main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Save a user
    public void saveUser(User user) {
        // You might want to add additional validation or encoding here
        userRepository.save(user);
    }

    // Find a user by ID
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    // Find a user by username
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Get a list of all users
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // Delete a user by ID
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}