package com.example.hospital.dto.admin;

public class AdminScheduleQueryRequest {

    private String hospitalId;     // 必填：用于分片路由
    private String departmentId;   // 可选：通过 doctor.department_id 过滤
    private String doctorId;       // 可选
    private String workDateFrom;   // yyyy-MM-dd
    private String workDateTo;     // yyyy-MM-dd
    private String timeSlot;       // 可选

    private Integer page = 1;
    private Integer size = 10;

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getWorkDateFrom() { return workDateFrom; }
    public void setWorkDateFrom(String workDateFrom) { this.workDateFrom = workDateFrom; }

    public String getWorkDateTo() { return workDateTo; }
    public void setWorkDateTo(String workDateTo) { this.workDateTo = workDateTo; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
}
