package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.hospital.dto.admin.*;
import com.example.hospital.entity.Department;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Schedule;
import com.example.hospital.mapper.DepartmentMapper;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.HospitalMapper;
import com.example.hospital.mapper.ScheduleMapper;
import com.example.hospital.service.AdminScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminScheduleServiceImpl implements AdminScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final HospitalMapper hospitalMapper;

    public AdminScheduleServiceImpl(
            ScheduleMapper scheduleMapper,
            DoctorMapper doctorMapper,
            DepartmentMapper departmentMapper,
            HospitalMapper hospitalMapper
    ) {
        this.scheduleMapper = scheduleMapper;
        this.doctorMapper = doctorMapper;
        this.departmentMapper = departmentMapper;
        this.hospitalMapper = hospitalMapper;
    }

    @Override
    public IPage<AdminScheduleVO> pageSchedules(AdminScheduleQueryRequest req) {
        // 如果没有 hospitalId，返回空结果（避免广播查询所有分片）
        if (!StringUtils.hasText(req.getHospitalId())) {
            Page<AdminScheduleVO> emptyPage = new Page<>(req.getPage() != null ? req.getPage() : 1, 
                                                         req.getSize() != null ? req.getSize() : 10);
            emptyPage.setTotal(0);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }

        // 确保分页参数有效
        int pageNum = (req.getPage() != null && req.getPage() > 0) ? req.getPage() : 1;
        int pageSize = (req.getSize() != null && req.getSize() > 0) ? req.getSize() : 10;
        Page<Schedule> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Schedule> qw = new LambdaQueryWrapper<>();
        qw.eq(Schedule::getHospitalId, req.getHospitalId());

        if (StringUtils.hasText(req.getDoctorId())) {
            qw.eq(Schedule::getDoctorId, req.getDoctorId());
        }
        if (StringUtils.hasText(req.getTimeSlot())) {
            qw.eq(Schedule::getTimeSlot, req.getTimeSlot());
        }
        if (StringUtils.hasText(req.getWorkDateFrom())) {
            try {
                qw.ge(Schedule::getWorkDate, LocalDate.parse(req.getWorkDateFrom()));
            } catch (Exception e) {
                System.err.println("日期解析失败 (workDateFrom): " + req.getWorkDateFrom() + ", 错误: " + e.getMessage());
                // 日期格式错误时忽略该条件
            }
        }
        if (StringUtils.hasText(req.getWorkDateTo())) {
            try {
                qw.le(Schedule::getWorkDate, LocalDate.parse(req.getWorkDateTo()));
            } catch (Exception e) {
                System.err.println("日期解析失败 (workDateTo): " + req.getWorkDateTo() + ", 错误: " + e.getMessage());
                // 日期格式错误时忽略该条件
            }
        }
        qw.orderByDesc(Schedule::getWorkDate).orderByAsc(Schedule::getTimeSlot);

        IPage<Schedule> entityPage = scheduleMapper.selectPage(page, qw);

        String hospitalId = req.getHospitalId();
        Hospital hospital = hospitalMapper.selectById(hospitalId);

        List<String> doctorIds = entityPage.getRecords().stream()
                .map(Schedule::getDoctorId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Doctor> doctorMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            List<Doctor> docs = doctorMapper.selectList(new LambdaQueryWrapper<Doctor>()
                    .eq(Doctor::getHospitalId, hospitalId)
                    .in(Doctor::getDoctorId, doctorIds));
            doctorMap = docs.stream().collect(Collectors.toMap(Doctor::getDoctorId, d -> d));
        }

        final Set<String> allowedDoctorIds = StringUtils.hasText(req.getDepartmentId())
                ? doctorMap.values().stream()
                .filter(d -> Objects.equals(req.getDepartmentId(), d.getDepartmentId()))
                .map(Doctor::getDoctorId)
                .collect(Collectors.toSet())
                : null;

        Set<String> deptIds = doctorMap.values().stream()
                .map(Doctor::getDepartmentId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, Department> deptMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            List<Department> deps = departmentMapper.selectList(new LambdaQueryWrapper<Department>()
                    .eq(Department::getHospitalId, hospitalId)
                    .in(Department::getDepartmentId, deptIds));
            deptMap = deps.stream().collect(Collectors.toMap(Department::getDepartmentId, d -> d));
        }

        List<Schedule> filtered = entityPage.getRecords();
        if (allowedDoctorIds != null) {
            filtered = filtered.stream()
                    .filter(s -> allowedDoctorIds.contains(s.getDoctorId()))
                    .collect(Collectors.toList());
        }

        Page<AdminScheduleVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<AdminScheduleVO> voRecords = new ArrayList<>();

        for (Schedule s : filtered) {
            AdminScheduleVO vo = new AdminScheduleVO();
            vo.setScheduleId(s.getScheduleId());
            vo.setHospitalId(s.getHospitalId());
            vo.setHospitalName(hospital == null ? null : hospital.getHospitalName());

            Doctor doc = doctorMap.get(s.getDoctorId());
            vo.setDoctorId(s.getDoctorId());
            vo.setDoctorName(doc == null ? null : doc.getDoctorName());

            String deptId = (doc == null ? null : doc.getDepartmentId());
            vo.setDepartmentId(deptId);
            Department dep = (deptId == null ? null : deptMap.get(deptId));
            vo.setDepartmentName(dep == null ? null : dep.getDepartmentName());

            vo.setWorkDate(s.getWorkDate() == null ? null : s.getWorkDate().toString());
            vo.setTimeSlot(s.getTimeSlot());
            vo.setTotalQuota(s.getTotalQuota());
            vo.setBookedCount(s.getBookedCount());
            int remain = (s.getTotalQuota() == null ? 0 : s.getTotalQuota()) - (s.getBookedCount() == null ? 0 : s.getBookedCount());
            vo.setRemainingQuota(Math.max(remain, 0));

            voRecords.add(vo);
        }

        voPage.setRecords(voRecords);
        return voPage;
    }

    @Override
    public void createSchedule(AdminScheduleCreateRequest req) {
        if (!StringUtils.hasText(req.getHospitalId())) throw new RuntimeException("hospitalId 必填");
        if (!StringUtils.hasText(req.getDoctorId())) throw new RuntimeException("doctorId 必填");
        if (!StringUtils.hasText(req.getWorkDate())) throw new RuntimeException("workDate 必填");
        if (!StringUtils.hasText(req.getTimeSlot())) throw new RuntimeException("timeSlot 必填");
        if (req.getTotalQuota() == null || req.getTotalQuota() < 0) throw new RuntimeException("totalQuota 必须为非负数");

        Doctor doc = doctorMapper.selectById(req.getDoctorId());
        if (doc == null) throw new RuntimeException("医生不存在");
        if (!Objects.equals(req.getHospitalId(), doc.getHospitalId())) {
            throw new RuntimeException("医生不属于该院区，无法创建排班");
        }

        Schedule s = new Schedule();
        s.setScheduleId(String.valueOf(IdWorker.getId()));
        s.setHospitalId(req.getHospitalId());
        s.setDoctorId(req.getDoctorId());
        s.setWorkDate(LocalDate.parse(req.getWorkDate()));
        s.setTimeSlot(req.getTimeSlot());
        s.setTotalQuota(req.getTotalQuota());
        s.setBookedCount(0);

        scheduleMapper.insert(s);
    }

    @Override
    public void updateSchedule(String scheduleId, AdminScheduleUpdateRequest req) {
        if (!StringUtils.hasText(scheduleId)) throw new RuntimeException("scheduleId 必填");

        Schedule exist = scheduleMapper.selectById(scheduleId);
        if (exist == null) throw new RuntimeException("排班不存在");

        if (!StringUtils.hasText(req.getDoctorId())) throw new RuntimeException("doctorId 必填");
        if (!StringUtils.hasText(req.getWorkDate())) throw new RuntimeException("workDate 必填");
        if (!StringUtils.hasText(req.getTimeSlot())) throw new RuntimeException("timeSlot 必填");
        if (req.getTotalQuota() == null || req.getTotalQuota() < 0) throw new RuntimeException("totalQuota 必须为非负数");

        Doctor doc = doctorMapper.selectById(req.getDoctorId());
        if (doc == null) throw new RuntimeException("医生不存在");
        if (!Objects.equals(exist.getHospitalId(), doc.getHospitalId())) {
            throw new RuntimeException("不允许跨院区更换医生（会导致分片键变更）");
        }

        int booked = exist.getBookedCount() == null ? 0 : exist.getBookedCount();
        if (req.getTotalQuota() < booked) {
            throw new RuntimeException("总号源不能小于已预约数：" + booked);
        }

        LambdaUpdateWrapper<Schedule> uw = new LambdaUpdateWrapper<>();
        uw.eq(Schedule::getScheduleId, scheduleId);
        uw.eq(Schedule::getHospitalId, exist.getHospitalId());

        uw.set(Schedule::getDoctorId, req.getDoctorId());
        uw.set(Schedule::getWorkDate, LocalDate.parse(req.getWorkDate()));
        uw.set(Schedule::getTimeSlot, req.getTimeSlot());
        uw.set(Schedule::getTotalQuota, req.getTotalQuota());

        scheduleMapper.update(null, uw);
    }

    @Override
    public void deleteSchedule(String scheduleId) {
        if (!StringUtils.hasText(scheduleId)) throw new RuntimeException("scheduleId 必填");

        Schedule exist = scheduleMapper.selectById(scheduleId);
        if (exist == null) return;

        LambdaQueryWrapper<Schedule> qw = new LambdaQueryWrapper<>();
        qw.eq(Schedule::getScheduleId, scheduleId);
        qw.eq(Schedule::getHospitalId, exist.getHospitalId());
        scheduleMapper.delete(qw);
    }

    @Override
    public List<Hospital> listHospitals() {
        return hospitalMapper.selectList(null);
    }

    @Override
    public List<Department> listDepartments(String hospitalId) {
        if (!StringUtils.hasText(hospitalId)) throw new RuntimeException("hospitalId 必填");
        return departmentMapper.selectList(new LambdaQueryWrapper<Department>()
                .eq(Department::getHospitalId, hospitalId));
    }

    @Override
    public List<Doctor> listDoctors(String hospitalId, String departmentId) {
        if (!StringUtils.hasText(hospitalId)) throw new RuntimeException("hospitalId 必填");

        LambdaQueryWrapper<Doctor> qw = new LambdaQueryWrapper<>();
        qw.eq(Doctor::getHospitalId, hospitalId);
        if (StringUtils.hasText(departmentId)) {
            qw.eq(Doctor::getDepartmentId, departmentId);
        }
        qw.orderByAsc(Doctor::getDoctorName);
        return doctorMapper.selectList(qw);
    }
}

