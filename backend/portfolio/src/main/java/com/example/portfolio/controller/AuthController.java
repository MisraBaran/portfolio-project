package com.example.portfolio.controller;

import com.example.portfolio.dto.AuthResponse;
import com.example.portfolio.dto.LoginRequest;
import com.example.portfolio.dto.RegisterRequest;
import com.example.portfolio.model.User;
import com.example.portfolio.security.JwtService;
import com.example.portfolio.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        User u = userService.register(req);
        String token = jwtService.generateToken(u.getEmail());
        return ResponseEntity.ok(new AuthResponse(token)); // JSON body: {"token":"..."}
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        String username = ((UserDetails) auth.getPrincipal()).getUsername();
        String token = jwtService.generateToken(username);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // Debug için alternatif (Map döndürür)
    @PostMapping("/registerMap")
    public Map<String,String> registerMap(@Valid @RequestBody RegisterRequest req) {
        User u = userService.register(req);
        String token = jwtService.generateToken(u.getEmail());
        return Map.of("token", token);
    }
}

