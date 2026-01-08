package com.example.hospital.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.common.annotation.Log;
import com.example.hospital.dto.admin.AdminDepartmentCreateRequest;
import com.example.hospital.dto.admin.AdminDepartmentQueryRequest;
import com.example.hospital.dto.admin.AdminDepartmentUpdateRequest;
import com.example.hospital.dto.admin.AdminDepartmentVO;
import com.example.hospital.entity.Hospital;
import com.example.hospital.mapper.DepartmentMapper;
import com.example.hospital.service.AdminDepartmentService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/departments")
@UserLoginToken(roles = {"ADMIN"})
public class AdminDepartmentController {

    private final AdminDepartmentService adminDepartmentService;
    private final DepartmentMapper departmentMapper;

    public AdminDepartmentController(AdminDepartmentService adminDepartmentService,
                                     DepartmentMapper departmentMapper) {
        this.adminDepartmentService = adminDepartmentService;
        this.departmentMapper = departmentMapper;
    }

    /**
     * 院区列表
     */
    @GetMapping("/hospitals")
    public Result<List<Hospital>> listHospitals() {
        List<Hospital> list = adminDepartmentService.listHospitals();
        return Result.success(list);
    }

    /**
     * 科室下拉选项（供新增/编辑医生弹窗使用）
     * GET /api/admin/departments/options?hospitalId=xxx
     */
    @GetMapping("/options")
    public Result<List<AdminDepartmentVO>> options(@RequestParam String hospitalId) {
        if (!StringUtils.hasText(hospitalId)) {
            throw new RuntimeException("hospitalId不能为空");
        }
        List<AdminDepartmentVO> list = departmentMapper.selectOptionsByHospitalId(hospitalId.trim());
        return Result.success(list);
    }

    /**
     * 分页查询科室
     */
    @GetMapping
    public Result<IPage<AdminDepartmentVO>> page(AdminDepartmentQueryRequest req) {
        IPage<AdminDepartmentVO> page = adminDepartmentService.pageDepartments(req);
        return Result.success(page);
    }

    /**
     * 新增科室
     */
    @Log("新增科室")
    @PostMapping
    public Result<Boolean> create(@RequestBody AdminDepartmentCreateRequest req) {
        boolean ok = adminDepartmentService.createDepartment(req);
        return Result.success(ok);
    }

    /**
     * 修改科室
     */
    @Log("修改科室信息")
    @PutMapping("/{departmentId}")
    public Result<Boolean> update(@PathVariable String departmentId,
                                  @RequestBody AdminDepartmentUpdateRequest req) {
        req.setDepartmentId(departmentId);
        boolean ok = adminDepartmentService.updateDepartment(req);
        return Result.success(ok);
    }

    /**
     * 删除科室
     */
    @Log("删除科室")
    @DeleteMapping("/{departmentId}")
    public Result<Boolean> delete(@PathVariable String departmentId) {
        boolean ok = adminDepartmentService.deleteDepartment(departmentId);
        return Result.success(ok);
    }
}

