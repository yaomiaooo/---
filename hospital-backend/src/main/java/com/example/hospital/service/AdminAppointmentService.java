package com.example.hospital.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.dto.admin.AdminAppointmentQueryRequest;
import com.example.hospital.dto.admin.AdminAppointmentVO;

import java.util.List;

public interface AdminAppointmentService {

    Page<AdminAppointmentVO> pageAppointments(AdminAppointmentQueryRequest req);

    AdminAppointmentVO getDetail(String appointmentId);

    void updateStatus(String appointmentId, String status);

    void batchUpdateStatus(List<String> appointmentIds, String status);
}

