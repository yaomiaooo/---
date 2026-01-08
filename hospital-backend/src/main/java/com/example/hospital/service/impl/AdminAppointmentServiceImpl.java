package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.dto.admin.AdminAppointmentQueryRequest;
import com.example.hospital.dto.admin.AdminAppointmentVO;
import com.example.hospital.entity.*;
import com.example.hospital.mapper.*;
import com.example.hospital.service.AdminAppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAppointmentServiceImpl implements AdminAppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final UserMapper userMapper;
    private final PatientMapper patientMapper;
    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final HospitalMapper hospitalMapper;

    @Override
    public Page<AdminAppointmentVO> pageAppointments(AdminAppointmentQueryRequest req) {
        long pageNo = Math.max(1, req.getPage());
        long size = Math.max(1, req.getSize());
        Page<Appointment> page = new Page<>(pageNo, size);

        QueryWrapper<Appointment> qw = new QueryWrapper<>();

        // 1) status 精确过滤
        if (StringUtils.isNotBlank(req.getStatus())) {
            qw.eq("status", req.getStatus());
        }

        // 2) hospitalId 精确过滤（关键：兼容 A/B，避免 "For input string: A"）
        String normalizedHospitalId = normalizeHospitalId(req.getHospitalId());
        if (StringUtils.isNotBlank(normalizedHospitalId)) {
            qw.eq("hospital_id", normalizedHospitalId);
        }

        // 半连接过滤
        boolean needSemiJoin =
                StringUtils.isNotBlank(req.getDepartmentId())
                        || StringUtils.isNotBlank(req.getDoctorId())
                        || StringUtils.isNotBlank(req.getScheduleId());

        if (needSemiJoin) {
            // 半连接需要精准路由，否则分库分表场景可能触发广播
            if (StringUtils.isBlank(normalizedHospitalId)) {
                throw new RuntimeException("按科室/医生/排班筛选预约订单时，必须要填写 hospitalId ");
            }

            Set<String> scheduleIds = resolveScheduleIdsForAppointmentFilter(
                    normalizedHospitalId,
                    req.getDepartmentId(),
                    req.getDoctorId(),
                    req.getScheduleId()
            );

            // 半连接结果为空时直接返回空页，避免 appointment 全表扫描
            if (scheduleIds.isEmpty()) {
                Page<AdminAppointmentVO> empty = new Page<>(pageNo, size);
                empty.setTotal(0);
                empty.setRecords(Collections.emptyList());
                return empty;
            }

            // schedule_id IN (...)：避免 SQL 过长
            applyInBatches(qw, "schedule_id", new ArrayList<>(scheduleIds), 800);
        }
        // 半连接结束

        // 3) 时间段过滤：按 created_at
        LocalDateTime start = req.getStartTime();
        LocalDateTime end = req.getEndTime();
        if (start != null) {
            qw.ge("created_at", start);
        }
        if (end != null) {
            qw.le("created_at", end);
        }

        // 4) keyword：手机号 / 就诊人姓名 / 订单号 模糊匹配
        String keyword = req.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            List<String> matchedUserIds = userMapper.selectList(
                    new QueryWrapper<User>().like("user_phone", keyword)
            ).stream().map(User::getUserId).toList();

            List<String> matchedPatientIds = patientMapper.selectList(
                    new QueryWrapper<Patient>().like("patient_name", keyword)
            ).stream().map(Patient::getPatientId).toList();

            qw.and(w -> {
                w.like("appointment_id", keyword);
                if (!matchedUserIds.isEmpty()) {
                    w.or().in("user_id", matchedUserIds);
                }
                if (!matchedPatientIds.isEmpty()) {
                    w.or().in("patient_id", matchedPatientIds);
                }
            });
        }

        // 默认按创建时间倒序
        qw.orderByDesc("created_at");

        Page<Appointment> apPage = appointmentMapper.selectPage(page, qw);
        List<Appointment> records = apPage.getRecords();
        if (records == null || records.isEmpty()) {
            Page<AdminAppointmentVO> empty = new Page<>(pageNo, size);
            empty.setTotal(apPage.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        // 批量拉取关联数据（避免 N+1）
        Set<String> userIds = records.stream().map(Appointment::getUserId).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        Set<String> patientIds = records.stream().map(Appointment::getPatientId).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        Set<String> scheduleIds = records.stream().map(Appointment::getScheduleId).filter(StringUtils::isNotBlank).collect(Collectors.toSet());

        Map<String, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity(), (a, b) -> a));

        Map<String, Patient> patientMap = patientIds.isEmpty()
                ? Collections.emptyMap()
                : patientMapper.selectBatchIds(patientIds).stream()
                .collect(Collectors.toMap(Patient::getPatientId, Function.identity(), (a, b) -> a));

        Map<String, Schedule> scheduleMap = scheduleIds.isEmpty()
                ? Collections.emptyMap()
                : scheduleMapper.selectBatchIds(scheduleIds).stream()
                .collect(Collectors.toMap(Schedule::getScheduleId, Function.identity(), (a, b) -> a));

        // schedule -> doctor / department / hospital
        Set<String> doctorIds = scheduleMap.values().stream()
                .map(Schedule::getDoctorId).filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        Set<String> deptIds = new HashSet<>();
        Set<String> hospitalIds = new HashSet<>();
        hospitalIds.addAll(records.stream().map(Appointment::getHospitalId).filter(StringUtils::isNotBlank).collect(Collectors.toSet()));

        Map<String, Doctor> doctorMap = doctorIds.isEmpty()
                ? Collections.emptyMap()
                : doctorMapper.selectBatchIds(doctorIds).stream()
                .collect(Collectors.toMap(Doctor::getDoctorId, Function.identity(), (a, b) -> a));

        for (Doctor d : doctorMap.values()) {
            if (d != null && StringUtils.isNotBlank(d.getDepartmentId())) {
                deptIds.add(d.getDepartmentId());
            }
            if (d != null && StringUtils.isNotBlank(d.getHospitalId())) {
                hospitalIds.add(d.getHospitalId());
            }
        }

        Map<String, Department> deptMap = deptIds.isEmpty()
                ? Collections.emptyMap()
                : departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Department::getDepartmentId, Function.identity(), (a, b) -> a));

        Map<String, Hospital> hospitalMap = hospitalIds.isEmpty()
                ? Collections.emptyMap()
                : hospitalMapper.selectBatchIds(hospitalIds).stream()
                .collect(Collectors.toMap(Hospital::getHospitalId, Function.identity(), (a, b) -> a));

        // 组装 VO
        List<AdminAppointmentVO> voList = new ArrayList<>(records.size());
        for (Appointment ap : records) {
            AdminAppointmentVO vo = new AdminAppointmentVO();
            vo.setAppointmentId(ap.getAppointmentId());
            vo.setStatus(ap.getStatus());

            vo.setCreatedAt(ap.getCreatedAt());
            vo.setHospitalId(ap.getHospitalId());
            Hospital hos = hospitalMap.get(ap.getHospitalId());
            vo.setHospitalName(hos != null ? hos.getHospitalName() : null);

            // 用户
            vo.setUserId(ap.getUserId());
            User u = userMap.get(ap.getUserId());
            vo.setUserPhone(u != null ? u.getUserPhone() : null);

            // 就诊人
            vo.setPatientId(ap.getPatientId());
            Patient p = patientMap.get(ap.getPatientId());
            vo.setPatientName(p != null ? p.getPatientName() : null);

            // 排班/医生/科室
            vo.setScheduleId(ap.getScheduleId());
            Schedule s = scheduleMap.get(ap.getScheduleId());
            if (s != null) {
                vo.setWorkDate(s.getWorkDate());
                vo.setTimeSlot(s.getTimeSlot());

                vo.setDoctorId(s.getDoctorId());
                Doctor d = doctorMap.get(s.getDoctorId());
                if (d != null) {
                    vo.setDoctorName(d.getDoctorName());
                    vo.setDepartmentId(d.getDepartmentId());
                    Department dep = deptMap.get(d.getDepartmentId());
                    vo.setDepartmentName(dep != null ? dep.getDepartmentName() : null);
                }
            }

            voList.add(vo);
        }

        Page<AdminAppointmentVO> result = new Page<>(pageNo, size);
        result.setTotal(apPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public AdminAppointmentVO getDetail(String appointmentId) {
        Appointment ap = appointmentMapper.selectById(appointmentId);
        if (ap == null) return null;

        // 复用分页组装逻辑（这里简单做一次 "单条" 组装）
        AdminAppointmentQueryRequest req = new AdminAppointmentQueryRequest();
        req.setPage(1);
        req.setSize(1);
        req.setHospitalId(ap.getHospitalId());
        req.setScheduleId(ap.getScheduleId());
        Page<AdminAppointmentVO> page = pageAppointments(req);
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    @Override
    @Transactional
    public void updateStatus(String appointmentId, String status) {
        appointmentMapper.update(null, new LambdaUpdateWrapper<Appointment>()
                .eq(Appointment::getAppointmentId, appointmentId)
                .set(Appointment::getStatus, status));
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<String> appointmentIds, String status) {
        if (appointmentIds == null || appointmentIds.isEmpty()) return;
        appointmentMapper.update(null, new LambdaUpdateWrapper<Appointment>()
                .in(Appointment::getAppointmentId, appointmentIds)
                .set(Appointment::getStatus, status));
    }

    /**
     * 半连接：根据 departmentId/doctorId/scheduleId 解析出用于过滤 appointment 的 scheduleIds
     * 优先级：scheduleId > doctorId > departmentId
     * 此处强制带 hospitalId ，避免分片环境广播。
     */
    private Set<String> resolveScheduleIdsForAppointmentFilter(
            String hospitalId,
            String departmentId,
            String doctorId,
            String scheduleId
    ) {
        // 1) scheduleId 优先
        if (StringUtils.isNotBlank(scheduleId)) {
            return Set.of(scheduleId.trim());
        }

        Set<String> doctorIds = new HashSet<>();

        // 2) doctorId 直接加入
        if (StringUtils.isNotBlank(doctorId)) {
            doctorIds.add(doctorId.trim());
        }

        // 3) departmentId -> doctorIds
        if (StringUtils.isNotBlank(departmentId)) {
            List<Doctor> docs = doctorMapper.selectList(new QueryWrapper<Doctor>()
                    .eq("hospital_id", hospitalId)
                    .eq("department_id", departmentId.trim())
                    .select("doctor_id"));

            for (Doctor d : docs) {
                if (d != null && StringUtils.isNotBlank(d.getDoctorId())) {
                    doctorIds.add(d.getDoctorId());
                }
            }
        }

        if (doctorIds.isEmpty()) {
            return Collections.emptySet();
        }

        // 4) doctorIds -> scheduleIds
        List<Schedule> schedules = scheduleMapper.selectList(new QueryWrapper<Schedule>()
                .eq("hospital_id", hospitalId)
                .in("doctor_id", doctorIds)
                .select("schedule_id"));

        Set<String> scheduleIds = new HashSet<>();
        for (Schedule s : schedules) {
            if (s != null && StringUtils.isNotBlank(s.getScheduleId())) {
                scheduleIds.add(s.getScheduleId());
            }
        }
        return scheduleIds;
    }

    /**
     * 大 IN 列表分批拼接：
     * WHERE (col IN (b1) OR col IN (b2) OR ...)
     */
    private void applyInBatches(QueryWrapper<?> qw, String column, List<String> ids, int batchSize) {
        if (ids == null || ids.isEmpty()) return;

        List<String> clean = ids.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .toList();

        if (clean.isEmpty()) return;

        if (clean.size() <= batchSize) {
            qw.in(column, clean);
            return;
        }

        qw.and(w -> {
            int i = 0;
            while (i < clean.size()) {
                int end = Math.min(i + batchSize, clean.size());
                List<String> part = clean.subList(i, end);
                w.or().in(column, part);
                i = end;
            }
        });
    }

    private String normalizeHospitalId(String raw) {
        if (StringUtils.isBlank(raw)) return null;

        String v = raw.trim();
        if ("1".equals(v) || "2".equals(v)) return v;

        // 兼容你现在的 "A/B" 传参
        if ("A".equalsIgnoreCase(v)) return "1";
        if ("B".equalsIgnoreCase(v)) return "2";

        // 兼容可能的中文（可选）
        if (v.contains("朝晖")) return "1";
        if (v.contains("屏峰")) return "2";

        // 非法值：不做过滤，避免 "For input string"
        return null;
    }
}

