package com.teamtasker.service;

import com.teamtasker.entity.User;
import com.teamtasker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already in use");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Search methods

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found. Username: " + username));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found. Email: " + email));
    }

    // Flexible search - first/last, first + last, last + first
    public List<User> searchUsersByName(String keyword) {
        String[] parts = keyword.trim().split("\\s+");
        if (parts.length == 1) {
            List<User> byFirst = userRepository.findByFirstNameContainingIgnoreCase(parts[0]);
            List<User> byLast = userRepository.findByLastNameContainingIgnoreCase(parts[0]);
            Set<User> combined = new HashSet<>(byFirst);
            combined.addAll(byLast);
            return new ArrayList<>(combined);

        } else if (parts.length > 1) {
            String first = parts[0];
            String last = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
            List<User> forward = userRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(first, last);
            List<User> reverse = userRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(last, first);
            Set<User> combined = new HashSet<>(forward);
            combined.addAll(reverse);
            return new ArrayList<>(combined);
        }
        return Collections.emptyList();
    }

    //------------------------------------------------------------------------------------------------------------------
    // User Management

    public User updateUser(User updatedUser) {
        User existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found. Id: " + updatedUser.getId()));
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        return userRepository.save(existingUser);
    }

    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found. Id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found. Id: " + userId));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
