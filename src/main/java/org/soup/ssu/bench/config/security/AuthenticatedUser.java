package org.soup.ssu.bench.config.security;

import lombok.Builder;

import java.math.BigInteger;

@Builder
public record AuthenticatedUser(BigInteger id, String username, String role) {
}
