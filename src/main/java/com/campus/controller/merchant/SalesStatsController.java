package com.campus.controller.merchant;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.result.Result;
import com.campus.dto.merchant.DateRangeDTO;
import com.campus.dto.merchant.DishRankDTO;
import com.campus.service.SalesStatsService;
import com.campus.vo.merchant.DailySalesVO;
import com.campus.vo.merchant.DishRankVO;
import com.campus.vo.merchant.HourlySalesVO;
import com.campus.vo.merchant.SalesOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "商家模块 - 销售统计")
@RestController
@RequestMapping("/api/merchant/stats")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@RequireRole(UserRole.MERCHANT)
public class SalesStatsController {

    private final SalesStatsService salesStatsService;

    @Operation(summary = "首页统计数据")
    @GetMapping
    public Result<Map<String, Object>> getHomeStats(HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        return Result.success(salesStatsService.getHomeStats(merchantId));
    }

    @Operation(summary = "销售总览统计")
    @GetMapping("/sales/overview")
    public Result<SalesOverviewVO> getSalesOverview(DateRangeDTO dto, HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        return Result.success(salesStatsService.getSalesOverview(merchantId, dto));
    }

    @Operation(summary = "时段销售趋势", description = "查询指定日期各时段的销售额和订单数，默认今天")
    @GetMapping("/hourly")
    public Result<List<HourlySalesVO>> getHourlySales(
            @RequestParam(required = false) String date,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        return Result.success(salesStatsService.getHourlySales(merchantId, date));
    }

    @Operation(summary = "每日销售明细")
    @GetMapping("/daily/list")
    public Result<List<DailySalesVO>> getDailySalesList(DateRangeDTO dto, HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        return Result.success(salesStatsService.getDailySalesList(merchantId, dto));
    }

    @Operation(summary = "菜品销售排行")
    @GetMapping("/dish/rank")
    public Result<List<DishRankVO>> getDishRank(DishRankDTO dto, HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        return Result.success(salesStatsService.getDishRank(merchantId, dto));
    }
}
