package com.campus.controller.student;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.result.Result;
import com.campus.entity.Message;
import com.campus.entity.Orders;
import com.campus.mapper.OrdersMapper;
import com.campus.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "学生端-联系商家")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/student/messages")
@RequiredArgsConstructor
@RequireRole(UserRole.STUDENT)
public class StudentMessageController {

    private final MessageService messageService;
    private final OrdersMapper ordersMapper;

    @Operation(summary = "获取订单聊天记录")
    @GetMapping("/{orderId}")
    public Result<List<Message>> getMessages(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error(403, "无权查看该订单消息");
        }
        List<Message> messages = messageService.getMessageListByOrderId(orderId, order.getMerchantId());
        messageService.markReadByOrder(orderId, order.getMerchantId(), "user");
        return Result.success(messages);
    }

    @Operation(summary = "发送消息给商家")
    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestBody StudentSendMessageDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (dto.getOrderId() == null || dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return Result.error(400, "订单ID和消息内容不能为空");
        }

        Orders order = ordersMapper.selectById(dto.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作该订单");
        }

        Message message = new Message();
        message.setOrderId(dto.getOrderId());
        message.setSenderType("user");
        message.setSenderId(userId);
        message.setReceiverType("merchant");
        message.setReceiverId(order.getMerchantId());
        message.setContent(dto.getContent().trim());
        message.setStatus(1);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        messageService.save(message);
        return Result.success();
    }

    @Data
    public static class StudentSendMessageDTO {
        private Long orderId;
        private String content;
    }
}
