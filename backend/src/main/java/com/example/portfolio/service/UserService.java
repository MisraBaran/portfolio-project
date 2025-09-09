package com.example.portfolio.service;

import com.example.portfolio.dto.RegisterRequest;
import com.example.portfolio.model.User;
import com.example.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public User register(RegisterRequest r) {
        if (repo.existsByEmail(r.email())) throw new IllegalArgumentException("Email already in use");
        User u = User.builder()
                .email(r.email().toLowerCase())
                .password(encoder.encode(r.password()))
                .build();
        return repo.save(u);
    }

    public User findByEmail(String email) {
        return repo.findByEmail(email).orElseThrow();
    }
}
