package org.soup.ssu.bench.feature.auth.login;

import ssu.bench.model.RoleEnum;

import java.math.BigInteger;

public record LoginCredentials(BigInteger id, String passwordHash, RoleEnum role) {
}
