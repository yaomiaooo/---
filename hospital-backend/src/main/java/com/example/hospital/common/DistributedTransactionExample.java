package com.example.hospital.common;

import com.example.hospital.entity.Appointment;
import com.example.hospital.entity.Visit;
import com.example.hospital.mapper.AppointmentMapper;
import com.example.hospital.mapper.VisitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 分布式事务示例
 * 
 * 在分库分表场景中，如果操作涉及多个分片（多个数据库），需要使用分布式事务
 * 来保证数据一致性。
 * 
 * 示例场景：
 * 1. 创建预约（appointment 表，按 hospital_id 分片）
 * 2. 创建就诊记录（visit 表，按 hospital_id 分片）
 * 
 * 如果这两个操作在不同的分片上，需要分布式事务来保证：
 * - 要么全部成功
 * - 要么全部回滚
 */
@Service
@RequiredArgsConstructor
public class DistributedTransactionExample {

    private final AppointmentMapper appointmentMapper;
    private final VisitMapper visitMapper;

    /**
     * 示例1：跨分片操作 - 创建预约并创建就诊记录
     * 
     * 注意：在 ShardingSphere 中，如果两个操作的分片键（hospitalId）相同，
     * 它们会在同一个分片上执行，使用本地事务即可。
     * 
     * 但如果 hospitalId 不同，或者需要保证跨分片的一致性，需要使用分布式事务。
     * 
     * @param appointment 预约记录
     * @param visit 就诊记录
     * @param hospitalId 院区ID（分片键）
     */
    @Transactional(rollbackFor = Exception.class)
    public void createAppointmentWithVisit(Appointment appointment, Visit visit, String hospitalId) {
        // 1. 创建预约（会在对应的分片上执行）
        appointment.setHospitalId(hospitalId);
        appointmentMapper.insert(appointment);
        
        // 2. 创建就诊记录（如果 hospitalId 相同，会在同一个分片上执行）
        // 如果 hospitalId 不同，ShardingSphere 会自动使用分布式事务
        visit.setHospitalId(hospitalId);
        visitMapper.insert(visit);
        
        // 如果任何一步失败，整个事务会回滚
    }

    /**
     * 示例2：跨分片更新操作
     * 
     * 场景：更新预约状态，同时更新就诊记录
     * 如果这两个记录在不同的分片上，需要分布式事务
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAppointmentAndVisit(String appointmentId, String visitId, 
                                         String appointmentHospitalId, String visitHospitalId) {
        // 更新预约（在 appointmentHospitalId 对应的分片上）
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment != null) {
            appointment.setStatus("COMPLETED");
            appointmentMapper.updateById(appointment);
        }
        
        // 更新就诊记录（在 visitHospitalId 对应的分片上）
        // 如果 appointmentHospitalId != visitHospitalId，会使用分布式事务
        Visit visit = visitMapper.selectById(visitId);
        if (visit != null) {
            visit.setDiagnosis("诊断完成");
            visitMapper.updateById(visit);
        }
    }

    /**
     * 示例3：批量操作跨分片
     * 
     * 场景：批量创建预约，可能涉及多个分片
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateAppointments(java.util.List<Appointment> appointments) {
        for (Appointment appointment : appointments) {
            // 每个 appointment 可能在不同的分片上
            appointmentMapper.insert(appointment);
        }
        // 如果任何一个插入失败，所有操作都会回滚
    }
}

