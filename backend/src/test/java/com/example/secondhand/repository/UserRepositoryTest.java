package com.example.secondhand.repository;

import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import com.example.secondhand.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String username, String phone, UserStatus status) {
        return User.builder()
                .name("Ali Ahmadi").username(username).password("hashed-password")
                .phone(phone).email("ali@example.com").role(Role.USER).status(status).build();
    }

    // ==================== findByUsername ====================

    @Test
    void findByUsername_shouldReturnUser_whenUsernameExists() {
        userRepository.save(buildUser("ali123", "09121234567", UserStatus.ACTIVE));

        Optional<User> result = userRepository.findByUsername("ali123");

        assertTrue(result.isPresent());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenUsernameDoesNotExist() {
        assertTrue(userRepository.findByUsername("unknown").isEmpty());
    }

    // ==================== existsByUsername / existsByPhone ====================

    @Test
    void existsByUsername_shouldReturnTrue_whenUsernameExists() {
        userRepository.save(buildUser("ali123", "09121234567", UserStatus.ACTIVE));

        assertTrue(userRepository.existsByUsername("ali123"));
        assertFalse(userRepository.existsByUsername("someone-else"));
    }

    @Test
    void existsByPhone_shouldReturnTrue_whenPhoneExists() {
        userRepository.save(buildUser("ali123", "09121234567", UserStatus.ACTIVE));

        assertTrue(userRepository.existsByPhone("09121234567"));
        assertFalse(userRepository.existsByPhone("09129999999"));
    }

    // ==================== countByStatus ====================

    @Test
    void countByStatus_shouldReturnCorrectCount() {
        userRepository.save(buildUser("active1", "09121111111", UserStatus.ACTIVE));
        userRepository.save(buildUser("active2", "09122222222", UserStatus.ACTIVE));
        userRepository.save(buildUser("blocked1", "09123333333", UserStatus.BLOCKED));

        assertEquals(2, userRepository.countByStatus(UserStatus.ACTIVE));
        assertEquals(1, userRepository.countByStatus(UserStatus.BLOCKED));
    }

    // ==================== unique constraint enforcement (real DB behavior) ====================

    @Test
    void save_shouldThrowDataAccessException_whenUsernameIsDuplicated() {
        userRepository.saveAndFlush(buildUser("ali123", "09121234567", UserStatus.ACTIVE));

        User duplicateUsername = buildUser("ali123", "09121111111", UserStatus.ACTIVE);

        assertThrows(DataAccessException.class,
                () -> userRepository.saveAndFlush(duplicateUsername));
    }

    @Test
    void save_shouldThrowDataAccessException_whenPhoneIsDuplicated() {
        userRepository.saveAndFlush(buildUser("ali123", "09121234567", UserStatus.ACTIVE));

        User duplicatePhone = buildUser("someone_else", "09121234567", UserStatus.ACTIVE);

        assertThrows(DataAccessException.class,
                () -> userRepository.saveAndFlush(duplicatePhone));
    }
}
