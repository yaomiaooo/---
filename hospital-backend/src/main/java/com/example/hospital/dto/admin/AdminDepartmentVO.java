package com.example.hospital.dto.admin;

public class AdminDepartmentVO {
    private String departmentId;
    private String hospitalId;
    private String hospitalName;
    private String departmentName;
    private String departmentIntro;

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentIntro() {
        return departmentIntro;
    }

    public void setDepartmentIntro(String departmentIntro) {
        this.departmentIntro = departmentIntro;
    }

    public static AdminDepartmentVO ofOption(String departmentId, String departmentName) {
        AdminDepartmentVO vo = new AdminDepartmentVO();
        vo.setDepartmentId(departmentId);
        vo.setDepartmentName(departmentName);
        return vo;
    }
}
