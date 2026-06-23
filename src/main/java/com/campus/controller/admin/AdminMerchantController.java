package com.campus.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.Result;
import com.campus.entity.Merchant;
import com.campus.entity.MerchantBlacklist;
import com.campus.mapper.MerchantBlacklistMapper;
import com.campus.mapper.MerchantMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "管理员端-商家信息与状态管控")
@RestController
@RequestMapping("/api/admin/merchants")
@RequiredArgsConstructor
@RequireRole(UserRole.ADMIN)
public class AdminMerchantController {

    private final MerchantMapper merchantMapper;
    private final MerchantBlacklistMapper merchantBlacklistMapper;

    @Operation(summary = "查询已入驻商家列表")
    @GetMapping
    public Result<List<Merchant>> list(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer status,
                                       HttpServletRequest request) {
        requireAdmin(request);
        List<Merchant> merchants = merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(Merchant::getShopName, keyword)
                                .or()
                                .like(Merchant::getPhone, keyword)
                                .or()
                                .like(Merchant::getContactPerson, keyword))
                        .eq(status != null, Merchant::getStatus, status)
                        .orderByDesc(Merchant::getCreatedAt)
                        .orderByDesc(Merchant::getId)
        );
        return Result.success(merchants);
    }

    @Operation(summary = "管理员修改商家信息")
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id,
                                 @RequestBody AdminMerchantUpdateDTO dto,
                                 HttpServletRequest request) {
        requireAdmin(request);
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商家不存在");
        }
        if (StringUtils.hasText(dto.getShopName())) merchant.setShopName(dto.getShopName());
        if (StringUtils.hasText(dto.getCategory())) merchant.setCategory(dto.getCategory());
        if (StringUtils.hasText(dto.getContactPerson())) merchant.setContactPerson(dto.getContactPerson());
        if (StringUtils.hasText(dto.getPhone())) merchant.setPhone(dto.getPhone());
        if (StringUtils.hasText(dto.getAddress())) merchant.setAddress(dto.getAddress());
        if (dto.getDescription() != null) merchant.setDescription(dto.getDescription());
        if (StringUtils.hasText(dto.getBusinessLicense())) merchant.setBusinessLicense(dto.getBusinessLicense());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        return Result.success("商家信息已更新");
    }

    @Operation(summary = "管理员管控商家状态")
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id,
                                       @RequestParam Integer status,
                                       HttpServletRequest request) {
        requireAdmin(request);
        if (status == null || (status != 1 && status != 2 && status != 3)) {
            throw new BusinessException(400, "状态值不合法");
        }
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商家不存在");
        }
        merchant.setStatus(status);
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        return Result.success("商家状态已更新");
    }

    @Operation(summary = "将已入驻商家加入黑名单")
    @PostMapping("/{id}/blacklist")
    public Result<String> addBlacklist(@PathVariable Long id,
                                       @RequestParam String reason,
                                       HttpServletRequest request) {
        requireAdmin(request);
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商家不存在");
        }
        MerchantBlacklist exist = merchantBlacklistMapper.selectOne(
                new LambdaQueryWrapper<MerchantBlacklist>()
                        .eq(MerchantBlacklist::getMerchantId, id)
                        .last("LIMIT 1")
        );
        if (exist == null) {
            MerchantBlacklist blacklist = new MerchantBlacklist();
            blacklist.setMerchantId(id);
            blacklist.setReason(reason);
            blacklist.setCreatedAt(LocalDateTime.now());
            merchantBlacklistMapper.insert(blacklist);
        } else {
            exist.setReason(reason);
            exist.setCreatedAt(LocalDateTime.now());
            merchantBlacklistMapper.updateById(exist);
        }
        merchant.setStatus(3);
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        return Result.success("商家已加入黑名单并禁止营业");
    }

    private void requireAdmin(HttpServletRequest request) {
        String role = String.valueOf(request.getAttribute("role"));
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new BusinessException(403, "无管理员权限");
        }
    }

    @Data
    public static class AdminMerchantUpdateDTO {
        private String shopName;
        private String category;
        private String contactPerson;
        private String phone;
        private String address;
        private String description;
        private String businessLicense;
    }
}
