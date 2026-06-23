package com.campus.common.interceptor;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;

class JwtInterceptorTest {

    private JwtUtil jwtUtil;
    private JwtInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
                "test-jwt-secret-key-must-be-at-least-256-bits-long",
                60_000L);
        interceptor = new JwtInterceptor(jwtUtil);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldReturn401WhenTokenIsMissing() throws Exception {
        boolean allowed = interceptor.preHandle(
                request, response, handler("studentEndpoint"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void shouldReturn403WhenRoleDoesNotMatch() throws Exception {
        prepareValidToken("merchant");

        boolean allowed = interceptor.preHandle(
                request, response, handler("studentEndpoint"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("\"code\":403");
    }

    @Test
    void shouldAllowMatchingRoleAndExposeIdentity() throws Exception {
        prepareValidToken("student");

        boolean allowed = interceptor.preHandle(
                request, response, handler("studentEndpoint"));

        assertThat(allowed).isTrue();
        assertThat(request.getAttribute("userId")).isEqualTo(100L);
        assertThat(request.getAttribute("role")).isEqualTo("student");
    }

    @Test
    void methodRoleShouldOverrideControllerRole() throws Exception {
        prepareValidToken("admin");

        boolean allowed = interceptor.preHandle(
                request, response, handler("adminEndpoint"));

        assertThat(allowed).isTrue();
    }

    private void prepareValidToken(String role) {
        String token = jwtUtil.generateToken(100L, role);
        request.addHeader("Authorization", "Bearer " + token);
    }

    private HandlerMethod handler(String methodName) throws NoSuchMethodException {
        TestController controller = new TestController();
        return new HandlerMethod(controller, TestController.class.getMethod(methodName));
    }

    @RequireRole(UserRole.STUDENT)
    static class TestController {

        public void studentEndpoint() {
        }

        @RequireRole(UserRole.ADMIN)
        public void adminEndpoint() {
        }
    }
}
