package com.campus.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.Result;
import com.campus.entity.Dish;
import com.campus.entity.Merchant;
import com.campus.mapper.DishMapper;
import com.campus.mapper.MerchantMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "管理员端-菜品销量")
@RestController
@RequestMapping("/api/admin/dishes")
@RequiredArgsConstructor
@RequireRole(UserRole.ADMIN)
public class AdminDishController {

    private final DishMapper dishMapper;
    private final MerchantMapper merchantMapper;

    @Operation(summary = "查询菜品月销量")
    @GetMapping
    public Result<List<AdminDishVO>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Long merchantId,
                                          @RequestParam(required = false) Integer shelfStatus,
                                          HttpServletRequest request) {
        requireAdmin(request);
        List<Dish> dishes = dishMapper.selectList(
                new LambdaQueryWrapper<Dish>()
                        .eq(merchantId != null, Dish::getMerchantId, merchantId)
                        .eq(shelfStatus != null, Dish::getShelfStatus, shelfStatus)
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(Dish::getName, keyword)
                                .or()
                                .like(Dish::getCategory, keyword))
                        .orderByDesc(Dish::getMonthlySales)
                        .orderByDesc(Dish::getUpdatedAt)
        );

        Map<Long, String> merchantNames = merchantMapper.selectList(null).stream()
                .collect(Collectors.toMap(Merchant::getId, Merchant::getShopName, (a, b) -> a));

        return Result.success(dishes.stream()
                .map(dish -> AdminDishVO.from(dish, merchantNames.get(dish.getMerchantId())))
                .toList());
    }

    private void requireAdmin(HttpServletRequest request) {
        String role = String.valueOf(request.getAttribute("role"));
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new BusinessException(403, "无管理员权限");
        }
    }

    @Data
    public static class AdminDishVO {
        private Long id;
        private Long merchantId;
        private String shopName;
        private String name;
        private String category;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private BigDecimal discount;
        private Integer monthlySales;
        private Integer stockStatus;
        private Integer shelfStatus;
        private LocalDateTime updatedAt;

        public static AdminDishVO from(Dish dish, String shopName) {
            AdminDishVO vo = new AdminDishVO();
            vo.setId(dish.getId());
            vo.setMerchantId(dish.getMerchantId());
            vo.setShopName(shopName);
            vo.setName(dish.getName());
            vo.setCategory(dish.getCategory());
            vo.setPrice(dish.getPrice());
            vo.setOriginalPrice(dish.getOriginalPrice());
            vo.setDiscount(dish.getDiscount());
            vo.setMonthlySales(dish.getMonthlySales());
            vo.setStockStatus(dish.getStockStatus());
            vo.setShelfStatus(dish.getShelfStatus());
            vo.setUpdatedAt(dish.getUpdatedAt());
            return vo;
        }
    }
}
