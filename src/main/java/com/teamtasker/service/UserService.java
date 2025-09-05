package com.teamtasker.service;

import com.teamtasker.entity.Role;
import com.teamtasker.entity.User;
import com.teamtasker.exception.*;
import com.teamtasker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User registerAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }

    public User registerManager(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.MANAGER);
        return userRepository.save(user);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean isEmailTaken(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    //------------------------------------------------------------------------------------------------------------------
    // Search methods

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found. UserID: " + userId));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found. Username: " + username));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found. Email: " + email));
    }

    // Flexible search - username, first/last, first + last, last + first
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        return userRepository.searchByNameOrUsername(searchTerm.trim(), pageable);
    }

    //------------------------------------------------------------------------------------------------------------------
    // User Management

    public User updateUser(User updatedUser) {
        User existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found. Id: " + updatedUser.getId()));
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        return userRepository.save(existingUser);
    }

    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found. Id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found. Id: " + userId));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidChangePasswordException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Role Management (Admin functions)

    public void updateUserRole(Integer userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found. Id: " + userId));
        user.setRole(newRole);
        userRepository.save(user);
    }

    public Page<User> getUsersByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Statistics

    public Page<User> getUsersNotInTeam(Integer teamId, Pageable pageable) {
        return userRepository.findUsersNotInTeam(teamId, pageable);
    }

    public long getUserCountByRole(Role role) {
        return userRepository.countByRole(role);
    }

    public Map<Role, Long> getUserCountsByRole() {
        Map<Role, Long> counts = new HashMap<>();
        for (Role role : Role.values()) {
            counts.put(role, getUserCountByRole(role));
        }
        return counts;
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("usersByRole", getUserCountsByRole());
        return stats;
    }
}