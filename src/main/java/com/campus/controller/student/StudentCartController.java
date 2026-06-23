package com.campus.controller.student;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.result.Result;
import com.campus.dto.student.CartAddDTO;
import com.campus.dto.student.CartUpdateDTO;
import com.campus.entity.CartItem;
import com.campus.entity.Dish;
import com.campus.entity.Merchant;
import com.campus.service.CartItemService;
import com.campus.service.DishService;
import com.campus.service.MerchantService;
import com.campus.vo.student.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Tag(name = "学生端-购物车")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/student/cart")
@RequiredArgsConstructor
@RequireRole(UserRole.STUDENT)
public class StudentCartController {

    private final CartItemService cartItemService;
    private final DishService dishService;
    private final MerchantService merchantService;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @Operation(summary = "查询购物车")
    @GetMapping
    public Result<List<CartVO>> listCart(@Parameter(hidden = true) HttpServletRequest request) {
        Long userId = getUserId(request);
        List<CartVO> list = cartItemService.lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getUpdatedAt)
                .list()
                .stream()
                .map(this::toCartVO)
                .toList();
        return Result.success(list);
    }

    @Operation(summary = "添加购物车")
    @PostMapping
    public Result<Void> addCart(@Valid @RequestBody CartAddDTO dto,
                                @Parameter(hidden = true) HttpServletRequest request) {
        Long userId = getUserId(request);
        Dish dish = dishService.getById(dto.getDishId());
        if (dish == null || !Objects.equals(dish.getShelfStatus(), 1)) {
            return Result.error(400, "菜品不存在或已下架");
        }
        Merchant merchant = merchantService.getById(dish.getMerchantId());
        if (merchant == null || !Integer.valueOf(1).equals(merchant.getStatus())) {
            return Result.error(400, "商家当前非营业状态，暂不能加入购物车");
        }

        CartItem item = cartItemService.lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getDishId, dto.getDishId())
                .one();
        if (item == null) {
            item = new CartItem();
            item.setUserId(userId);
            item.setDishId(dto.getDishId());
            item.setQuantity(dto.getQuantity());
            item.setCreatedAt(LocalDateTime.now());
        } else {
            item.setQuantity(item.getQuantity() + dto.getQuantity());
        }
        item.setUpdatedAt(LocalDateTime.now());
        cartItemService.saveOrUpdate(item);
        return Result.success();
    }

    @Operation(summary = "修改购物车数量")
    @PutMapping
    public Result<Void> updateCart(@Valid @RequestBody CartUpdateDTO dto,
                                   @Parameter(hidden = true) HttpServletRequest request) {
        Long userId = getUserId(request);
        CartItem item = cartItemService.getById(dto.getCartItemId());
        if (item == null || !item.getUserId().equals(userId)) {
            return Result.error(400, "购物车项不存在");
        }
        item.setQuantity(dto.getQuantity());
        item.setUpdatedAt(LocalDateTime.now());
        cartItemService.updateById(item);
        return Result.success();
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCartItem(
            @Parameter(description = "购物车项ID") @PathVariable Long id,
            @Parameter(hidden = true) HttpServletRequest request) {
        Long userId = getUserId(request);
        CartItem item = cartItemService.getById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            return Result.error(400, "购物车项不存在");
        }
        cartItemService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping
    public Result<Void> clearCart(@Parameter(hidden = true) HttpServletRequest request) {
        Long userId = getUserId(request);
        cartItemService.lambdaUpdate()
                .eq(CartItem::getUserId, userId)
                .remove();
        return Result.success();
    }

    private CartVO toCartVO(CartItem item) {
        Dish dish = dishService.getById(item.getDishId());
        CartVO vo = new CartVO();
        vo.setId(item.getId());
        vo.setDishId(item.getDishId());
        vo.setQuantity(item.getQuantity());
        if (dish != null) {
            vo.setMerchantId(dish.getMerchantId());
            vo.setDishName(dish.getName());
            vo.setDishImage(dish.getImageUrl());
            vo.setPrice(dish.getPrice());
            vo.setSubtotal(dish.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
        }
        return vo;
    }
}
