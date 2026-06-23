package com.campus.controller.merchant;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.result.Result;
import com.campus.dto.merchant.AddDishDTO;
import com.campus.dto.merchant.UpdateDishDTO;
import com.campus.entity.Dish;
import com.campus.service.DishService;
import com.campus.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@Tag(name = "商家模块 - 菜品管理", description = "商家新增、修改、上下架菜品")
@RestController
@RequestMapping("/api/merchant/dishes")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@RequireRole(UserRole.MERCHANT)
public class MerchantDishController {

    private final DishService dishService;
    private final MerchantService merchantService;

    @Operation(summary = "获取商家菜品列表")
    @GetMapping
    public Result<List<Dish>> listDishes(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer shelfStatus,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        List<Dish> dishes = dishService.listByMerchant(merchantId, name, category, shelfStatus);
        return Result.success(dishes);
    }

    @Operation(summary = "新增菜品")
    @PostMapping
    public Result<String> addDish(@Valid @RequestBody AddDishDTO dto, HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        merchantService.ensureCanManageDish(merchantId);
        dishService.addDish(dto, merchantId);
        return Result.success("菜品新增成功");
    }

    @Operation(summary = "修改菜品")
    @PutMapping
    public Result<String> updateDish(@Valid @RequestBody UpdateDishDTO dto, HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        merchantService.ensureCanManageDish(merchantId);
        dishService.updateDish(dto, merchantId);
        return Result.success("菜品修改成功");
    }

    @Operation(summary = "上下架菜品")
    @PutMapping("/{dishId}/shelf/{status}")
    public Result<String> changeShelfStatus(
            @PathVariable Long dishId,
            @PathVariable Integer status,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        merchantService.ensureCanManageDish(merchantId);
        dishService.changeShelfStatus(dishId, status, merchantId);
        return Result.success(status == 1 ? "菜品已上架" : "菜品已下架");
    }

    @Operation(summary = "批量上下架菜品")
    @PutMapping("/batch/shelf/{status}")
    public Result<String> batchChangeShelfStatus(
            @PathVariable Integer status,
            @RequestBody List<Long> dishIds,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        merchantService.ensureCanManageDish(merchantId);
        dishService.batchChangeShelfStatus(dishIds, status, merchantId);
        return Result.success("批量操作成功");
    }
}
