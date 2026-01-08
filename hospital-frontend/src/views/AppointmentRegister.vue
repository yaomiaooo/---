<template>
  <div class="appointment-page">
    
    <header class="main-header">
      <div class="header-inner">
        <div class="logo-group" @click="router.push('/user')">
          <span class="logo-icon">🏥</span>
          <div class="logo-text">
            <h1>浙江工业大学健行医院</h1>
            <small>ZHEJIANG PROVINCIAL PEOPLE'S HOSPITAL</small>
          </div>
        </div>
        <div class="back-home" @click="router.push('/user')">
          <Icon icon="mdi:home" /> 返回首页
        </div>
      </div>
    </header>

    <div class="top-banner-section">
      <div class="banner-bg">
        <div class="banner-text"><h1>预约挂号</h1></div>
      </div>
      <div class="breadcrumb-strip">
        <div class="container">
          <span @click="router.push('/user')">网站首页</span> 
          <Icon icon="mdi:chevron-right" />
          <span>就诊指南</span>
          <Icon icon="mdi:chevron-right" />
          <span class="current">预约挂号</span>
        </div>
        <div class="strip-shape"></div>
      </div>
    </div>

    <main class="main-content">
      <div class="content-container">
        
        <el-steps :active="currentStep - 1" finish-status="success" align-center style="margin-bottom: 30px;">
          <el-step title="选择院区" />
          <el-step title="选择科室" />
          <el-step title="选择医生" />
          <el-step title="排班详情" />
          <el-step title="选择就诊人" />
          <el-step title="确认预约" />
        </el-steps>

        <div v-if="currentStep === 1" class="step-content fade-in">
          <h2 class="step-title">请选择就诊院区</h2>
          <div v-if="loading" class="loading-tip">加载中...</div>
          <div v-else-if="campuses.length === 0" class="empty-tip">暂无院区信息</div>
          <div v-else class="campus-grid">
            <div v-for="campus in campuses" :key="campus.id" class="campus-card" @click="selectCampus(campus)">
              <img :src="campus.img" alt="campus" class="campus-img">
              <div class="campus-info">
                <h3>{{ campus.name }}</h3>
                <p><Icon icon="mdi:map-marker" /> {{ campus.addr }}</p>
                <button class="btn-select">去挂号</button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 2" class="step-content fade-in">
          <div class="step-header-row">
            <h2 class="step-title">当前选择：<span class="highlight">{{ bookingData.campusName }}</span></h2>
            <button class="btn-back" @click="currentStep = 1">重新选择院区</button>
          </div>
          <div class="dept-selector">
            <div class="dept-sidebar">
              <div 
                v-for="(cat, index) in deptCategories" :key="index"
                class="sidebar-item" :class="{ active: activeCategory === cat.name }"
                @click="activeCategory = cat.name"
              >
                {{ cat.name }}
              </div>
            </div>
            <div class="dept-main-list">
              <div v-if="loading" class="loading-tip">加载中...</div>
              <div v-else-if="currentDepts.length === 0" class="empty-tip">暂无科室信息</div>
              <div v-else class="grid-wrapper">
                <div v-for="dept in currentDepts" :key="dept.departmentId" class="dept-item" @click="selectDept(dept)">
                  {{ dept.departmentName }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 3" class="step-content fade-in">
          <div class="step-header-row">
            <div class="info-crumb">
              <span class="tag">{{ bookingData.campusName }}</span>
              <Icon icon="mdi:chevron-right" />
              <span class="tag">{{ bookingData.deptName }}</span>
            </div>
            <button class="btn-back" @click="currentStep = 2">返回上一步</button>
          </div>

          <div class="booking-tabs">
            <div class="b-tab" :class="{ active: bookingMode === 'doctor' }" @click="switchBookingMode('doctor')">按医生挂号</div>
            <div class="b-tab" :class="{ active: bookingMode === 'date' }" @click="switchBookingMode('date')">按日期挂号</div>
          </div>

          <div v-if="bookingMode === 'date'" class="date-calendar-strip">
            <div 
              v-for="(day, idx) in weekData" :key="idx" 
              class="day-box" :class="{ active: selectedDateIndex === idx }"
              @click="selectedDateIndex = idx"
            >
              <div class="week-day">{{ day.week }}</div>
              <div class="date-num">{{ day.date }}</div>
              <div class="status" :class="day.status === '有号' ? 'available' : 'full'">{{ day.status }}</div>
            </div>
          </div>

          <div class="doctor-list-wrapper">
            <div v-if="loading" class="empty-tip">加载中...</div>
            <div v-else-if="bookingMode === 'date' && filteredDoctors.length === 0" class="empty-tip">
              <Icon icon="mdi:calendar-remove" style="font-size: 48px; color: #999; margin-bottom: 16px;" />
              <p style="font-size: 16px; color: #666; margin-bottom: 8px;">
                {{ selectedDateIndex >= 0 && weekData[selectedDateIndex] ? `${weekData[selectedDateIndex].fullDate} 当天暂无号源，无法预约` : '当前日期暂无号源，无法预约' }}
              </p>
              <p style="font-size: 14px; color: #999;">请选择其他日期或切换为"按医生挂号"模式</p>
            </div>
            <div v-else-if="bookingMode === 'doctor' && filteredDoctors.length === 0" class="empty-tip">当前暂无医生信息</div>
            <div v-else v-for="doc in filteredDoctors" :key="doc.id" class="doctor-row">
              <div class="doc-left">
                <img :src="doc.photo" class="avatar" />
                <div class="doc-basic">
                  <div class="name-line">
                    <span class="name">{{ doc.name }}</span>
                    <span class="title">{{ doc.title }}</span>
                    <span class="badge" v-if="doc.isExpert">名医</span>
                  </div>
                  <div class="skill">擅长：{{ doc.skill }}</div>
                </div>
              </div>
              <div class="doc-right">
                <button class="btn-book" @click="goToDoctorDetail(doc)">预约挂号</button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 4" class="step-content fade-in">
          <div class="step-header-row">
             <h2 class="step-title">医生排班详情</h2>
             <button class="btn-back" @click="currentStep = 3">返回列表</button>
          </div>

          <div class="doctor-profile-card">
            <div class="profile-left">
              <img :src="selectedDoctor.photo" class="profile-avatar">
              <div class="profile-info">
                <div class="profile-name">
                  {{ selectedDoctor.name }} <span class="profile-title">{{ selectedDoctor.title }}</span>
                </div>
                <div class="profile-dept">{{ bookingData.campusName }} | {{ bookingData.deptName }}</div>
                <div class="profile-tags">
                  <span class="tag-item">从业20年</span>
                  <span class="tag-item">好评率 99%</span>
                  <span class="tag-item">接诊量 5000+</span>
                </div>
                <div class="profile-desc">擅长：{{ selectedDoctor.skill }}</div>
              </div>
            </div>
            <div class="profile-right">
              <button class="btn-fav"><Icon icon="mdi:heart-outline" /> 关注医生</button>
            </div>
          </div>

          <div class="schedule-grid-container">
            <div class="grid-header-title"><Icon icon="mdi:calendar-clock" /> 选择就诊时间</div>
            <table class="schedule-table">
              <thead>
                <tr>
                  <th width="100">时段</th>
                  <th v-for="(day, i) in weekData" :key="i">
                    <div class="th-week">{{ day.week }}</div>
                    <div class="th-date">{{ day.date }}</div>
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="td-period">08:00-10:00</td>
                  <td v-for="(day, i) in weekData" :key="'morning-'+i" class="td-cell">
                    <div v-if="hasSlot(selectedDoctor, day.date, 'morning')">
                      <button class="btn-slot available" @click="selectSlot(day, 'morning')">
                        挂号 <span class="price">￥{{ selectedDoctor.price }}</span>
                      </button>
                    </div>
                    <div v-else class="empty-slot">无号</div>
                  </td>
                </tr>
                <tr>
                  <td class="td-period">13:30-17:00</td>
                  <td v-for="(day, i) in weekData" :key="'afternoon-'+i" class="td-cell">
                    <div v-if="hasSlot(selectedDoctor, day.date, 'afternoon')">
                      <button class="btn-slot available" @click="selectSlot(day, 'afternoon')">
                        挂号 <span class="price">￥{{ selectedDoctor.price }}</span>
                      </button>
                    </div>
                    <div v-else class="empty-slot">无号</div>
                  </td>
                </tr>
                <tr>
                  <td class="td-period">19:00-21:00</td>
                  <td v-for="(day, i) in weekData" :key="'evening-'+i" class="td-cell">
                    <div v-if="hasSlot(selectedDoctor, day.date, 'evening')">
                      <button class="btn-slot available" @click="selectSlot(day, 'evening')">
                        挂号 <span class="price">￥{{ selectedDoctor.price }}</span>
                      </button>
                    </div>
                    <div v-else class="empty-slot">无号</div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-if="currentStep === 5" class="step-content fade-in">
          <div class="step-header-row">
             <h2 class="step-title">选择就诊人</h2>
             <button class="btn-back" @click="currentStep = 4">返回时间选择</button>
          </div>
          
          <div class="patient-selection-container">
            <div v-if="loadingPatients" class="loading-tip">加载就诊人列表中...</div>
            <div v-else-if="patientList.length === 0 && !showAddForm" class="empty-tip">
              <Icon icon="mdi:account-group-outline" class="empty-icon" />
              <p>您还没有添加就诊人，请先添加就诊人</p>
            </div>
            <div v-else class="saved-patient-list">
              <div 
                v-for="p in patientList" 
                :key="p.id" 
                class="patient-card"
                :class="{ active: selectedPatientId === p.id }"
                @click="selectPatient(p.id)"
              >
                <div class="p-header">
                  <span class="p-name">{{ p.name }}</span>
                  <span class="p-tag">{{ p.relation }}</span>
                  <Icon icon="mdi:check-circle" class="check-icon" v-if="selectedPatientId === p.id" />
                </div>
                <div class="p-info">身份证：{{ maskIdCard(p.idCard) }}</div>
                <div class="p-info">手机号：{{ maskPhone(p.phone) }}</div>
              </div>

              <div class="add-patient-btn" @click="showAddForm = true" v-if="!showAddForm">
                <Icon icon="mdi:plus-circle-outline" /> 添加就诊人
              </div>
            </div>

            <div class="add-patient-form" v-if="showAddForm">
              <h3 class="form-title">添加新就诊人</h3>
              <div class="form-grid">
                <div class="form-group">
                  <label>姓名 <span class="required">*</span></label>
                  <input type="text" v-model="newPatient.name" placeholder="请输入真实姓名">
                </div>
                <div class="form-group">
                  <label>身份证号 <span class="required">*</span></label>
                  <input type="text" v-model="newPatient.idCard" placeholder="请输入18位身份证号" maxlength="18">
                </div>
                <div class="form-group">
                  <label>手机号码 <span class="required">*</span></label>
                  <input type="text" v-model="newPatient.phone" placeholder="请输入11位手机号码" maxlength="11">
                </div>
                <div class="form-group">
                  <label>出生日期 <span class="required">*</span></label>
                  <input type="date" v-model="newPatient.dob">
                </div>
                <div class="form-group">
                  <label>性别 <span class="required">*</span></label>
                  <select v-model="newPatient.gender">
                    <option value="男">男</option>
                    <option value="女">女</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>关系</label>
                  <select v-model="newPatient.relation">
                    <option value="本人">本人</option>
                    <option value="父母">父母</option>
                    <option value="子女">子女</option>
                    <option value="配偶">配偶</option>
                    <option value="其他">其他</option>
                  </select>
                </div>
              </div>
              <div class="form-actions">
                <button class="btn-cancel-add" @click="showAddForm = false" :disabled="loadingPatients">取消</button>
                <button class="btn-save-add" @click="addNewPatient" :disabled="loadingPatients">
                  {{ loadingPatients ? '保存中...' : '保存并使用' }}
                </button>
              </div>
            </div>

            <div class="action-footer" v-if="!showAddForm">
              <button class="btn-next-step" :disabled="!selectedPatientId" @click="goToConfirm">
                下一步：确认预约
              </button>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 6" class="step-content fade-in">
          <div class="confirm-card">
            <div class="card-header">确认挂号信息</div>
            <div class="card-body">
              <div class="confirm-row">
                <span class="label">预约医院：</span>
                <span class="val">{{ bookingData.campusName }}</span>
              </div>
              <div class="confirm-row">
                <span class="label">预约科室：</span>
                <span class="val">{{ bookingData.deptName }}</span>
              </div>
              <div class="confirm-row">
                <span class="label">预约医生：</span>
                <span class="val bold">{{ bookingData.doctorName }} ({{ bookingData.doctorTitle }})</span>
              </div>
              <div class="confirm-row">
                <span class="label">就诊时间：</span>
                <span class="val highlight">{{ bookingData.date }} ({{ bookingData.week }}) {{ bookingData.period }}</span>
              </div>
              <div class="confirm-row">
                <span class="label">挂号费用：</span>
                <span class="val price">￥{{ bookingData.price }}.00</span>
              </div>
              <div class="divider"></div>
              <div class="confirm-row">
                <span class="label">就诊人：</span>
                <span class="val">{{ currentPatient?.name }} ({{ maskPhone(currentPatient?.phone) }})</span>
              </div>
              <div class="confirm-row">
                <span class="label">身份证：</span>
                <span class="val">{{ maskIdCard(currentPatient?.idCard) }}</span>
              </div>
            </div>
            <div class="card-footer">
              <div class="agreement">
                <input type="checkbox" id="agree" checked>
                <label for="agree">我已阅读并同意《预约挂号须知》</label>
              </div>
              <div class="btn-group">
                <button class="btn-cancel" @click="currentStep = 5">返回修改</button>
                <button class="btn-confirm" @click="submitBooking">确定预约</button>
              </div>
            </div>
          </div>
        </div>

      </div>
    </main>

    <footer class="app-footer">
      <div class="footer-bottom-bar">Copyright © 2025 浙江工业大学健行医院网站版权所有</div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { ElSteps, ElStep } from 'element-plus';
import 'element-plus/dist/index.css';
import { getAllHospitals, getDepartmentList } from '../api/hospital';
import { getDoctors } from '../api/doctor';
import { getSchedules, getOutpatientSchedules } from '../api/schedule';
import { getMyPatients, addPatient } from '../api/patient';
import { createAppointment } from '../api/appointment';

const router = useRouter();
const currentStep = ref(1);
const bookingMode = ref('doctor'); 
const activeCategory = ref('内科');
const selectedDateIndex = ref(0);
const selectedDoctor = ref({}); 
const loading = ref(false);

// 院区ID映射
const campusIdMap = {
  '朝晖院区': '1',
  '屏峰院区': '2'
};

// 暂存预约数据
const bookingData = ref({
  campusId: '',
  campusName: '',
  deptId: '',
  deptName: '',
  doctorId: '', // 添加医生ID
  doctorName: '',
  doctorTitle: '',
  price: 0,
  date: '',
  week: '',
  period: '',
  scheduleId: '', // 添加排班ID
  timeSlot: '', // 时间段（如：08:00-10:00）
  patientId: '', // 就诊人ID
  patientName: '' // 就诊人姓名
});

// --- 数据加载 ---
const campuses = ref([]);
const departmentList = ref([]);
const doctorList = ref([]);
const outpatientScheduleData = ref(null); // 存储按日期挂号的排班数据

// 加载院区列表
const loadHospitals = async () => {
  loading.value = true;
  try {
    const res = await getAllHospitals();
    if (res.code === 200 && res.data) {
      campuses.value = res.data.map(hospital => ({
        id: hospital.hospitalId,
        name: hospital.hospitalName,
        addr: hospital.hospitalAddress || '地址待补充',
        img: 'https://tse4.mm.bing.net/th/id/OIP.FXcznu5jbYoxxJD3mQZ8-gHaFj?rs=1&pid=ImgDetMain&o=7&rm=3'
      }));
    }
  } catch (error) {
    console.error('获取院区列表失败:', error);
    campuses.value = [];
  } finally {
    loading.value = false;
  }
};

// 加载科室列表
const loadDepartments = async () => {
  if (!bookingData.value.campusId) return;
  
  loading.value = true;
  try {
    const res = await getDepartmentList(bookingData.value.campusId);
    if (res.code === 200 && res.data) {
      departmentList.value = res.data;
      // 按科室名称分组（简化处理，实际可以根据科室类型分组）
      groupDepartmentsByCategory();
    }
  } catch (error) {
    console.error('获取科室列表失败:', error);
    departmentList.value = [];
  } finally {
    loading.value = false;
  }
};

// 按类别分组科室（简化处理）
const deptCategories = ref([]);
const groupDepartmentsByCategory = () => {
  // 简化处理：将所有科室放在"全部科室"类别下
  deptCategories.value = [
    { name: '全部科室', list: departmentList.value }
  ];
  if (departmentList.value.length > 0) {
    activeCategory.value = '全部科室';
  }
};

// 加载医生列表
const loadDoctors = async () => {
  if (!bookingData.value.campusId || !bookingData.value.deptId) return;
  
  loading.value = true;
  try {
    const res = await getDoctors(bookingData.value.campusId, bookingData.value.deptId, null);
    if (res.code === 200 && res.data) {
      // 根据医生职称计算挂号费
      const calculatePrice = (title) => {
        if (!title) return 30;
        const titleLower = title.toLowerCase();
        if (titleLower.includes('专家')) return 100;
        if (titleLower.includes('主任')) return 50;
        if (titleLower.includes('医师')) return 30;
        return 30;
      };
      
      doctorList.value = res.data.map(doctor => ({
        id: doctor.doctorId,
        name: doctor.doctorName,
        title: doctor.title || '医师',
        isExpert: doctor.title && doctor.title.includes('专家'), // 根据职称判断是否为专家
        price: calculatePrice(doctor.title), // 根据职称计算挂号费
        skill: doctor.doctorIntro || '暂无介绍',
        photo: doctor.avatarUrl || 'https://randomuser.me/api/portraits/men/32.jpg',
        schedule: {}, // 暂时没有排班信息
        departmentName: doctor.departmentName || '' // 科室名称
      }));
      
      // 如果当前是"按日期挂号"模式，加载排班数据
      if (bookingMode.value === 'date') {
        // 不在这里设置 loading，因为已经在 loadDoctors 中设置了
        await loadOutpatientSchedules();
      }
    } else {
      doctorList.value = [];
    }
  } catch (error) {
    console.error('获取医生列表失败:', error);
    doctorList.value = [];
  } finally {
    loading.value = false;
  }
};

// 加载按日期挂号的排班数据
const loadOutpatientSchedules = async () => {
  if (!bookingData.value.campusId || !bookingData.value.deptId) {
    outpatientScheduleData.value = null;
    return;
  }
  
  try {
    // 获取未来7天的排班（从今天开始）
    const today = new Date();
    const endDate = new Date(today);
    endDate.setDate(today.getDate() + 6); // 未来7天
    
    const startDateStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    const endDateStr = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`;
    
    const res = await getOutpatientSchedules(bookingData.value.campusId, 'all', startDateStr, endDateStr);
    console.log('门诊排班API响应:', res);
    
    if (res.code === 200 && res.data) {
      // 找到当前科室的排班数据
      const deptSchedule = res.data.find(dept => dept.departmentId === bookingData.value.deptId);
      outpatientScheduleData.value = deptSchedule || null;
      
      // 更新日期状态（是否有号源）
      updateDateStatus();
    } else {
      outpatientScheduleData.value = null;
      updateDateStatus(); // 即使没有数据，也要更新状态
    }
  } catch (error) {
    console.error('获取门诊排班失败:', error);
    outpatientScheduleData.value = null;
    updateDateStatus(); // 出错时也要更新状态
  }
};

// 更新日期状态（是否有号源）
const updateDateStatus = () => {
  if (!outpatientScheduleData.value || !outpatientScheduleData.value.doctors) {
    // 如果没有排班数据，所有日期都显示"无号"
    weekData.value.forEach(day => {
      day.status = '无号';
    });
    return;
  }
  
  // 遍历所有日期，检查是否有号源
  weekData.value.forEach(day => {
    // 将日期格式转换为后端返回的格式（MM.dd）
    const dateParts = day.fullDate.split('-');
    const dateKey = `${dateParts[1]}.${dateParts[2]}`;
    
    // 检查该日期是否有任何医生有号源
    let hasAvailable = false;
    for (const doctor of outpatientScheduleData.value.doctors) {
      if (doctor.scheduleMap && doctor.scheduleMap[dateKey]) {
        const timeSlots = doctor.scheduleMap[dateKey];
        // 检查是否有可预约的时间段
        const hasAvailableSlot = timeSlots.some(slot => slot.isAvailable === true);
        if (hasAvailableSlot) {
          hasAvailable = true;
          break;
        }
      }
    }
    
    day.status = hasAvailable ? '有号' : '无号';
  });
};

// --- 就诊人管理逻辑 ---
const selectedPatientId = ref(null); // 默认不选中
const showAddForm = ref(false);
const newPatient = ref({ name: '', idCard: '', phone: '', dob: '', gender: '男', relation: '其他' });
const patientList = ref([]); // 从后端获取的就诊人列表
const loadingPatients = ref(false); // 就诊人加载状态

// 加载就诊人列表
const loadPatients = async () => {
  loadingPatients.value = true;
  try {
    const res = await getMyPatients();
    if (res.code === 200 && res.data) {
      // 将后端返回的数据格式转换为前端需要的格式
      // 后端返回：{ patientId, name, idCard, phone, relation, dob, gender }
      // 前端需要：{ id, name, idCard, phone, relation, gender }
      patientList.value = res.data.map(p => ({
        id: p.patientId, // 使用 patientId 作为 id
        name: p.name,
        idCard: p.idCard,
        phone: p.phone,
        relation: p.relation || '其他',
        gender: p.gender,
        dob: p.dob // 保留出生日期，用于添加新就诊人时的参考
      }));
      
      // 如果有就诊人，默认选中第一个
      if (patientList.value.length > 0 && !selectedPatientId.value) {
        selectedPatientId.value = patientList.value[0].id;
      }
    } else {
      console.error('获取就诊人列表失败:', res.message);
      patientList.value = [];
    }
  } catch (error) {
    console.error('获取就诊人列表失败:', error);
    alert(error.message || '获取就诊人列表失败，请检查网络连接');
    patientList.value = [];
  } finally {
    loadingPatients.value = false;
  }
};

const currentPatient = computed(() => {
  return patientList.value.find(p => p.id === selectedPatientId.value);
});

// 掩码处理函数
const maskPhone = (str) => str ? str.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '';
const maskIdCard = (str) => str ? str.replace(/(\d{4})\d{10}(\d{4})/, '$1**********$2') : '';

// 选择就诊人
const selectPatient = (id) => {
  selectedPatientId.value = id;
};

// 添加新就诊人
const addNewPatient = async () => {
  if(!newPatient.value.name || !newPatient.value.idCard || !newPatient.value.phone || !newPatient.value.dob) {
    alert('请填写完整信息（姓名、身份证号、手机号、出生日期）');
    return;
  }

  // 验证手机号格式
  const phoneRegex = /^1\d{10}$/;
  if (!phoneRegex.test(newPatient.value.phone)) {
    alert('请输入正确的手机号码格式（11位数字，以1开头）');
    return;
  }

  // 验证身份证号格式
  if (newPatient.value.idCard.length !== 18) {
    alert('请输入18位身份证号');
    return;
  }

  loadingPatients.value = true;
  try {
    const res = await addPatient({
      name: newPatient.value.name,
      idCard: newPatient.value.idCard,
      phone: newPatient.value.phone,
      dob: newPatient.value.dob,
      gender: newPatient.value.gender,
      relation: newPatient.value.relation || '其他'
    });

    if (res.code === 200 && res.data) {
      alert('添加就诊人成功！');
      // 重新加载就诊人列表
      await loadPatients();
      // 自动选中新添加的就诊人
      if (res.data.patientId) {
        selectedPatientId.value = res.data.patientId;
      }
      showAddForm.value = false;
      // 清空表单
      newPatient.value = { name: '', idCard: '', phone: '', dob: '', gender: '男', relation: '其他' };
    } else {
      alert(res.message || '添加就诊人失败，请重试');
    }
  } catch (error) {
    console.error('添加就诊人失败:', error);
    alert(error.message || '添加就诊人失败，请检查网络连接');
  } finally {
    loadingPatients.value = false;
  }
};

const goToConfirm = () => {
  // 保存选中的就诊人ID到预约数据中
  if (selectedPatientId.value && currentPatient.value) {
    bookingData.value.patientId = selectedPatientId.value;
    bookingData.value.patientName = currentPatient.value.name;
  }
  currentStep.value = 6;
  window.scrollTo(0, 0);
};

// --- 排班数据 ---
const scheduleList = ref([]); // 存储从后端获取的排班数据

// 动态生成未来7天的日期数据
const generateWeekData = () => {
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  const weekData = [];
  const today = new Date();
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(today);
    date.setDate(today.getDate() + i);
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const weekDay = weekDays[date.getDay()];
    const dateStr = `${month}-${day}`;
    const fullDateStr = `${date.getFullYear()}-${month}-${day}`;
    
    weekData.push({
      date: dateStr,
      fullDate: fullDateStr, // 完整日期用于匹配排班数据
      week: weekDay,
      status: '有号' // 默认状态，实际会根据排班数据更新
    });
  }
  
  return weekData;
};

const weekData = ref(generateWeekData());

const currentDepts = computed(() => {
  const cat = deptCategories.value.find(c => c.name === activeCategory.value);
  return cat ? cat.list : [];
});

const filteredDoctors = computed(() => {
  if (bookingMode.value === 'doctor') {
    return doctorList.value;
  } else {
    // 按日期挂号模式：筛选出指定日期有号源的医生
    if (!outpatientScheduleData.value || !outpatientScheduleData.value.doctors) {
      return []; // 没有排班数据，返回空列表
    }
    
    // 获取选中的日期
    const selectedDay = weekData.value[selectedDateIndex.value];
    if (!selectedDay) {
      return [];
    }
    
    // 将日期格式转换为后端返回的格式（MM.dd）
    const dateParts = selectedDay.fullDate.split('-');
    const dateKey = `${dateParts[1]}.${dateParts[2]}`;
    
    // 筛选出该日期有号源的医生
    const availableDoctors = [];
    for (const scheduleDoctor of outpatientScheduleData.value.doctors) {
      if (scheduleDoctor.scheduleMap && scheduleDoctor.scheduleMap[dateKey]) {
        const timeSlots = scheduleDoctor.scheduleMap[dateKey];
        // 检查是否有可预约的时间段
        const hasAvailableSlot = timeSlots.some(slot => slot.isAvailable === true);
        if (hasAvailableSlot) {
          // 从 doctorList 中找到对应的医生信息
          const doctorInfo = doctorList.value.find(doc => doc.id === scheduleDoctor.doctorId);
          if (doctorInfo) {
            availableDoctors.push(doctorInfo);
          }
        }
      }
    }
    
    return availableDoctors;
  }
});

// 选择院区
const selectCampus = (campus) => {
  bookingData.value.campusId = campus.id;
  bookingData.value.campusName = campus.name;
  currentStep.value = 2;
  window.scrollTo(0, 0);
  loadDepartments();
};

// 选择科室
const selectDept = (dept) => {
  bookingData.value.deptId = dept.departmentId;
  bookingData.value.deptName = dept.departmentName;
  currentStep.value = 3;
  window.scrollTo(0, 0);
  loadDoctors();
};

// 切换预约模式
const switchBookingMode = async (mode) => {
  bookingMode.value = mode;
  if (mode === 'date') {
    // 切换到按日期挂号模式时，加载排班数据
    loading.value = true;
    try {
      await loadOutpatientSchedules();
    } finally {
      loading.value = false;
    }
  }
};

// 监听日期选择变化
watch(selectedDateIndex, () => {
  // 日期变化时，filteredDoctors 会自动更新（computed）
  // 这里可以添加额外的逻辑，比如滚动到顶部等
});

const goToDoctorDetail = async (doc) => { 
  selectedDoctor.value = doc;
  bookingData.value.doctorId = doc.id; // 保存医生ID
  currentStep.value = 4;
  window.scrollTo(0, 0);
  // 加载该医生的排班信息
  await loadSchedules(doc.id);
};

// 加载医生的排班信息
const loadSchedules = async (doctorId) => {
  if (!doctorId || !bookingData.value.campusId) return;
  
  loading.value = true;
  try {
    // 获取未来7天的排班（从今天开始）
    const today = new Date();
    const endDate = new Date(today);
    endDate.setDate(today.getDate() + 7);
    
    const startDateStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    const endDateStr = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`;
    
    const res = await getSchedules(doctorId, bookingData.value.campusId, startDateStr, endDateStr);
    console.log('排班API响应:', res);
    
    if (res.code === 200 && res.data) {
      scheduleList.value = res.data;
      console.log('排班数据:', scheduleList.value);
    } else {
      scheduleList.value = [];
    }
  } catch (error) {
    console.error('获取排班信息失败:', error);
    scheduleList.value = [];
  } finally {
    loading.value = false;
  }
};

// 判断某个日期和时间段是否有号
const hasSlot = (doc, dateStr, period) => {
  // 找到对应的完整日期
  const dayInfo = weekData.value.find(d => d.date === dateStr);
  if (!dayInfo) return false;
  
  // 根据时间段判断匹配模式
  // morning: 08:00-10:00
  // afternoon: 13:30-17:00
  // evening: 19:00-21:00
  let timeSlotPatterns = [];
  if (period === 'morning' || period === 'am' || period === '上午') {
    // 匹配 08:00-10:00 时间段
    timeSlotPatterns = ['08:00', '09:00', '10:00'];
  } else if (period === 'afternoon' || period === 'pm' || period === '下午') {
    // 匹配 13:30-17:00 时间段
    timeSlotPatterns = ['13:30', '14:00', '15:00', '16:00', '17:00'];
  } else if (period === 'evening' || period === '晚间') {
    // 匹配 19:00-21:00 时间段
    timeSlotPatterns = ['19:00', '20:00', '21:00'];
  }
  
  // 查找该日期和时间段的排班
  const schedule = scheduleList.value.find(s => {
    const scheduleDate = s.workDate ? (typeof s.workDate === 'string' ? s.workDate.split('T')[0] : s.workDate) : '';
    const matchesDate = scheduleDate === dayInfo.fullDate;
    
    // 检查时间段是否匹配
    let matchesTime = false;
    if (s.timeSlot) {
      const timeSlot = s.timeSlot || '';
      // 优先根据 timeSlot 字符串判断
      if (period === 'morning' || period === 'am' || period === '上午') {
        matchesTime = timeSlot.includes('08:00') || timeSlot.includes('09:00') || timeSlot.includes('10:00');
      } else if (period === 'afternoon' || period === 'pm' || period === '下午') {
        matchesTime = timeSlot.includes('13:30') || timeSlot.includes('14:') || timeSlot.includes('15:') || 
                      timeSlot.includes('16:') || timeSlot.includes('17:00');
      } else if (period === 'evening' || period === '晚间') {
        matchesTime = timeSlot.includes('19:00') || timeSlot.includes('20:') || timeSlot.includes('21:00');
      }
      
      // 兼容旧格式：如果没有匹配到，尝试用 pattern 匹配
      if (!matchesTime && timeSlotPatterns.length > 0) {
        for (const pattern of timeSlotPatterns) {
          if (timeSlot.startsWith(pattern) || timeSlot.includes(pattern)) {
            matchesTime = true;
            break;
          }
        }
      }
    }
    
    return matchesDate && matchesTime && s.isAvailable;
  });
  
  return schedule != null;
};

// 获取排班信息（用于显示和保存scheduleId）
const getScheduleForSlot = (dateStr, period) => {
  const dayInfo = weekData.value.find(d => d.date === dateStr);
  if (!dayInfo) return null;
  
  // 根据时间段判断匹配模式
  let timeSlotPatterns = [];
  if (period === 'morning' || period === 'am' || period === '上午') {
    timeSlotPatterns = ['08:00', '09:00', '10:00'];
  } else if (period === 'afternoon' || period === 'pm' || period === '下午') {
    timeSlotPatterns = ['13:30', '14:00', '15:00', '16:00', '17:00'];
  } else if (period === 'evening' || period === '晚间') {
    timeSlotPatterns = ['19:00', '20:00', '21:00'];
  }
  
  return scheduleList.value.find(s => {
    const scheduleDate = s.workDate ? (typeof s.workDate === 'string' ? s.workDate.split('T')[0] : s.workDate) : '';
    const matchesDate = scheduleDate === dayInfo.fullDate;
    
    // 检查时间段是否匹配
    let matchesTime = false;
    if (s.timeSlot) {
      const timeSlot = s.timeSlot || '';
      // 优先根据 timeSlot 字符串判断
      if (period === 'morning' || period === 'am' || period === '上午') {
        matchesTime = timeSlot.includes('08:00') || timeSlot.includes('09:00') || timeSlot.includes('10:00');
      } else if (period === 'afternoon' || period === 'pm' || period === '下午') {
        matchesTime = timeSlot.includes('13:30') || timeSlot.includes('14:') || timeSlot.includes('15:') || 
                      timeSlot.includes('16:') || timeSlot.includes('17:00');
      } else if (period === 'evening' || period === '晚间') {
        matchesTime = timeSlot.includes('19:00') || timeSlot.includes('20:') || timeSlot.includes('21:00');
      }
      
      // 兼容旧格式：如果没有匹配到，尝试用 pattern 匹配
      if (!matchesTime && timeSlotPatterns.length > 0) {
        for (const pattern of timeSlotPatterns) {
          if (timeSlot.startsWith(pattern) || timeSlot.includes(pattern)) {
            matchesTime = true;
            break;
          }
        }
      }
    }
    
    return matchesDate && matchesTime && s.isAvailable;
  });
};

const selectSlot = (dayInfo, periodStr) => {
  // 获取对应的排班信息
  const schedule = getScheduleForSlot(dayInfo.date, periodStr);
  if (!schedule) {
    alert('该时间段暂无号源，请选择其他时间');
    return;
  }
  
  // 将时间段转换为中文显示
  let periodDisplay = '';
  if (periodStr === 'morning' || periodStr === 'am' || periodStr === '上午') {
    periodDisplay = '08:00-10:00';
  } else if (periodStr === 'afternoon' || periodStr === 'pm' || periodStr === '下午') {
    periodDisplay = '13:30-17:00';
  } else if (periodStr === 'evening' || periodStr === '晚间') {
    periodDisplay = '19:00-21:00';
  } else {
    // 如果 schedule 有 timeSlot，直接使用
    periodDisplay = schedule.timeSlot || periodStr;
  }
  
  bookingData.value.doctorName = selectedDoctor.value.name;
  bookingData.value.doctorTitle = selectedDoctor.value.title;
  bookingData.value.price = selectedDoctor.value.price;
  bookingData.value.date = dayInfo.fullDate; // 使用完整日期（yyyy-MM-dd格式）
  bookingData.value.week = dayInfo.week;
  bookingData.value.period = periodDisplay; // 保存时间段显示（如：08:00-10:00）
  bookingData.value.scheduleId = schedule.scheduleId; // 保存排班ID，用于后续预约
  bookingData.value.timeSlot = schedule.timeSlot; // 保存时间段（如：08:00-10:00）
  currentStep.value = 5; // 进入就诊人选择
  // 进入就诊人选择步骤时，加载就诊人列表
  loadPatients();
  window.scrollTo(0, 0);
};

const submitBooking = async () => {
  // 验证必要数据
  if (!bookingData.value.patientId) {
    alert('请选择就诊人');
    return;
  }
  if (!bookingData.value.scheduleId) {
    alert('请选择就诊时间');
    return;
  }
  if (!bookingData.value.campusId) {
    alert('预约信息不完整，请重新选择院区');
    return;
  }

  if (!confirm('确定要提交预约吗？')) {
    return;
  }

  loading.value = true;
  try {
    const res = await createAppointment({
      patientId: bookingData.value.patientId,
      scheduleId: bookingData.value.scheduleId,
      hospitalId: bookingData.value.campusId // 使用 campusId 作为 hospitalId
    });

    if (res.code === 200) {
      alert('预约成功！请按时就诊。');
      // 跳转到预约记录页面
      router.push('/visit-records');
    } else {
      alert(res.message || '预约失败，请重试');
    }
  } catch (error) {
    console.error('提交预约失败:', error);
    alert(error.message || '预约失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

// 组件挂载时加载院区列表
onMounted(() => {
  loadHospitals();
});
</script>

<style scoped>
/* 基础设置 */
.appointment-page { min-height: 100vh; background: #f4f6f9; font-family: 'Helvetica Neue', Arial, sans-serif; }
.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

/* Header & Banner 复用之前的样式 */
.main-header { height: 80px; background: white; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #ddd; }
.header-inner { width: 100%; max-width: 1200px; padding: 0 40px; display: flex; justify-content: space-between; align-items: center; }
.logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.logo-icon { font-size: 2.2rem; }
.logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
.logo-text small { font-size: 0.6rem; color: #666; }
.back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }

.top-banner-section { background: white; }
.banner-bg { height: 160px; background: linear-gradient(rgba(0,0,0,0.4), rgba(0,0,0,0.4)), url('https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?q=80&w=2000'); background-size: cover; background-position: center; display: flex; align-items: center; padding-left: 10%; }
.banner-text h1 { color: white; font-size: 2.2rem; }
.breadcrumb-strip { background: #f0ad4e; height: 50px; display: flex; align-items: center; position: relative; padding-left: 10%; color: white; }
.breadcrumb-strip .container { display: flex; align-items: center; gap: 10px; z-index: 2; }
.strip-shape { position: absolute; right: 0; top: 0; border-top: 50px solid #f0ad4e; border-left: 50px solid transparent; }

/* 主体容器 */
.main-content { padding: 40px 0; }
.content-container { max-width: 1200px; margin: 0 auto; padding: 0 40px; }

/* 步骤条 */
.steps-bar { display: flex; align-items: center; justify-content: center; margin-bottom: 40px; }
.step-item { display: flex; flex-direction: column; align-items: center; gap: 5px; position: relative; z-index: 2; }
.step-num { width: 40px; height: 40px; background: #ddd; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 1.2rem; transition: 0.3s; }
.step-text { font-size: 0.9rem; color: #999; font-weight: bold; }
.step-item.active .step-num { background: #2f80ed; }
.step-item.active .step-text { color: #2f80ed; }
.step-item.finished .step-num { background: #28a745; }
.step-line { width: 60px; height: 4px; background: #ddd; margin: -20px 10px 0 10px; transition: 0.3s; }
.step-line.active { background: #28a745; }

/* 院区 & 科室 & 医生列表 & 排班详情 复用之前的CSS (略微调整) */
.step-title { color: #333; margin-bottom: 25px; border-left: 5px solid #2f80ed; padding-left: 15px; }
.campus-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 30px; }
.campus-card { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 5px 15px rgba(0,0,0,0.05); cursor: pointer; transition: 0.3s; display: flex; }
.campus-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0,0,0,0.1); border: 1px solid #2f80ed; }
.campus-img { width: 200px; height: 160px; object-fit: cover; }
.campus-info { padding: 20px; flex: 1; display: flex; flex-direction: column; justify-content: center; }
.btn-select { align-self: flex-start; background: #2f80ed; color: white; border: none; padding: 8px 25px; border-radius: 20px; cursor: pointer; }

.step-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.highlight { color: #2f80ed; }
.btn-back { background: none; border: 1px solid #999; color: #666; padding: 5px 15px; border-radius: 4px; cursor: pointer; }
.dept-selector { display: flex; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.05); min-height: 500px; }
.dept-sidebar { width: 200px; background: #f7f9fc; border-right: 1px solid #eee; }
.sidebar-item { padding: 15px 20px; cursor: pointer; font-weight: bold; color: #555; transition: 0.2s; border-left: 4px solid transparent; }
.sidebar-item.active { background: white; color: #2f80ed; border-left-color: #2f80ed; }
.dept-main-list { flex: 1; padding: 30px; }
.grid-wrapper { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.dept-item { background: white; border: 1px solid #eee; padding: 15px; text-align: center; border-radius: 6px; cursor: pointer; transition: 0.2s; }
.dept-item:hover { color: #2f80ed; border-color: #2f80ed; box-shadow: 0 5px 10px rgba(47, 128, 237, 0.1); }

.booking-tabs { display: flex; gap: 0; margin-bottom: 20px; border-bottom: 2px solid #ddd; }
.b-tab { padding: 15px 30px; font-size: 1.1rem; cursor: pointer; font-weight: bold; color: #666; position: relative; top: 2px; }
.b-tab.active { color: #2f80ed; border-bottom: 3px solid #2f80ed; }
.date-calendar-strip { display: flex; gap: 10px; margin-bottom: 30px; background: white; padding: 15px; border-radius: 8px; justify-content: space-between; }
.day-box { flex: 1; text-align: center; padding: 10px; border: 1px solid #eee; border-radius: 6px; cursor: pointer; transition: 0.2s; }
.day-box.active { background: #2f80ed; color: white; border-color: #2f80ed; }
.status.available { color: #28a745; }
.day-box.active .status.available { color: #aefbc0; }
.doctor-list-wrapper { background: white; border-radius: 8px; padding: 10px; min-height: 200px; }
.empty-tip { text-align: center; padding: 40px; color: #999; }
.empty-tip .empty-icon { font-size: 3rem; margin-bottom: 15px; color: #ccc; }
.empty-tip p { margin: 10px 0 0 0; font-size: 1rem; }
.loading-tip { text-align: center; padding: 40px; color: #999; }
.doctor-row { display: flex; justify-content: space-between; border-bottom: 1px solid #f0f0f0; padding: 25px; transition: 0.2s; }
.doc-left { display: flex; gap: 20px; }
.avatar { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; }
.name { font-size: 1.3rem; font-weight: bold; color: #333; }
.title { color: #666; font-size: 0.9rem; margin-left: 10px; }
.badge { background: #f0ad4e; color: white; padding: 2px 8px; border-radius: 4px; font-size: 0.8rem; margin-left: 10px; }
.skill { color: #888; font-size: 0.9rem; margin-top: 5px; max-width: 500px; }
.btn-book { background: #2f80ed; color: white; border: none; padding: 8px 25px; border-radius: 20px; cursor: pointer; }

.doctor-profile-card { background: white; padding: 30px; border-radius: 8px; display: flex; justify-content: space-between; box-shadow: 0 4px 15px rgba(0,0,0,0.05); margin-bottom: 30px; }
.profile-left { display: flex; gap: 25px; }
.profile-avatar { width: 100px; height: 100px; border-radius: 50%; object-fit: cover; border: 3px solid #eee; }
.profile-name { font-size: 1.6rem; font-weight: bold; color: #333; margin-bottom: 5px; }
.profile-title { font-size: 1rem; color: #666; font-weight: normal; margin-left: 10px; }
.btn-fav { background: white; border: 1px solid #ddd; padding: 5px 15px; border-radius: 20px; cursor: pointer; display: flex; align-items: center; gap: 5px; color: #666; }
.schedule-grid-container { background: white; border-radius: 8px; padding: 30px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
.schedule-table { width: 100%; border-collapse: collapse; text-align: center; }
.schedule-table th { background: #f8f9fa; padding: 15px; border: 1px solid #eee; }
.schedule-table td { border: 1px solid #eee; height: 80px; vertical-align: middle; }
.btn-slot { display: block; width: 80%; margin: 0 auto; background: #28a745; color: white; border: none; padding: 8px 0; border-radius: 6px; cursor: pointer; font-size: 0.9rem; transition: 0.2s; }
.btn-slot:hover { background: #218838; transform: translateY(-2px); }

/* ★★★ Step 5: 就诊人选择 样式 ★★★ */
.patient-selection-container { background: white; border-radius: 8px; padding: 30px; min-height: 400px; }
.saved-patient-list { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 30px; }
.patient-card { 
  border: 2px solid #eee; border-radius: 8px; padding: 20px; cursor: pointer; transition: 0.2s; position: relative;
  background: #fafafa;
}
.patient-card:hover { border-color: #2f80ed; box-shadow: 0 5px 15px rgba(47, 128, 237, 0.1); }
.patient-card.active { border-color: #2f80ed; background: #f0f7ff; }
.p-header { display: flex; align-items: center; margin-bottom: 10px; }
.p-name { font-size: 1.2rem; font-weight: bold; color: #333; margin-right: 10px; }
.p-tag { background: #e0e0e0; color: #666; padding: 2px 8px; border-radius: 4px; font-size: 0.75rem; }
.check-icon { margin-left: auto; color: #2f80ed; font-size: 1.4rem; }
.p-info { color: #666; font-size: 0.9rem; margin-bottom: 5px; }

.add-patient-btn { 
  border: 2px dashed #ccc; border-radius: 8px; display: flex; align-items: center; justify-content: center; 
  cursor: pointer; color: #999; font-size: 1rem; gap: 8px; min-height: 120px;
}
.add-patient-btn:hover { border-color: #2f80ed; color: #2f80ed; background: #f9fcff; }

.add-patient-form { background: #f9f9f9; padding: 30px; border-radius: 8px; margin-top: 20px; border: 1px solid #eee; }
.form-title { margin-top: 0; margin-bottom: 20px; color: #333; }
.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 0.9rem; color: #666; font-weight: bold; }
.form-group input, .form-group select { 
  padding: 10px; border: 1px solid #ddd; border-radius: 4px; outline: none; transition: 0.2s; 
}
.form-group input:focus { border-color: #2f80ed; }
.form-actions { margin-top: 20px; display: flex; gap: 15px; justify-content: flex-end; }
.btn-save-add { background: #2f80ed; color: white; border: none; padding: 10px 30px; border-radius: 4px; cursor: pointer; }
.btn-cancel-add { background: white; border: 1px solid #ccc; padding: 10px 20px; border-radius: 4px; cursor: pointer; }

.action-footer { margin-top: 40px; text-align: center; border-top: 1px solid #eee; padding-top: 30px; }
.btn-next-step { 
  background: #2f80ed; color: white; border: none; padding: 12px 60px; border-radius: 30px; 
  font-size: 1.1rem; font-weight: bold; cursor: pointer; box-shadow: 0 5px 15px rgba(47, 128, 237, 0.3);
}
.btn-next-step:disabled { background: #ccc; cursor: not-allowed; box-shadow: none; }

/* Step 6: 确认 */
.confirm-card { background: white; max-width: 600px; margin: 0 auto; border-radius: 8px; overflow: hidden; box-shadow: 0 5px 20px rgba(0,0,0,0.1); }
.card-header { background: #2f80ed; color: white; text-align: center; padding: 15px; font-size: 1.2rem; font-weight: bold; }
.card-body { padding: 30px; }
.confirm-row { display: flex; justify-content: space-between; margin-bottom: 15px; font-size: 1rem; color: #555; }
.confirm-row .val { color: #333; font-weight: 500; }
.confirm-row .val.highlight { color: #2f80ed; font-weight: bold; }
.confirm-row .val.price { color: #ff4d4f; font-size: 1.3rem; }
.divider { border-bottom: 1px dashed #ddd; margin: 20px 0; }
.card-footer { background: #f9f9f9; padding: 20px; }
.agreement { margin-bottom: 20px; text-align: center; }
.btn-group { display: flex; gap: 20px; justify-content: center; }
.btn-cancel { background: white; border: 1px solid #ccc; padding: 10px 30px; border-radius: 4px; cursor: pointer; }
.btn-confirm { background: #2f80ed; color: white; border: none; padding: 10px 50px; border-radius: 4px; font-weight: bold; cursor: pointer; }

.app-footer { background: #1a3a6e; color: rgba(255,255,255,0.6); text-align: center; padding: 20px; margin-top: 50px; }
</style>