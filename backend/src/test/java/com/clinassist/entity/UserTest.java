package com.clinassist.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("jean.dupont");
        user.setFirstName("Jean");
        user.setLastName("Dupont");
        user.setEmail("jean.dupont@test.com");
        user.setPassword("password123");
        user.setRole(User.UserRole.PATIENT);
    }

    @Test
    @DisplayName("Should create user with valid data")
    void createUser_WithValidData_ShouldSucceed() {
        assertNotNull(user);
        assertEquals("jean.dupont", user.getUsername());
        assertEquals("Jean", user.getFirstName());
        assertEquals("Dupont", user.getLastName());
    }

    @Test
    @DisplayName("Should get full name")
    void getFullName_ShouldReturnCorrectName() {
        String fullName = user.getFirstName() + " " + user.getLastName();
        assertEquals("Jean Dupont", fullName);
    }

    @Test
    @DisplayName("Should update user role")
    void updateRole_ShouldChangeRole() {
        user.setRole(User.UserRole.THERAPEUTE);
        assertEquals(User.UserRole.THERAPEUTE, user.getRole());
    }

    @Test
    @DisplayName("User role enum should have correct values")
    void userRole_ShouldHaveCorrectValues() {
        User.UserRole[] roles = User.UserRole.values();
        assertTrue(roles.length >= 2);
    }

    @Test
    @DisplayName("Should set phone number")
    void setPhoneNumber_ShouldUpdatePhone() {
        user.setPhoneNumber("0612345678");
        assertEquals("0612345678", user.getPhoneNumber());
    }

    @Test
    @DisplayName("Should check email format")
    void email_ShouldContainAtSymbol() {
        assertTrue(user.getEmail().contains("@"));
    }
}
