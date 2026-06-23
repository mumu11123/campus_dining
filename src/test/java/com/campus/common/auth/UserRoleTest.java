package com.campus.common.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    @Test
    void shouldParseRoleIgnoringCaseAndSpaces() {
        assertThat(UserRole.fromToken(" merchant ")).contains(UserRole.MERCHANT);
        assertThat(UserRole.fromToken("ADMIN")).contains(UserRole.ADMIN);
    }

    @Test
    void shouldRejectMissingOrUnknownRole() {
        assertThat(UserRole.fromToken(null)).isEmpty();
        assertThat(UserRole.fromToken("")).isEmpty();
        assertThat(UserRole.fromToken("operator")).isEmpty();
    }
}
