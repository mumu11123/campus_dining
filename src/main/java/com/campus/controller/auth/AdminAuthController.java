package com.campus.controller.auth;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.result.Result;
import com.campus.entity.Admin;
import com.campus.mapper.AdminMapper;
import com.campus.utils.JwtUtil;
import com.campus.vo.admin.AdminVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "管理员认证")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminAuthController {

    private final JwtUtil jwtUtil;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "管理员登录")
    @PostMapping("/auth/admin/login")
    public Result<?> login(@RequestParam String username, @RequestParam String password) {
        Admin admin = adminMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, username)
        );
        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            return Result.error(400, "账号或密码错误");
        }
        if (Integer.valueOf(0).equals(admin.getStatus())) {
            return Result.error(403, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(admin.getId(), "ADMIN");
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        return Result.success("登录成功", data);
    }

    @Operation(summary = "获取管理员信息")
    @GetMapping("/admin/info")
    @RequireRole(UserRole.ADMIN)
    public Result<AdminVO> info(HttpServletRequest request) {
        Long adminId = Long.parseLong(request.getAttribute("userId").toString());
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            return Result.error(400, "管理员不存在");
        }
        AdminVO vo = new AdminVO();
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setRealName(admin.getRealName());
        vo.setRole(admin.getRole());
        vo.setStatus(admin.getStatus());
        return Result.success(vo);
    }
}
