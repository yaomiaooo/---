package com.example.hospital.service.impl;

import com.example.hospital.entity.Hospital;
import com.example.hospital.mapper.HospitalMapper;
import com.example.hospital.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 医院/院区服务实现类
 */
@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalMapper hospitalMapper;

    @Override
    public List<Hospital> getAllHospitals() {
        return hospitalMapper.selectList(null);
    }

    @Override
    public Hospital getHospitalById(String id) {
        return hospitalMapper.selectById(id);
    }
}

