package com.example.hospital.dto;

import com.example.hospital.common.PassToken;
import com.example.hospital.common.Result;
import com.example.hospital.entity.Department;
import com.example.hospital.service.DepartmentService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户个人资料更新请求的数据传输对象
 */
@Data
public class UserProfileUpdateRequest {
    private String realName; // 真实姓名
    private String phone;    // 手机号码

    @RestController
    @RequestMapping("/api/hospital")
    @CrossOrigin // ★★★ 这一行就是允许前端连接后端的关键 ★★★
    public static class HospitalController {

        @Autowired
        private DepartmentService departmentService;

        // 1. 获取科室列表接口（支持按 hospital_id 查询和搜索）
        @PassToken
        @GetMapping("/departments")
        public Result<List<Department>> getDepartments(
                @RequestParam(required = false) String hospitalId,
                @RequestParam(required = false) String keyword) {
            System.out.println("前端正在请求科室列表... hospitalId: " + hospitalId + ", keyword: " + keyword);

            List<Department> departments;
            
            // 如果提供了搜索关键词，使用搜索方法
            if (keyword != null && !keyword.trim().isEmpty()) {
                departments = departmentService.searchDepartments(keyword, hospitalId);
            } else if (hospitalId != null && !hospitalId.isEmpty()) {
                // 按 hospital_id 查询
                departments = departmentService.getDepartmentsByHospitalId(hospitalId);
            } else {
                // 查询所有科室
                departments = departmentService.getAllDepartments();
            }

            return Result.success(departments);
        }

        // 2. 获取科室详情接口
        @PassToken
        @GetMapping("/departments/{departmentId}")
        public Result<Department> getDepartmentById(@PathVariable("departmentId") String departmentId) {
            System.out.println("前端正在请求科室详情... departmentId: " + departmentId);
            Department department = departmentService.getDepartmentById(departmentId);
            if (department == null) {
                System.out.println("未找到科室，departmentId: " + departmentId);
                return Result.error("科室不存在");
            }
            System.out.println("成功获取科室详情: " + department.getDepartmentName());
            return Result.success(department);
        }

        // 3. 模拟预约提交接口
        @PostMapping("/appointment")
        public Result<String> createAppointment(@RequestBody Map<String, Object> payload) {
            System.out.println("收到预约请求: " + payload);
            return Result.success("预约成功，请留意短信通知！");
        }
    }
}

