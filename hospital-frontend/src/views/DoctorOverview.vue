<template>
  <div>
    <div class="stats-row">
      <div class="stat-card blue-gradient">
        <div class="stat-info">
          <h3>待接诊</h3>
          <span class="num">{{ stats.waiting }}</span>
        </div>
        <Icon icon="mdi:account-clock" class="bg-icon" />
      </div>

      <div class="stat-card green-gradient">
        <div class="stat-info">
          <h3>已完成</h3>
          <span class="num">{{ stats.completed }}</span>
        </div>
        <Icon icon="mdi:account-check" class="bg-icon" />
      </div>

      <div class="stat-card orange-gradient">
        <div class="stat-info">
          <h3>今日挂号</h3>
          <span class="num">{{ stats.total }}</span>
        </div>
        <Icon icon="mdi:calendar-today" class="bg-icon" />
      </div>

      <div class="stat-card purple-gradient">
        <div class="stat-info">
          <h3>完成率</h3>
          <span class="num">{{ completionRate }}%</span>
        </div>
        <Icon icon="mdi:chart-pie" class="bg-icon" />
      </div>
    </div>

    <div class="panel-container">
      <div class="panel-header">
        <h3>候诊队列 <small>Patient Queue</small></h3>
        <div class="actions">
          <button class="btn-refresh" @click="refresh"><Icon icon="mdi:refresh" /> 刷新</button>
        </div>
      </div>

      <div class="table-wrapper">
        <table>
          <thead>
          <tr>
            <th>预约编号</th>
            <th>姓名</th>
            <th>性别</th>
            <th>年龄</th>
            <th>挂号类型</th>
            <th>预约时间段</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="appointment in appointments" :key="appointment.appointmentId">
            <td class="id-col">{{ appointment.appointmentId }}</td>
            <td class="name-col">{{ getPatientName(appointment) }}</td>
            <td>{{ getPatientGender(appointment) }}</td>
            <td>{{ getPatientAge(appointment) }}</td>
            <td>
              <span class="type-badge" :class="getAppointmentTypeClass(appointment)">{{ getAppointmentType(appointment) }}</span>
            </td>
            <td><span class="badge">{{ getTimeSlot(appointment) }}</span></td>
            <td>
              <span class="status-badge" :class="getStatusClass(appointment.status)">
                {{ getStatusText(appointment.status) }}
              </span>
            </td>
            <td>
              <button v-if="appointment.status === 'BOOKED'"
                      class="btn-action primary"
                      @click="startConsultationHandler(appointment)">
                接诊
              </button>
              <span v-if="appointment.status === 'COMPLETED'" class="completed-text">已完成</span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 接诊诊断弹窗 -->
    <div v-if="showConsultationModal" class="modal-overlay" @click="hideConsultationModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>患者诊断</h3>
          <button class="close-btn" @click="hideConsultationModal">×</button>
        </div>
        <div class="modal-body">
          <div class="patient-info">
            <h4>患者信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <label>姓名：</label>
                <span>{{ currentConsultation?.patientName }}</span>
              </div>
              <div class="info-item">
                <label>性别：</label>
                <span>{{ currentConsultation?.patientGender }}</span>
              </div>
              <div class="info-item">
                <label>年龄：</label>
                <span>{{ currentConsultation?.patientAge }}</span>
              </div>
              <div class="info-item">
                <label>预约时间段：</label>
                <span class="badge">{{ currentConsultation?.timeSlot }}</span>
              </div>
            </div>
          </div>

          <div class="consultation-info">
            <h4>就诊信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <label>就诊时间：</label>
                <span>{{ currentConsultation?.visitTime }}</span>
              </div>
            </div>
          </div>

          <div class="diagnosis-section">
            <h4>诊断结果</h4>
            <textarea
              :key="showConsultationModal"
              v-model="diagnosis"
              placeholder="请输入诊断结果..."
              rows="6"
              class="diagnosis-input"
              tabindex="0"
              @input="handleDiagnosisInput"
              @keydown="handleKeyDown"
              @focus="handleFocus"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn secondary" @click="hideConsultationModal">取消</button>
          <button class="btn primary" @click="completeConsultationHandler" :disabled="!diagnosis.trim()">
完成诊断
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Icon } from '@iconify/vue'
import { getTodayAppointments, getAppointmentDetails, startConsultation, completeConsultation } from '../api/schedule.js'

const appointments = ref([])
const loading = ref(false)
const showConsultationModal = ref(false)
const currentConsultation = ref(null)
const diagnosis = ref('')

// 统计数据
const stats = computed(() => {
  const total = appointments.value.length
  const completed = appointments.value.filter(a => a.status === 'COMPLETED').length
  const waiting = appointments.value.filter(a => a.status === 'BOOKED').length

  return {
    total,
    completed,
    waiting
  }
})

