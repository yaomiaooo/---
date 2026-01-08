<template>
  <div class="doctor-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-icon">🏥</div>
        <div class="logo-text">
          <h2>医生工作台</h2>
          <small>Doctor Station</small>
        </div>
      </div>

      <nav class="side-nav">
        <div
            v-for="item in menuItems"
            :key="item.id"
            class="nav-item"
            :class="{ active: isActive(item) }"
            @click="go(item)"
        >
          <Icon :icon="item.icon" class="nav-icon" />
          <span>{{ item.name }}</span>
        </div>
      </nav>

      <div class="sidebar-footer">
        <button class="btn-logout" @click="handleLogout">
          <Icon icon="mdi:logout" /> 退出
        </button>
      </div>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="breadcrumb">
          <span>医生端</span> / <span class="current">{{ currentTitle }}</span>
        </div>

        <div class="user-profile">
          <div class="avatar">👨‍⚕️</div>
          <div class="info">
            <span class="name">{{ doctorName }}</span>
            <span class="dept">{{ doctorDept }}</span>
          </div>
          <Icon icon="mdi:bell-outline" class="bell-icon" />
        </div>
      </header>

      <div class="work-area">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { getCurrentDoctorProfile } from '../api/doctor'

const router = useRouter()
const route = useRoute()

const doctorName = ref(localStorage.getItem('doctorName') || '医生')
const doctorDept = ref(localStorage.getItem('doctorDept') || '未设置科室')

const currentTitle = computed(() => route.meta?.title || '工作台')

// 加载医生信息
async function loadDoctorInfo() {
  try {
    const res = await getCurrentDoctorProfile()
    if (res.code === 200 && res.data) {
      const data = res.data
      // 更新响应式数据
      doctorName.value = data.doctorName || '医生'
      doctorDept.value = data.departmentName || '未设置科室'
      // 同步到localStorage
      if (data.doctorName) {
        localStorage.setItem('doctorName', data.doctorName)
      }
      if (data.departmentName) {
        localStorage.setItem('doctorDept', data.departmentName)
      }
      if (data.title) {
        localStorage.setItem('doctorTitle', data.title)
      }
    }
  } catch (error) {
    console.error('加载医生信息失败:', error)
    // 失败时使用localStorage中的默认值
  }
}

// 组件挂载时加载医生信息
onMounted(() => {
  loadDoctorInfo()
})

const menuItems = [
  { id: 'dashboard', name: '今日接诊', icon: 'mdi:monitor-dashboard', routeName: 'DoctorOverview' },
  { id: 'schedule', name: '我的排班', icon: 'mdi:calendar-clock', routeName: 'DoctorSchedule' },
  { id: 'records', name: '病历档案', icon: 'mdi:file-document-outline', routeName: 'DoctorRecords' },
  { id: 'profile', name: '个人信息', icon: 'mdi:account-cog-outline', routeName: 'DoctorProfile' },
]

function isActive(item) {
  return route.name === item.routeName
}

function go(item) {
  if (route.name !== item.routeName) {
    router.push({ name: item.routeName })
  }
}

