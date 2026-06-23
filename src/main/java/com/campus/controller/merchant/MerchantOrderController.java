package com.campus.controller.merchant;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.result.Result;
import com.campus.entity.Orders;
import com.campus.service.MerchantService;
import com.campus.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "商家模块 - 订单管理", description = "商家查看、接单、出餐、取消订单")
@RestController
@RequestMapping("/api/merchant/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@RequireRole(UserRole.MERCHANT)
public class MerchantOrderController {

    private final OrderService orderService;
    private final MerchantService merchantService;

    @Operation(summary = "获取商家订单列表", description = "按状态筛选订单（可选：0待支付/2待接单/3制作中/4待取餐/5已完成）")
    @GetMapping
    public Result<List<Orders>> listOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        List<Orders> orders = orderService.listByMerchant(merchantId, status, orderNo,startDate,endDate);
        return Result.success(orders);
    }

    @Operation(summary = "商家接单", description = "将订单状态改为「制作中」")
    @PutMapping("/{orderId}/accept")
    public Result<String> acceptOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        merchantService.ensureCanHandleOrder(merchantId);
        orderService.acceptOrder(orderId, merchantId);
        return Result.success("接单成功");
    }

    @Operation(summary = "商家批量接单", description = "批量将多个订单改为「制作中」")
    @PutMapping("/batch-accept")
    public Result<String> batchAcceptOrder(
            @RequestBody List<Long> orderIds,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        merchantService.ensureCanHandleOrder(merchantId);
        orderService.batchAccept(orderIds, merchantId);
        return Result.success("批量接单成功，共处理 " + orderIds.size() + " 个订单");
    }

    @Operation(summary = "商家出餐", description = "将订单状态改为「待取餐」")
    @PutMapping("/{orderId}/outmeal")
    public Result<String> outMealOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        merchantService.ensureCanHandleOrder(merchantId);
        orderService.outMealOrder(orderId, merchantId);
        return Result.success("订单已出餐");
    }

    @Operation(summary = "商家批量出餐", description = "批量将多个订单改为「待取餐」")
    @PutMapping("/batch-outmeal")
    public Result<String> batchOutMealOrder(
            @RequestBody List<Long> orderIds,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        merchantService.ensureCanHandleOrder(merchantId);
        orderService.batchOutMeal(orderIds, merchantId);
        return Result.success("批量出餐成功，共处理 " + orderIds.size() + " 个订单");
    }

    @Operation(summary = "商家取消订单", description = "取消待接单/制作中订单，并填写原因")
    @PutMapping("/{orderId}/cancel")
    public Result<String> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String reason,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        merchantService.ensureCanHandleOrder(merchantId);
        orderService.cancelOrder(orderId, reason, merchantId);
        return Result.success("订单取消成功，已发起退款");
    }
}