// 完成率
const completionRate = computed(() => {
  if (stats.value.total === 0) return 0
  return Math.round((stats.value.completed / stats.value.total) * 100)
})

// 获取患者姓名
function getPatientName(appointment) {
  return appointment.patientName || '未知'
}

// 获取患者性别
function getPatientGender(appointment) {
  return appointment.patientGender || '未知'
}

// 获取患者年龄
function getPatientAge(appointment) {
  if (!appointment.patientBirthday) return '未知'

  const birthDate = new Date(appointment.patientBirthday)
  const today = new Date()
  let age = today.getFullYear() - birthDate.getFullYear()
  const monthDiff = today.getMonth() - birthDate.getMonth()

  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--
  }

  return age
}

// 获取挂号类型
function getAppointmentType(appointment) {
  const doctorTitle = appointment.title || ''
  return doctorTitle.includes('专家') || doctorTitle.includes('主任') ? '专家号' : '普通号'
}

// 获取挂号类型样式类
function getAppointmentTypeClass(appointment) {
  return getAppointmentType(appointment) === '专家号' ? 'expert' : 'normal'
}

// 获取预约时间段
function getTimeSlot(appointment) {
  return appointment.timeSlot || '未知'
}

// 获取状态文本
function getStatusText(status) {
  const statusMap = {
    'BOOKED': '等待中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'NO_SHOW': '已过期'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
function getStatusClass(status) {
  const classMap = {
    'BOOKED': 'waiting',
    'COMPLETED': 'completed',
    'CANCELLED': 'cancelled',
    'NO_SHOW': 'expired'
  }
  return classMap[status] || 'unknown'
}

// 加载今日预约数据
async function loadTodayAppointments() {
  loading.value = true
  try {
    const response = await getTodayAppointments()
    if (response.code === 200) {
      appointments.value = response.data || []
    } else {
      console.error('获取预约数据失败:', response.message)
      appointments.value = []
    }
  } catch (error) {
    console.error('加载预约数据失败:', error)
    appointments.value = []
  } finally {
    loading.value = false
  }
}

// 开始接诊
async function startConsultationHandler(appointment) {
  try {
    // 生成就诊时间（前端本地时间）
    const visitTimeString = getLocalTimeString()

    const response = await startConsultation(appointment.appointmentId, visitTimeString)
    if (response.code === 200) {
      // 获取完整的预约详情
      const detailResponse = await getAppointmentDetails(appointment.appointmentId)
      if (detailResponse.code === 200 && detailResponse.data && detailResponse.data.length > 0) {
        // 从返回的数组中选择匹配的记录（根据patientId匹配）
        const appointmentDetail = detailResponse.data.find(item => item.patientId === appointment.patientId) || detailResponse.data[0]

        currentConsultation.value = {
          ...appointmentDetail,
          appointmentId: appointment.appointmentId,
          patientName: appointmentDetail.patientName || '未知',
          patientGender: appointmentDetail.patientGender || '未知',
          patientAge: getPatientAge({ patientBirthday: appointmentDetail.patientBirthday }),
          timeSlot: appointmentDetail.timeSlot || '未知',
          visitTime: visitTimeString, // 使用传递给后端的时间
          isEditing: false // 新建诊断模式
        }
        diagnosis.value = ''
        showConsultationModal.value = true

        // 使用nextTick确保DOM更新后再聚焦
        await nextTick()
        const textarea = document.querySelector('.diagnosis-input')
        if (textarea) {
          textarea.focus()
        }
      }
    } else {
      alert('开始接诊失败: ' + response.message)
    }
  } catch (error) {
    console.error('开始接诊失败:', error)
    alert('开始接诊失败，请重试')
  }
}


// 隐藏诊断弹窗
function hideConsultationModal() {
  showConsultationModal.value = false
  currentConsultation.value = null
  diagnosis.value = ''
}

// 诊断输入调试
function handleDiagnosisInput(event) {
  console.log('Diagnosis input:', event.target.value)
  diagnosis.value = event.target.value
}

// 键盘按下调试
function handleKeyDown(event) {
  console.log('Key down:', event.key)
}

// 聚焦调试
function handleFocus(event) {
  console.log('Textarea focused')
}

// 完成诊断
async function completeConsultationHandler() {
  if (!diagnosis.value.trim()) {
    alert('请填写诊断结果')
    return
  }

  try {
    const response = await completeConsultation(currentConsultation.value.appointmentId, currentConsultation.value.patientId, diagnosis.value)
    if (response.code === 200) {
      alert('诊断完成！')
      hideConsultationModal()
      // 重新加载数据
      await loadTodayAppointments()
    } else {
      alert('完成诊断失败: ' + response.message)
    }
  } catch (error) {
    console.error('完成诊断失败:', error)
    alert('完成诊断失败，请重试')
  }
}


// 生成浏览器本地时区时间字符串
function getLocalTimeString() {
  const now = new Date()
  // 使用浏览器本地时区
  return now.toLocaleString('zh-CN')
}

// 刷新数据
async function refresh() {
  await loadTodayAppointments()
}

// 初始化
onMounted(() => {
  loadTodayAppointments()
})
</script>

<style scoped>
/* 复制你原 DoctorDashboard 的卡片/表格风格，确保一致 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 25px;
}
.stat-card {
  padding: 20px 25px;
  border-radius: 12px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
  position: relative;
  overflow: hidden;
  transition: transform 0.2s;
}
.stat-card:hover { transform: translateY(-3px); }
.stat-info h3 { margin: 0; font-size: 0.9rem; opacity: 0.9; font-weight: normal; }
.stat-info .num { font-size: 2rem; font-weight: bold; display: block; margin-top: 5px; }
.bg-icon { font-size: 4rem; opacity: 0.2; position: absolute; right: -10px; bottom: -10px; transform: rotate(-15deg); }

.blue-gradient { background: linear-gradient(135deg, #3b82f6, #0056b3); }
.green-gradient { background: linear-gradient(135deg, #42e695, #3bb2b8); }
.orange-gradient { background: linear-gradient(135deg, #f093fb, #f5576c); }
.purple-gradient { background: linear-gradient(135deg, #5ee7df, #b490ca); }

.panel-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  display: flex;
  flex-direction: column;
}
.panel-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
}
.panel-header h3 { margin: 0; font-size: 1.2rem; color: #333; }
.panel-header small { color: #999; font-weight: normal; margin-left: 8px; }

.actions { display: flex; gap: 10px; }
.btn-refresh { border: 1px solid #ddd; background: white; padding: 8px 15px; border-radius: 6px; cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
.btn-refresh:hover { background: #f5f5f5; }

.table-wrapper { flex: 1; overflow-y: auto; }
table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 15px; color: #999; font-size: 0.85rem; border-bottom: 1px solid #eee; font-weight: 500; }
td { padding: 15px; border-bottom: 1px solid #f9f9f9; color: #333; font-size: 0.95rem; }
tr:hover { background: #f0f7ff; }

.id-col { font-family: monospace; color: #004ea2; font-weight: bold; }
.name-col { font-weight: 600; }

.type-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.8rem; }
.type-badge.normal { background: #e6f7ff; color: #004ea2; }
.type-badge.expert { background: #fff7e6; color: #fa8c16; border: 1px solid #ffd591; }

.status-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.8rem; font-weight: 500; }
.status-badge.waiting { background: #e6f7ff; color: #1890ff; }
.status-badge.completed { background: #f6ffed; color: #52c41a; }
.status-badge.cancelled { background: #fff2f0; color: #ff4d4f; }
.status-badge.expired { background: #f9f9f9; color: #8c8c8c; }

.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  background: #e6f7ff;
  color: #004ea2;
  font-size: 0.85rem;
  font-weight: 700;
}

.btn-action {
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.9rem;
  margin-right: 10px;
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.2s;
}
.btn-action.primary {
  color: #004ea2;
  font-weight: bold;
  border: 1px solid #004ea2;
}
.btn-action.primary:hover {
  background: #004ea2;
  color: white;
}
.btn-action.success {
  background: #52c41a;
  color: white;
  font-weight: bold;
}
.btn-action.success:hover {
  background: #389e0d;
}
.completed-text { color: #52c41a; font-weight: 500; }
.completed-actions { display: flex; align-items: center; gap: 8px; }
.btn-action.secondary { color: #666; border-color: #d9d9d9; }
.btn-action.secondary:hover { background: #f5f5f5; }
.btn-action.small { padding: 4px 8px; font-size: 0.85rem; }

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #666;
}

.modal-body {
  padding: 24px;
}

.patient-info, .consultation-info, .diagnosis-section {
  margin-bottom: 24px;
}

.patient-info h4, .consultation-info h4, .diagnosis-section h4 {
  margin: 0 0 16px 0;
  font-size: 1rem;
  color: #333;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
  margin-right: 8px;
}

.info-item span {
  color: #333;
}

.diagnosis-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 0.95rem;
  line-height: 1.5;
  resize: vertical;
  outline: none;
  transition: border-color 0.2s;
}

.diagnosis-input:focus {
  border-color: #004ea2;
  box-shadow: 0 0 0 2px rgba(0, 78, 162, 0.1);
}

.modal-footer {
  padding: 20px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid #d9d9d9;
  background: white;
  color: #666;
  transition: all 0.2s;
}

.btn.primary {
  background: #004ea2;
  color: white;
  border-color: #004ea2;
}

.btn.primary:hover:not(:disabled) {
  background: #003d80;
  border-color: #003d80;
}

.btn.secondary:hover {
  background: #f5f5f5;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>

