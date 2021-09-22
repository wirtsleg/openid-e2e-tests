package com.example.demo.web;

import com.example.demo.dto.AuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
public class MainController {

    private final OAuth2ClientProperties oauth2Props;

    @GetMapping("/api/user")
    public Authentication getUser(Authentication auth) {
        return auth;
    }

    @GetMapping("/api/auth-config")
    public AuthConfig getAuthConfig() {
        return new AuthConfig()
            .setProviders(new ArrayList<>(oauth2Props.getRegistration().keySet()));
    }
}
