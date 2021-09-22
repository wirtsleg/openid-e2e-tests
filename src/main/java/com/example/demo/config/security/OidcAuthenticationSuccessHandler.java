package com.example.demo.config.security;

import com.example.demo.dto.Account;
import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Collections.singleton;

@Slf4j
@Component
@Conditional(ClientsConfiguredCondition.class)
@RequiredArgsConstructor
public class OidcAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AccountRepository accRepo;

    @Override public void onAuthenticationSuccess(
        HttpServletRequest req,
        HttpServletResponse res,
        Authentication authentication
    ) throws IOException {
        try {
            OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken)authentication;
            OidcUser oidcUser = (OidcUser)auth.getPrincipal();

            if (ObjectUtils.isEmpty(oidcUser.getEmail()))
                throw new AuthenticationServiceException("Email not found for subject: " + oidcUser.getName());

            Account acc = accRepo.findByEmail(oidcUser.getEmail())
                .orElseGet(() -> accRepo.insert(createAccount(oidcUser)));

            SecurityContextHolder.getContext().setAuthentication(
                new PreAuthenticatedAuthenticationToken(acc, acc.getPassword(), acc.getAuthorities())
            );

            res.sendRedirect("/");
        }
        catch (Exception e) {
            log.warn("Failed to authenticate OpenID user [errorMessage={}]", e.getMessage(), e);

            SecurityContextHolder.clearContext();

            res.sendRedirect(buildCallbackUrl(req, e));
        }
    }

    private Account createAccount(OidcUser oidcUser) {
        return new Account()
            .setEmail(oidcUser.getEmail())
            .setFirstName(oidcUser.getGivenName())
            .setLastName(oidcUser.getFamilyName())
            .setAuthorities(singleton("ROLE_USER"));
    }

    private String buildCallbackUrl(HttpServletRequest req, Exception e) {
        return ServletUriComponentsBuilder.fromRequest(req)
            .replacePath("/")
            .replaceQuery("error=" + e.getMessage())
            .build().encode().toUriString();
    }
}
