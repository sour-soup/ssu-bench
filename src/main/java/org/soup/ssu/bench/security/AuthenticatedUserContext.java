package org.soup.ssu.bench.security;

import org.soup.ssu.bench.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticatedUserContext {

    public AuthenticatedUser getAuthenticatedUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(Authentication::getPrincipal)
            .map(AuthenticatedUser.class::cast)
            .orElseThrow(UnauthorizedException::new);
    }
}