function handleLogout() {
  if (confirm('确定要退出登录吗？')) {
    // 清除所有登录相关数据
    localStorage.removeItem('hospital_token')
    localStorage.removeItem('doctorName')
    localStorage.removeItem('doctorTitle')
    localStorage.removeItem('doctorDept')
    router.push('/login')
  }
}
</script>
  
  <style scoped>
  /* 布局容器 */
  .doctor-layout {
    display: flex;
    height: 100vh;
    width: 100vw;
    background-color: #f0f2f5;
    font-family: 'Helvetica Neue', Arial, sans-serif;
    overflow: hidden;
  }
  
  /* --- 1. 侧边栏样式 --- */
  .sidebar {
    width: 260px;
    background: white;
    display: flex;
    flex-direction: column;
    box-shadow: 2px 0 10px rgba(0,0,0,0.05);
    z-index: 10;
  }
  
  .sidebar-header {
    height: 80px;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 0 24px;
    border-bottom: 1px solid #f0f0f0;
  }
  .logo-icon { font-size: 2rem; }
  .logo-text h2 { font-size: 1.2rem; margin: 0; color: #004ea2; font-weight: 700; }
  .logo-text small { font-size: 0.7rem; color: #999; display: block; }
  
  .side-nav { flex: 1; padding: 20px 0; }
  .nav-item {
    display: flex; align-items: center; gap: 15px;
    padding: 15px 30px;
    cursor: pointer;
    color: #666;
    font-weight: 500;
    transition: 0.3s;
    border-right: 4px solid transparent;
  }
  .nav-item:hover { background: #f6f8fa; color: #004ea2; }
  .nav-item.active {
    background: #e6f7ff;
    color: #004ea2;
    border-right-color: #004ea2;
  }
  .nav-icon { font-size: 1.4rem; }
  
  .sidebar-footer {
    padding: 20px;
    border-top: 1px solid #f0f0f0;
  }
  .sys-status { font-size: 0.75rem; color: #999; margin-bottom: 10px; display: flex; align-items: center; gap: 5px; }
  .dot.green { width: 8px; height: 8px; background: #52c41a; border-radius: 50%; }
  
  .btn-logout {
    width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px;
    padding: 10px; border: 1px solid #ffdcd6; background: #fff5f5; color: #ff4d4f;
    border-radius: 6px; cursor: pointer; transition: 0.2s;
  }
  .btn-logout:hover { background: #ff4d4f; color: white; border-color: #ff4d4f; }
  
  
  /* --- 2. 主体内容样式 --- */
  .main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  
  /* 顶部 Header */
  .top-header {
    height: 64px;
    background: white;
    padding: 0 30px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 5px rgba(0,0,0,0.02);
  }
  .breadcrumb { color: #999; font-size: 0.9rem; }
  .breadcrumb .current { color: #333; font-weight: bold; }
  
  .user-profile { display: flex; align-items: center; gap: 15px; }
  .avatar { width: 40px; height: 40px; background: #e6f7ff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; }
  .info { display: flex; flex-direction: column; text-align: right; }
  .info .name { font-weight: bold; font-size: 0.9rem; color: #333; }
  .info .dept { font-size: 0.75rem; color: #999; }
  .bell-icon { font-size: 1.5rem; color: #666; cursor: pointer; margin-left: 10px; }
  
  /* 核心工作区 */
  .work-area {
    flex: 1;
    padding: 30px;
    overflow-y: auto;
  }
  
  /* 统计卡片 */
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
  
  /* 渐变色 */
  .blue-gradient { background: linear-gradient(135deg, #3b82f6, #0056b3); }
  .green-gradient { background: linear-gradient(135deg, #42e695, #3bb2b8); }
  .orange-gradient { background: linear-gradient(135deg, #f093fb, #f5576c); }
  .purple-gradient { background: linear-gradient(135deg, #5ee7df, #b490ca); }
  
  /* 列表面板 */
  .panel-container {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 5px 20px rgba(0,0,0,0.03);
    height: calc(100% - 150px); /* 撑满剩余高度 */
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
  .btn-call-next { background: #004ea2; color: white; border: none; padding: 8px 20px; border-radius: 6px; cursor: pointer; display: flex; align-items: center; gap: 5px; font-weight: bold; transition: 0.2s; }
  .btn-call-next:hover { background: #003d80; box-shadow: 0 4px 10px rgba(0,78,162,0.3); }
  
  /* 表格样式 */
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
  .type-badge.emergency { background: #fff1f0; color: #f5222d; font-weight: bold; }
  
  .status-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 5px; vertical-align: middle; }
  .status-dot.blue { background: #1890ff; }
  .status-dot.orange { background: #fa8c16; }
  .status-dot.gray { background: #d9d9d9; }
  
  .btn-action { border: none; background: none; cursor: pointer; font-size: 0.9rem; margin-right: 10px; }
  .btn-action.primary { color: #004ea2; font-weight: bold; }
  .btn-action.text { color: #999; }
  .btn-action:hover { text-decoration: underline; }
  
  </style>