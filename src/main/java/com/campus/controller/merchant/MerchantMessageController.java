package com.campus.controller.merchant;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.result.Result;
import com.campus.dto.merchant.ReplyMessageDTO;
import com.campus.entity.Message;
import com.campus.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商家模块 - 消息管理", description = "商家查看聊天记录、回复学生消息、标记已读")
@RestController
@RequestMapping("/api/merchant/message")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@RequireRole(UserRole.MERCHANT)
public class MerchantMessageController {

    private final MessageService messageService;

    @Operation(summary = "获取未读消息总数", description = "商家首页统计用，返回所有未读消息数量")
    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        long count = messageService.getUnreadCount(merchantId);
        return Result.success(count);
    }

    @Operation(summary = "获取指定订单的未读消息列表", description = "用于订单列表显示红点提醒，传入订单ID列表")
    @GetMapping("/unread/list")
    public Result<List<Message>> getUnreadMessagesByOrderIds(
            @Parameter(description = "订单ID列表，逗号分隔") @RequestParam String orderIds,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        List<Long> idList = java.util.Arrays.stream(orderIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
        List<Message> list = messageService.getUnreadMessagesByOrderIds(idList, merchantId);
        return Result.success(list);
    }

    @Operation(summary = "获取订单聊天记录", description = "查询指定订单下的全部聊天消息（商家与学生之间）")
    @GetMapping("/list/{orderId}")
    public Result<List<Message>> getMessageList(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        List<Message> list = messageService.getMessageListByOrderId(orderId, merchantId);
        return Result.success(list);
    }

    @Operation(summary = "标记订单消息为已读", description = "商家打开聊天窗口后，将该订单下用户发来的未读消息标记为已读")
    @PutMapping("/{orderId}/read")
    public Result<String> markAsRead(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        messageService.markReadByOrder(orderId, merchantId, "merchant");
        return Result.success("已标记已读");
    }

    @Operation(summary = "商家回复学生消息", description = "商家向指定订单的学生用户发送消息")
    @PostMapping("/reply")
    public Result<String> replyMessage(
            @Valid @RequestBody ReplyMessageDTO dto,
            HttpServletRequest request) {
        Long merchantId = Long.parseLong(request.getAttribute("userId").toString());
        messageService.replyMessage(dto, merchantId);
        return Result.success("回复成功");
    }
}
