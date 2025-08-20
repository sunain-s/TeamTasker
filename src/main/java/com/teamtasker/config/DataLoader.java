package com.teamtasker.config;

import com.teamtasker.entity.Role;
import com.teamtasker.entity.User;
import com.teamtasker.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final UserService userService;

    @Autowired
    public DataLoader(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void loadInitialData() {
        createDefaultAdmin();
        createTestUsers();
    }

    private void createDefaultAdmin() {
        if (!userService.isUsernameTaken("admin")) {
            User admin = new User("Admin", "User", "admin@test.com", "admin", "password123");
            userService.registerAdmin(admin);
            System.out.println("✅ Default admin user created:");
            System.out.println("   Username: admin");
            System.out.println("   Password: password123");
            System.out.println("   Role: ADMIN");
        } else {
            System.out.println("ℹ️ Admin user already exists");
        }
    }

    private void createTestUsers() {
        // Create a test regular user for testing
        if (!userService.isUsernameTaken("testuser")) {
            User testUser = new User("Test", "User", "test@test.com", "testuser", "password123");
            userService.registerUser(testUser);
            System.out.println("✅ Test user created:");
            System.out.println("   Username: testuser");
            System.out.println("   Password: password123");
            System.out.println("   Role: USER");
        }

        // Create a test manager user
        if (!userService.isUsernameTaken("manager")) {
            User manager = new User("Manager", "User", "manager@test.com", "manager", "password123");
            userService.registerManager(manager);
            System.out.println("✅ Test manager created:");
            System.out.println("   Username: manager");
            System.out.println("   Password: password123");
            System.out.println("   Role: MANAGER");
        }
    }
}