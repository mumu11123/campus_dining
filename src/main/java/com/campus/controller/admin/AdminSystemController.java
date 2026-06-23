package com.campus.controller.admin;

import com.campus.common.auth.RequireRole;
import com.campus.common.auth.UserRole;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "管理员端-系统维护")
@RestController
@RequestMapping("/api/admin/system")
@RequireRole(UserRole.ADMIN)
public class AdminSystemController {

    private static final Map<String, SystemParamVO> PARAMS = new LinkedHashMap<>();
    private static final List<OperationLogVO> LOGS = new ArrayList<>();
    private static final List<BackupRecordVO> BACKUPS = new ArrayList<>();

    static {
        resetParams();
        LOGS.add(new OperationLogVO("SYSTEM", "系统启动", "初始化系统参数", LocalDateTime.now()));
    }

    @Operation(summary = "查询系统参数")
    @GetMapping("/params")
    public Result<List<SystemParamVO>> params(HttpServletRequest request) {
        requireAdmin(request);
        return Result.success(new ArrayList<>(PARAMS.values()));
    }

    @Operation(summary = "修改系统参数")
    @PutMapping("/params/{code}")
    public Result<String> updateParam(@PathVariable String code,
                                      @RequestBody SystemParamUpdateDTO dto,
                                      HttpServletRequest request) {
        requireAdmin(request);
        SystemParamVO param = PARAMS.get(code);
        if (param == null) {
            throw new BusinessException(404, "系统参数不存在");
        }
        if (dto.getValue() == null || dto.getValue().trim().isEmpty()) {
            throw new BusinessException(400, "参数值不能为空");
        }
        String oldValue = param.getValue();
        param.setValue(dto.getValue().trim());
        addLog(request, "修改系统参数", param.getName() + "：" + oldValue + " -> " + param.getValue());
        return Result.success("参数配置成功");
    }

    @Operation(summary = "恢复默认系统参数")
    @PostMapping("/params/reset")
    public Result<String> reset(HttpServletRequest request) {
        requireAdmin(request);
        resetParams();
        addLog(request, "恢复默认参数", "系统参数已恢复默认值");
        return Result.success("恢复默认参数成功");
    }

    @Operation(summary = "查询操作日志")
    @GetMapping("/logs")
    public Result<List<OperationLogVO>> logs(HttpServletRequest request) {
        requireAdmin(request);
        List<OperationLogVO> logs = new ArrayList<>(LOGS);
        Collections.reverse(logs);
        return Result.success(logs);
    }

    @Operation(summary = "手动备份数据")
    @PostMapping("/backup")
    public Result<BackupRecordVO> backup(HttpServletRequest request) {
        requireAdmin(request);
        String filename = "campus-dining-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".sql";
        BackupRecordVO record = new BackupRecordVO(filename, "手动备份", "已完成", LocalDateTime.now());
        BACKUPS.add(record);
        addLog(request, "手动备份数据", filename);
        return Result.success("备份任务已完成", record);
    }

    @Operation(summary = "查询备份记录")
    @GetMapping("/backups")
    public Result<List<BackupRecordVO>> backups(HttpServletRequest request) {
        requireAdmin(request);
        List<BackupRecordVO> backups = new ArrayList<>(BACKUPS);
        Collections.reverse(backups);
        return Result.success(backups);
    }

    @Operation(summary = "模拟恢复备份")
    @PostMapping("/restore")
    public Result<String> restore(@RequestParam String filename, HttpServletRequest request) {
        requireAdmin(request);
        boolean exists = BACKUPS.stream().anyMatch(item -> item.getFilename().equals(filename));
        if (!exists) {
            throw new BusinessException(404, "备份文件不存在");
        }
        addLog(request, "恢复备份数据", filename);
        return Result.success("恢复任务已记录，请验证业务数据");
    }

    private static void resetParams() {
        PARAMS.clear();
        PARAMS.put("order_timeout_minutes", new SystemParamVO("order_timeout_minutes", "订单超时时间", "15", "订单", "分钟，建议5-60"));
        PARAMS.put("max_cancel_minutes", new SystemParamVO("max_cancel_minutes", "可取消时间", "5", "订单", "分钟，超过后需联系商家"));
        PARAMS.put("merchant_auto_open", new SystemParamVO("merchant_auto_open", "审核通过默认营业", "true", "商家", "true/false"));
        PARAMS.put("student_message_enabled", new SystemParamVO("student_message_enabled", "联系商家开关", "true", "学生", "true/false"));
    }

    private void addLog(HttpServletRequest request, String action, String detail) {
        String operator = String.valueOf(request.getAttribute("userId"));
        LOGS.add(new OperationLogVO(operator, action, detail, LocalDateTime.now()));
    }

    private void requireAdmin(HttpServletRequest request) {
        String role = String.valueOf(request.getAttribute("role"));
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new BusinessException(403, "无管理员权限");
        }
    }

    @Data
    public static class SystemParamUpdateDTO {
        private String value;
    }

    @Data
    public static class SystemParamVO {
        private final String code;
        private final String name;
        private String value;
        private final String category;
        private final String remark;

        public SystemParamVO(String code, String name, String value, String category, String remark) {
            this.code = code;
            this.name = name;
            this.value = value;
            this.category = category;
            this.remark = remark;
        }
    }

    @Data
    public static class OperationLogVO {
        private final String operator;
        private final String action;
        private final String detail;
        private final LocalDateTime createdAt;
    }

    @Data
    public static class BackupRecordVO {
        private final String filename;
        private final String type;
        private final String status;
        private final LocalDateTime createdAt;
    }
}
