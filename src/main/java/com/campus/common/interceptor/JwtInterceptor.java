package com.campus.common.interceptor;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Optional;

/**
 * JWT认证拦截器
 * 拦截需要登录的请求，验证Token是否有效
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证 Token");
            return false;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token 无效或已过期");
            return false;
        }

        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        if (!hasRequiredRole(handler, role)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "当前角色无权访问该接口");
            return false;
        }

        return true;
    }

    private boolean hasRequiredRole(Object handler, String tokenRole) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequireRole.class);
        if (requireRole == null) {
            requireRole = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(), RequireRole.class);
        }
        if (requireRole == null) {
            return true;
        }

        Optional<UserRole> currentRole = UserRole.fromToken(tokenRole);
        return currentRole.isPresent()
                && Arrays.stream(requireRole.value()).anyMatch(currentRole.get()::equals);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":" + status + ",\"message\":\"" + message + "\",\"data\":null}");
    }
}
