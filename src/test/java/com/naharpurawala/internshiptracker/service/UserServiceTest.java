package com.naharpurawala.internshiptracker.service;

import com.naharpurawala.internshiptracker.dto.SignupRequest;
import com.naharpurawala.internshiptracker.entity.User;
import com.naharpurawala.internshiptracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() { userService = new UserService(userRepository, passwordEncoder); }

    @Test
    void register_normalizesEmailAndStoresOnlyEncodedPassword() {
        SignupRequest request = signup(" Mohammed ", "  MOHAMMED@Example.COM ", "password123", "password123");
        when(userRepository.existsByEmailIgnoreCase("mohammed@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$encoded-value");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.register(request);
        assertThat(registered.getName()).isEqualTo("Mohammed");
        assertThat(registered.getEmail()).isEqualTo("mohammed@example.com");
        assertThat(registered.getPassword()).isEqualTo("$2a$encoded-value");
        assertThat(registered.getPassword()).doesNotContain("password123");
        verify(passwordEncoder).encode("password123");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$encoded-value");
    }

    @Test
    void register_rejectsDuplicateEmailBeforeSaving() {
        SignupRequest request = signup("Mohammed", "mohammed@example.com", "password123", "password123");
        when(userRepository.existsByEmailIgnoreCase("mohammed@example.com")).thenReturn(true);
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_rejectsPasswordConfirmationMismatch() {
        SignupRequest request = signup("Mohammed", "mohammed@example.com", "password123", "different123");
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passwords do not match");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    private SignupRequest signup(String name, String email, String password, String confirmPassword) {
        SignupRequest request = new SignupRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        return request;
    }
}
