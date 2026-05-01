package org.soup.ssu.bench.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.soup.ssu.bench.config.SecurityConfig.PUBLIC_PATHS;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";


    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
        throws ServletException, IOException {

        String path = request.getServletPath();

        for (String pathPattern : PUBLIC_PATHS) {
            if (pathMatcher.match(pathPattern, path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String jwtToken = getJwtToken(request);
        Optional<AuthenticatedUser> jwtPrincipal = jwtService.parseToken(jwtToken);

        jwtPrincipal.ifPresent(this::setAuthentication);

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(AuthenticatedUser authenticatedUser) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(ROLE_PREFIX + authenticatedUser.role());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                List.of(authority)
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getJwtToken(HttpServletRequest request) {
        String headerAuth = request.getHeader(AUTHORIZATION_HEADER);
        if (headerAuth != null && headerAuth.startsWith(BEARER_PREFIX)) {
            return headerAuth.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
