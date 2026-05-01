package org.soup.ssu.bench.security;

import lombok.Builder;

import java.math.BigInteger;

@Builder
public record AuthenticatedUser(BigInteger userId, String username, String role) {
}
