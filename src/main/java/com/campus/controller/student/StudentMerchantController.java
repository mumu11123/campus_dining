package com.campus.controller.student;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.result.Result;
import com.campus.entity.Merchant;
import com.campus.entity.User;
import com.campus.service.MerchantService;
import com.campus.service.UserService;
import com.campus.vo.merchant.MerchantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "学生端-商家浏览")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@RequireRole(UserRole.STUDENT)
public class StudentMerchantController {

    private final MerchantService merchantService;
    private final UserService userService;

    @Operation(summary = "获取营业中商家列表")
    @GetMapping("/merchants")
    public Result<List<MerchantVO>> getMerchants() {
        List<MerchantVO> merchants = merchantService.lambdaQuery()
                .eq(Merchant::getStatus, 1)
                .orderByDesc(Merchant::getUpdatedAt)
                .orderByDesc(Merchant::getCreatedAt)
                .list()
                .stream()
                .map(this::toVO)
                .toList();
        return Result.success(merchants);
    }

    @Operation(summary = "获取当前学生信息")
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(400, "用户不存在");
        }
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("realName", user.getRealName());
        profile.put("phone", user.getPhone());
        profile.put("avatar", user.getAvatar());
        return Result.success(profile);
    }

    private MerchantVO toVO(Merchant merchant) {
        MerchantVO vo = new MerchantVO();
        BeanUtils.copyProperties(merchant, vo);
        vo.setUsername(null);
        vo.setBusinessLicense(null);
        return vo;
    }
}
