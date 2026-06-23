package com.campus.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.result.Result;
import com.campus.entity.Dish;
import com.campus.entity.Merchant;
import com.campus.service.DishService;
import com.campus.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "学生端-菜品浏览")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@RequireRole(UserRole.STUDENT)
public class StudentDishController {

    private final DishService dishService;
    private final MerchantService merchantService;

    @Operation(summary = "查询菜品列表")
    @GetMapping("/dishes")
    public Result<List<Dish>> listDishes(
            @Parameter(description = "商家ID，不传则查询全部上架菜品")
            @RequestParam(required = false) Long merchantId,
            @Parameter(description = "菜品分类，不传则不过滤分类")
            @RequestParam(required = false) String category) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getShelfStatus, 1);
        if (merchantId != null) {
            Merchant merchant = merchantService.getById(merchantId);
            if (merchant == null || !Integer.valueOf(1).equals(merchant.getStatus())) {
                return Result.success(List.of());
            }
            wrapper.eq(Dish::getMerchantId, merchantId);
        } else {
            List<Long> activeMerchantIds = merchantService.lambdaQuery()
                    .eq(Merchant::getStatus, 1)
                    .list()
                    .stream()
                    .map(Merchant::getId)
                    .toList();
            if (activeMerchantIds.isEmpty()) {
                return Result.success(List.of());
            }
            wrapper.in(Dish::getMerchantId, activeMerchantIds);
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(Dish::getCategory, category);
        }
        wrapper.orderByDesc(Dish::getMonthlySales);
        return Result.success(dishService.list(wrapper));
    }
}
