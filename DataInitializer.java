package com.example.financeapi.config;

import com.example.financeapi.model.User;
import com.example.financeapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("admin@finance.com", "Admin User", "admin123", User.Role.ADMIN);
        seedUser("analyst@finance.com", "Analyst User", "analyst123", User.Role.ANALYST);
        seedUser("viewer@finance.com", "Viewer User", "viewer123", User.Role.VIEWER);
    }

    private void seedUser(String email, String name, String password, User.Role role) {
        if (!userRepository.existsByEmail(email)) {
            userRepository.save(User.builder()
                    .email(email)
                    .name(name)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .active(true)
                    .build());
            log.info("Seeded {} user: {}", role, email);
        }
    }
}
