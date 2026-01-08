<template>
    <div class="schedule-page">
      
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
  
      <div class="breadcrumb-bar">
        <div class="bar-content">
          <span @click="router.push('/user')">网站首页</span> 
          <Icon icon="mdi:chevron-right" class="sep" />
          <span>就诊指南</span>
          <Icon icon="mdi:chevron-right" class="sep" />
          <span class="current">门诊排班</span>
        </div>
        <div class="page-title-overlay">门诊排班</div>
      </div>
  
      <main class="main-content">
        <div class="content-container">
          
          <div class="filter-toolbar">
            <div class="campus-tabs">
              <div 
                v-for="c in ['朝晖院区', '屏峰院区']" 
                :key="c"
                class="campus-tab"
                :class="{ active: activeCampus === c }"
                @click="activeCampus = c"
              >
                <span class="marker" v-if="activeCampus === c"></span>
                {{ c }}
              </div>
            </div>
  
            <div class="date-navigator">
              <button class="btn-nav" @click="prevWeek"><Icon icon="mdi:chevron-left" /></button>
              <span class="date-range">{{ dateRangeText }}</span>
              <button class="btn-nav" @click="nextWeek"><Icon icon="mdi:chevron-right" /></button>
            </div>
          </div>
  
          <div class="type-search-bar">
            <div class="type-tabs">
              <div 
                v-for="t in ['全部门诊', '普通门诊', '专家门诊']" 
                :key="t" 
                class="type-btn"
                :class="{ active: activeType === t }"
                @click="activeType = t"
              >
                {{ t }}
              </div>
            </div>
            <div class="search-box">
              <Icon icon="mdi:magnify" class="search-icon"/>
              <input 
                type="text" 
                placeholder="请输入疾病/科室名称" 
                v-model="searchKeyword"
                @keyup.enter="handleSearch"
              />
              <button class="btn-search" @click="handleSearch">搜索</button>
            </div>
          </div>
  
          <div class="schedule-table-wrapper">
            <div v-if="loading" class="loading-state">
              <Icon icon="mdi:loading" class="loading-icon" />
              <p>加载中，请稍候...</p>
            </div>
            <div v-else-if="filteredScheduleData.length === 0" class="empty-state">
              <Icon icon="mdi:information-outline" class="empty-icon" />
              <p>当前筛选条件下暂无排班信息</p>
            </div>
            <table v-else class="schedule-table">
              <thead>
                <tr>
                  <th style="width: 150px">科室</th>
                  <th style="width: 80px">时段</th>
                  <th v-for="(day, idx) in weekDays" :key="idx">
                    <div class="th-date">{{ day.date }}</div>
                    <div class="th-week">{{ day.week }}</div>
                  </th>
                </tr>
              </thead>
              <tbody>
                <template v-for="dept in filteredScheduleData" :key="dept.id">
                  <tr>
                    <td rowspan="3" class="col-dept">{{ dept.name }}</td>
                    <td class="col-period">08:00-10:00</td>
                    <td v-for="(day, dIdx) in weekDays" :key="'morning'+dIdx" class="col-doc">
                      <span v-if="getDoctor(dept, day.date, 'morning')" class="doc-name">
                        {{ getDoctor(dept, day.date, 'morning') }}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td class="col-period">13:30-17:00</td>
                    <td v-for="(day, dIdx) in weekDays" :key="'afternoon'+dIdx" class="col-doc">
                      <span v-if="getDoctor(dept, day.date, 'afternoon')" class="doc-name">
                        {{ getDoctor(dept, day.date, 'afternoon') }}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td class="col-period">19:00-21:00</td>
                    <td v-for="(day, dIdx) in weekDays" :key="'evening'+dIdx" class="col-doc">
                      <span v-if="getDoctor(dept, day.date, 'evening')" class="doc-name">
                        {{ getDoctor(dept, day.date, 'evening') }}
                      </span>
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
  
        </div>
      </main>
  
      <footer class="simple-footer">
        <div class="footer-inner">
          <p>Copyright © 2025 浙江工业大学健行医院 | 浙ICP备06015436号</p>
          <p>技术支持：杭州梦智能科技有限公司</p>
        </div>
      </footer>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, onMounted, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import { Icon } from '@iconify/vue';
  import { getOutpatientSchedules } from '../api/schedule';
  import { getAllHospitals } from '../api/hospital';
  
  const router = useRouter();
  const activeCampus = ref('朝晖院区');
  const activeType = ref('全部门诊');
  const loading = ref(false);
  const searchKeyword = ref('');
  
  // 院区ID映射
  const campusIdMap = {
    '朝晖院区': '1',
    '屏峰院区': '2'
  };
  
  // 门诊类型映射
  const typeFilterMap = {
    '全部门诊': 'all',
    '普通门诊': 'normal',
    '专家门诊': 'expert'
  };
  
  // 当前日期范围（默认未来7天）
  const currentWeekStart = ref(new Date());
  const weekDays = ref([]);
  
  // 排班数据
  const scheduleData = ref([]);
  
  // 生成一周日期
  const generateWeekDays = () => {
    const days = [];
    const start = new Date(currentWeekStart.value);
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    
    for (let i = 0; i < 7; i++) {
      const date = new Date(start);
      date.setDate(start.getDate() + i);
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      days.push({
        date: `${month}.${day}`,
        fullDate: `${date.getFullYear()}-${month}-${day}`,
        week: weekdays[date.getDay()],
        dateObj: date
      });
    }
    weekDays.value = days;
  };
  
  // 加载排班数据
  const loadSchedules = async () => {
    const hospitalId = campusIdMap[activeCampus.value];
    if (!hospitalId) {
      scheduleData.value = [];
      return;
    }
    
    loading.value = true;
    try {
      const titleFilter = typeFilterMap[activeType.value] || 'all';
      const startDate = weekDays.value.length > 0 ? weekDays.value[0].fullDate : null;
      const endDate = weekDays.value.length > 0 ? weekDays.value[weekDays.value.length - 1].fullDate : null;
      
      const res = await getOutpatientSchedules(hospitalId, titleFilter, startDate, endDate);
      console.log('门诊排班API响应:', res);
      
      if (res.code === 200 && res.data) {
        // 转换数据格式以匹配前端显示
        scheduleData.value = res.data.map(dept => {
          // 为每个科室构建排班数据结构
          const scheduleMap = {};
          
          // 遍历该科室的所有医生（如果有的话）
          if (dept.doctors && dept.doctors.length > 0) {
            dept.doctors.forEach(doctor => {
              if (doctor.scheduleMap) {
                Object.keys(doctor.scheduleMap).forEach(dateKey => {
                  if (!scheduleMap[dateKey]) {
                    scheduleMap[dateKey] = { morning: '', afternoon: '', evening: '' };
                  }
                  
                  const timeSlots = doctor.scheduleMap[dateKey];
                  timeSlots.forEach(slot => {
                    // 根据 timeSlot 字段判断时间段（如 "08:00-10:00", "13:30-17:00", "19:00-21:00"）
                    let periodKey = null;
                    const timeSlot = slot.timeSlot || '';
                    
                    // 优先根据 timeSlot 字符串判断
                    if (timeSlot.includes('08:00') || timeSlot.includes('09:00') || timeSlot.includes('10:00')) {
                      // 8:00-10:00 时间段
                      periodKey = 'morning';
                    } else if (timeSlot.includes('13:30') || timeSlot.includes('14:') || timeSlot.includes('15:') || 
                               timeSlot.includes('16:') || timeSlot.includes('17:00')) {
                      // 13:30-17:00 时间段
                      periodKey = 'afternoon';
                    } else if (timeSlot.includes('19:00') || timeSlot.includes('20:') || timeSlot.includes('21:00')) {
                      // 19:00-21:00 时间段
                      periodKey = 'evening';
                    } else {
                      // 兼容旧格式：根据 period 或 timeSlot 的枚举值判断
                      if (slot.period === 'am' || timeSlot === 'AM' || timeSlot === 'am') {
                        periodKey = 'morning';
                      } else if (slot.period === 'pm' || timeSlot === 'PM' || timeSlot === 'pm') {
                        periodKey = 'afternoon';
                      } else if (slot.period === 'evening' || timeSlot === 'EVENING' || timeSlot === 'evening') {
                        periodKey = 'evening';
                      }
                    }
                    
                    if (periodKey && slot.isAvailable) {
                      // 如果有多个医生，用逗号分隔
                      if (scheduleMap[dateKey][periodKey]) {
                        scheduleMap[dateKey][periodKey] += `、${doctor.doctorName}`;
                      } else {
                        scheduleMap[dateKey][periodKey] = doctor.doctorName;
                      }
                    }
                  });
                });
              }
            });
          }
          // 如果没有医生，scheduleMap 为空对象，科室仍然会显示
          
          return {
            id: dept.departmentId,
            name: dept.departmentName,
            subName: dept.departmentName,
            schedule: scheduleMap
          };
        });
        
        console.log('转换后的排班数据:', scheduleData.value);
      } else {
        console.error('获取门诊排班失败:', res.message);
        scheduleData.value = [];
      }
    } catch (error) {
      console.error('获取门诊排班失败:', error);
      scheduleData.value = [];
    } finally {
      loading.value = false;
    }
  };
  
  // 辅助函数：获取医生名字
  const getDoctor = (dept, dateStr, period) => {
    if (dept.schedule && dept.schedule[dateStr] && dept.schedule[dateStr][period]) {
      return dept.schedule[dateStr][period];
    }
    return '';
  };
  
  // 日期导航
  const prevWeek = () => {
    const newDate = new Date(currentWeekStart.value);
    newDate.setDate(newDate.getDate() - 7);
    currentWeekStart.value = newDate;
    generateWeekDays();
    loadSchedules();
  };
  
  const nextWeek = () => {
    const newDate = new Date(currentWeekStart.value);
    newDate.setDate(newDate.getDate() + 7);
    currentWeekStart.value = newDate;
    generateWeekDays();
    loadSchedules();
  };
  
  // 格式化日期范围显示
  const dateRangeText = computed(() => {
    if (weekDays.value.length === 0) return '';
    const start = weekDays.value[0];
    const end = weekDays.value[weekDays.value.length - 1];
    return `${start.dateObj.getFullYear()}.${String(start.dateObj.getMonth() + 1).padStart(2, '0')}.${String(start.dateObj.getDate()).padStart(2, '0')} ~ ${end.dateObj.getFullYear()}.${String(end.dateObj.getMonth() + 1).padStart(2, '0')}.${String(end.dateObj.getDate()).padStart(2, '0')}`;
  });
  
  // 搜索功能
  const handleSearch = () => {
    // 前端筛选已在 computed 中实现
  };
  
  // 筛选后的数据
  const filteredScheduleData = computed(() => {
    if (!searchKeyword.value || !searchKeyword.value.trim()) {
      return scheduleData.value;
    }
    const keyword = searchKeyword.value.trim().toLowerCase();
    return scheduleData.value.filter(dept => 
      dept.name.toLowerCase().includes(keyword) || 
      dept.subName.toLowerCase().includes(keyword)
    );
  });
  
  // 监听院区和类型变化
  watch([activeCampus, activeType], () => {
    loadSchedules();
  });
  
  // 初始化
  onMounted(() => {
    generateWeekDays();
    loadSchedules();
  });
  </script>
  
  <style scoped>
  .schedule-page { min-height: 100vh; background: #fff; font-family: 'Helvetica Neue', Arial, sans-serif; }
  
  /* Header 复用 */
  .main-header { height: 80px; background: white; border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: center; }
  .header-inner { width: 100%; max-width: 1200px; padding: 0 20px; display: flex; justify-content: space-between; align-items: center; }
  .logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
  .logo-icon { font-size: 2.2rem; }
  .logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
  .logo-text small { font-size: 0.6rem; color: #666; }
  .back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
  
  /* 面包屑 (橙色背景) */
  .breadcrumb-bar { background: #f0ad4e; height: 100px; position: relative; display: flex; align-items: flex-end; padding-bottom: 20px; padding-left: calc(50vw - 600px + 20px); overflow: hidden; }
  .bar-content { color: white; font-size: 0.9rem; display: flex; align-items: center; gap: 8px; z-index: 2; position: relative; }
  .bar-content span { cursor: pointer; opacity: 0.9; }
  .bar-content .sep { font-size: 1.2rem; opacity: 0.6; }
  .page-title-overlay { position: absolute; left: calc(50vw - 600px + 20px); top: 15px; font-size: 2.5rem; color: rgba(255,255,255,0.9); font-weight: bold; }
  
  /* 内容容器 */
  .main-content { padding: 40px 0; background: #fff; }
  .content-container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
  
  /* 筛选工具栏 */
  .filter-toolbar { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 30px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
  .campus-tabs { display: flex; gap: 40px; font-size: 1.2rem; font-weight: bold; color: #666; }
  .campus-tab { cursor: pointer; padding-bottom: 10px; position: relative; display: flex; align-items: center; gap: 8px; }
  .campus-tab.active { color: #004ea2; }
  .marker { width: 10px; height: 10px; background: #004ea2; display: inline-block; border-radius: 2px; }
  
  .date-navigator { display: flex; align-items: center; gap: 15px; color: #333; font-weight: bold; font-size: 1.1rem; }
  .btn-nav { width: 30px; height: 30px; border-radius: 50%; border: 1px solid #ccc; background: white; color: #666; cursor: pointer; display: flex; align-items: center; justify-content: center; }
  .btn-nav:hover { background: #004ea2; color: white; border-color: #004ea2; }
  
  /* 类型与搜索 */
  .type-search-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
  .type-tabs { display: flex; gap: 0; }
  .type-btn { padding: 8px 25px; background: #f0f2f5; color: #666; cursor: pointer; border-right: 1px solid white; font-size: 0.9rem; transition: 0.3s; }
  .type-btn:first-child { border-radius: 4px 0 0 4px; }
  .type-btn:last-child { border-radius: 0 4px 4px 0; border-right: none; }
  .type-btn.active { background: #2f80ed; color: white; }
  
  .search-box { position: relative; display: flex; align-items: center; }
  .search-box input { width: 300px; padding: 10px 10px 10px 35px; border: 1px solid #ddd; border-radius: 30px; outline: none; }
  .search-icon { position: absolute; left: 12px; color: #999; }
  .btn-search { margin-left: 10px; background: #2f80ed; color: white; border: none; padding: 10px 25px; border-radius: 30px; cursor: pointer; }
  
  /* 表格样式 */
  .schedule-table-wrapper { border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; }
  .schedule-table { width: 100%; border-collapse: collapse; text-align: center; }
  .schedule-table thead { background: #2f80ed; color: white; }
  .schedule-table th { padding: 15px 5px; font-weight: normal; border-right: 1px solid rgba(255,255,255,0.2); }
  .th-date { font-weight: bold; font-size: 1.1rem; }
  .th-week { font-size: 0.8rem; opacity: 0.9; }
  
  .schedule-table td { border: 1px solid #eee; padding: 15px 5px; color: #333; font-size: 0.95rem; }
  .col-dept, .col-sub-dept { font-weight: bold; background: #fbfbfb; color: #333; }
  .col-period { color: #666; }
  .doc-name { display: block; cursor: pointer; font-weight: bold; }
  .doc-name:hover { color: #2f80ed; }
  
  /* Footer */
  .simple-footer { background: #1a3a6e; padding: 40px 0; color: rgba(255,255,255,0.6); font-size: 0.85rem; text-align: center; margin-top: 50px; }
  
  /* 加载和空状态 */
  .loading-state, .empty-state { text-align: center; padding: 60px 20px; color: #999; }
  .loading-icon { font-size: 3rem; animation: spin 1s linear infinite; margin-bottom: 15px; color: #2f80ed; }
  .empty-icon { font-size: 3rem; margin-bottom: 15px; color: #ccc; }
  @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
  </style>