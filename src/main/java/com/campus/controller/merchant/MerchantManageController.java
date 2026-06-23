package com.campus.controller.merchant;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.result.Result;
import com.campus.dto.merchant.ApplyMerchantDTO;
import com.campus.dto.merchant.StatusMerchantDTO;
import com.campus.dto.merchant.UpdateMerchantDTO;
import com.campus.entity.Merchant;
import com.campus.entity.MerchantApply;
import com.campus.service.MerchantService;
import com.campus.vo.merchant.MerchantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "商家模块 - 店铺管理")
@RequireRole(UserRole.MERCHANT)
public class MerchantManageController {

    private final MerchantService merchantService;

    @Operation(summary = "获取商家信息")
    @GetMapping("/info")
    public Result<MerchantVO> info(HttpServletRequest request) {
        Long id = Long.parseLong(request.getAttribute("userId").toString());
        Merchant merchant = merchantService.getById(id);
        MerchantVO vo = new MerchantVO();
        BeanUtils.copyProperties(merchant, vo);
        return Result.success(vo);
    }

    @Operation(summary = "修改商家信息")
    @PutMapping("/info")
    public Result<String> update(@Valid @RequestBody UpdateMerchantDTO dto, HttpServletRequest request) {
        Long id = Long.parseLong(request.getAttribute("userId").toString());
        merchantService.updateInfo(dto, id);
        return Result.success("修改成功");
    }

    @Operation(summary = "提交入驻申请")
    @PostMapping("/apply")
    public Result<String> apply(@Valid @RequestBody ApplyMerchantDTO dto) {
        merchantService.applyMerchant(dto);
        return Result.success("申请提交成功");
    }

    @Operation(summary = "根据手机号查询入驻进度")
    @GetMapping("/apply/check")
    public Result<MerchantApply> checkApply(String phone) {
        MerchantApply apply = merchantService.getApplyByPhone(phone);
        return Result.success(apply);
    }

    @Operation(summary = "修改营业状态")
    @PutMapping("/status")
    public Result<String> status(@Valid @RequestBody StatusMerchantDTO dto, HttpServletRequest request) {
        Long id = Long.parseLong(request.getAttribute("userId").toString());
        merchantService.updateMerchantStatus(dto.getStatus(), id);
        return Result.success("状态更新成功");
    }
}
