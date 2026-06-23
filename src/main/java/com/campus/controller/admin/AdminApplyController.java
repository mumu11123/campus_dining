package com.campus.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.result.Result;
import com.campus.entity.Merchant;
import com.campus.entity.MerchantApply;
import com.campus.entity.MerchantBlacklist;
import com.campus.mapper.MerchantBlacklistMapper;
import com.campus.mapper.MerchantMapper;
import com.campus.service.MerchantApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "管理员端-商家入驻审核")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireRole(UserRole.ADMIN)
public class AdminApplyController {

    private final MerchantApplyService merchantApplyService;
    private final MerchantBlacklistMapper merchantBlacklistMapper;
    private final MerchantMapper merchantMapper;

    @Operation(summary = "查询所有入驻申请")
    @GetMapping("/apply/list")
    public Result<?> list(@RequestParam(required = false) Integer status,
                          HttpServletRequest request) {
        requireAdmin(request);
        if (status != null) {
            return Result.success(merchantApplyService.listApplicationsByStatus(status));
        }
        return Result.success(merchantApplyService.listApplications());
    }

    @Operation(summary = "审核通过并创建商家账号")
    @PutMapping("/apply/{id}/approve")
    public Result<?> approve(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = getAdminId(request);
        requireAdmin(request);
        Map<String, String> account = merchantApplyService.approve(id, adminId);
        return Result.success("审核通过，已创建商家账号", account);
    }

    @PostMapping("/apply/{id}/approve")
    public Result<?> approveByPost(@PathVariable Long id, HttpServletRequest request) {
        return approve(id, request);
    }

    @Operation(summary = "审核驳回")
    @PutMapping("/apply/{id}/reject")
    public Result<?> reject(@PathVariable Long id,
                            @RequestParam(defaultValue = "") String rejectReason,
                            HttpServletRequest request) {
        Long adminId = getAdminId(request);
        requireAdmin(request);
        merchantApplyService.reject(id, rejectReason, adminId);
        return Result.success("已驳回");
    }

    @PostMapping("/apply/{id}/reject")
    public Result<?> rejectByPost(@PathVariable Long id,
                                  @RequestParam(defaultValue = "") String rejectReason,
                                  HttpServletRequest request) {
        return reject(id, rejectReason, request);
    }

    @Operation(summary = "退回补充资料")
    @PutMapping("/apply/{id}/supplement")
    public Result<?> supplement(@PathVariable Long id,
                                @RequestParam(defaultValue = "") String reason,
                                HttpServletRequest request) {
        requireAdmin(request);
        MerchantApply apply = merchantApplyService.getById(id);
        if (apply == null) {
            throw new com.campus.common.exception.BusinessException(404, "申请记录不存在");
        }
        if (!Integer.valueOf(0).equals(apply.getStatus())) {
            throw new com.campus.common.exception.BusinessException(400, "申请已处理，不能退回补充");
        }
        apply.setStatus(3);
        apply.setReviewerId(getAdminId(request));
        apply.setReviewComment(reason);
        apply.setReviewedAt(LocalDateTime.now());
        merchantApplyService.updateById(apply);
        return Result.success("已退回补充资料");
    }

    @Operation(summary = "加入黑名单")
    @PostMapping("/apply/{id}/blacklist")
    public Result<?> addBlacklist(@PathVariable Long id,
                                  @RequestParam String reason,
                                  HttpServletRequest request) {
        requireAdmin(request);
        Long adminId = getAdminId(request);
        merchantApplyService.addToBlacklist(id, reason, adminId);
        return Result.success("已加入黑名单");
    }

    @Operation(summary = "移出黑名单")
    @DeleteMapping("/apply/{id}/blacklist/{merchantId}")
    public Result<?> removeBlacklist(@PathVariable Long id,
                                     @PathVariable Long merchantId,
                                     HttpServletRequest request) {
        requireAdmin(request);
        merchantBlacklistMapper.delete(
                new LambdaQueryWrapper<MerchantBlacklist>()
                        .eq(MerchantBlacklist::getMerchantId, merchantId)
        );
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant != null && Integer.valueOf(3).equals(merchant.getStatus())) {
            merchant.setStatus(2);
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantMapper.updateById(merchant);
        }
        return Result.success("已移出黑名单");
    }

    @Operation(summary = "查看黑名单列表")
    @GetMapping("/blacklist")
    public Result<?> blacklist(HttpServletRequest request) {
        requireAdmin(request);
        List<MerchantBlacklist> list = merchantBlacklistMapper.selectList(
                new LambdaQueryWrapper<MerchantBlacklist>()
                        .orderByDesc(MerchantBlacklist::getCreatedAt)
        );
        list.forEach(item -> {
            Merchant merchant = merchantMapper.selectById(item.getMerchantId());
            if (merchant != null) {
                item.setShopName(merchant.getShopName());
            }
        });
        return Result.success(list);
    }

    private void requireAdmin(HttpServletRequest request) {
        String role = String.valueOf(request.getAttribute("role"));
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new com.campus.common.exception.BusinessException(403, "无管理员权限");
        }
    }

    private Long getAdminId(HttpServletRequest request) {
        return Long.parseLong(request.getAttribute("userId").toString());
    }
}
