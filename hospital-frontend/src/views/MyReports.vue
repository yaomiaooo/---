<template>
    <div class="report-page">
      
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
          <div class="banner-text"><h1>预约记录</h1></div>
        </div>
        <div class="breadcrumb-strip">
          <div class="container">
            <span @click="router.push('/user')" style="cursor: pointer">网站首页</span> 
            <Icon icon="mdi:chevron-right" />
            <span>就诊指南</span>
            <Icon icon="mdi:chevron-right" />
            <span class="current">预约记录</span>
          </div>
          <div class="strip-shape"></div>
        </div>
      </div>
  
      <main class="main-content">
        <div class="content-container">
          
          <div class="filter-toolbar">
            <div class="filter-left">
              <div class="filter-item">
                <label>就诊人：</label>
                <select v-model="filters.patient">
                  <option value="all">全部就诊人</option>
                  <option v-for="p in patients" :key="p.id" :value="p.name">{{ p.name }}</option>
                </select>
              </div>
              <div class="filter-item">
                <label>院区：</label>
                <select v-model="filters.campus">
                  <option value="all">全部院区</option>
                  <option value="朝晖院区">朝晖院区</option>
                  <option value="屏峰院区">屏峰院区</option>
                </select>
              </div>
            </div>
            <div class="filter-right">
              <div class="search-input">
                <Icon icon="mdi:magnify" class="s-icon" />
                <input type="text" v-model="filters.keyword" placeholder="搜索科室/医生姓名...">
              </div>
              <button class="btn-query">查询</button>
            </div>
          </div>
  
          <div class="status-tabs">
            <div 
              v-for="tab in tabs" 
              :key="tab.key"
              class="tab-item"
              :class="{ active: currentTab === tab.key }"
              @click="currentTab = tab.key"
            >
              {{ tab.name }}
              <span class="count" v-if="tab.count > 0">{{ tab.count }}</span>
            </div>
          </div>
  
          <div class="record-list-area">
            
            <div v-if="loading" class="empty-state">
              <img src="https://cdn-icons-png.flaticon.com/512/7486/7486754.png" alt="Loading" class="empty-img">
              <p>加载中，请稍候...</p>
            </div>
            
            <div v-else-if="filteredRecords.length === 0" class="empty-state">
              <img src="https://cdn-icons-png.flaticon.com/512/7486/7486754.png" alt="No Data" class="empty-img">
              <p>未查询到相关预约记录</p>
              <button class="btn-go-book" @click="router.push('/appointment')">去预约挂号</button>
            </div>
  
            <div v-else class="record-grid">
              <div v-for="record in filteredRecords" :key="record.id" class="record-card">
                <div class="card-header">
                  <span class="campus-tag">{{ record.campus }}</span>
                  <span class="record-time">{{ record.time }}</span>
                  <span class="status-badge" :class="getStatusClass(record.status)">{{ record.status }}</span>
                </div>
                <div class="card-body">
                  <div class="doctor-info">
                    <div class="doc-name">{{ record.doctor }} <small>{{ record.title }}</small></div>
                    <div class="dept-name">{{ record.dept }}</div>
                  </div>
                  <div class="patient-info">
                    <span class="label">就诊人：</span>
                    <span class="val">{{ record.patientName }}</span>
                  </div>
                  <div class="price-info">
                    <span class="label">挂号费：</span>
                    <span class="val">￥{{ record.price }}</span>
                  </div>
                </div>
                <div class="card-footer">
                  <button class="btn-outline" v-if="record.status === '已就诊'">查看报告</button>
                  <button class="btn-outline" v-if="record.status === '已就诊'">电子发票</button>
                  
                  <button class="btn-outline" v-if="record.status === '未就诊'" @click="handleCancelAppointment(record)">取消预约</button>
                  <button class="btn-primary" v-if="record.status === '未就诊'">去支付</button>
                  
                  <button class="btn-disabled" v-if="record.status === '已过期' || record.status === '已取消'" disabled>重新挂号</button>
                </div>
              </div>
            </div>
  
          </div>
  
        </div>
      </main>
  
      <footer class="app-footer">
        <div class="footer-bg-image"></div>
        <div class="footer-content">
          <div class="footer-col col-left">
            <div class="footer-logos">
              <div class="logo-placeholder">
                <Icon icon="mdi:hospital-building" class="logo-ico" />
                <div class="logo-txt">
                  <h3>浙江工业大学健行医院</h3>
                  <small>ZHEJIANG PROVINCIAL PEOPLE'S HOSPITAL</small>
                </div>
              </div>
            </div>
            <div class="address-list">
              <div class="addr-item"><h4>朝晖院区</h4><p>地址：杭州市上塘路158号</p></div>
              <div class="addr-item"><h4>屏峰院区</h4><p>地址：杭州市西湖区留和路288号</p></div>
            </div>
          </div>
          <div class="footer-col col-mid">
            <h3 class="footer-title">托管医院</h3>
            <ul class="footer-link-list">
              <li><span class="dot">●</span> 浙江工业大学健行医院淳安分院</li>
              <li><span class="dot">●</span> 浙江工业大学健行医院天台分院</li>
              <li><span class="dot">●</span> 浙江工业大学健行医院浙东南院区</li>
            </ul>
          </div>
          <div class="footer-col col-right">
            <h3 class="footer-title">卫生系统网站</h3>
            <ul class="footer-link-list">
              <li><span class="dot">●</span> 国家卫生健康委员会</li>
              <li><span class="dot">●</span> 浙江省卫生健康委员会</li>
            </ul>
            <div class="copyright-text">
              <p>Copyright © 2025 浙江工业大学健行医院网站版权所有</p>
            </div>
          </div>
        </div>
      </footer>
  
    </div>
  </template>
  
  <script setup>
  import { ref, computed, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { Icon } from '@iconify/vue';
  import { getMyAppointments, cancelAppointment } from '../api/appointment';
  import { getMyPatients } from '../api/patient';
  
  const router = useRouter();
  
  // --- 状态管理 ---
  const currentTab = ref('all');
  const loading = ref(false);
  const filters = ref({
    patient: 'all',
    campus: 'all',
    keyword: ''
  });
  
  // 动态加载的就诊人列表
  const patients = ref([]);
  
  // 动态加载的预约记录
  const allRecords = ref([]);
  
  // 动态计算 tab 的 count
  const tabs = computed(() => {
    const allCount = allRecords.value.length;
    const unvisitedCount = allRecords.value.filter(r => r.status === '未就诊').length;
    const visitedCount = allRecords.value.filter(r => r.status === '已就诊').length;
    const canceledCount = allRecords.value.filter(r => r.status === '已取消').length;
    const expiredCount = allRecords.value.filter(r => r.status === '已过期').length;
    
    return [
      { key: 'all', name: '全部', count: allCount },
      { key: 'unvisited', name: '未就诊', count: unvisitedCount },
      { key: 'visited', name: '已就诊', count: visitedCount },
      { key: 'canceled', name: '已取消', count: canceledCount },
      { key: 'expired', name: '已过期', count: expiredCount },
    ];
  });
  
  // 加载就诊人列表
  const loadPatients = async () => {
    try {
      const res = await getMyPatients();
      if (res.code === 200 && res.data) {
        patients.value = res.data.map(p => ({
          id: p.patientId,
          name: p.name
        }));
      }
    } catch (error) {
      console.error('获取就诊人列表失败:', error);
      patients.value = [];
    }
  };
  
  // 加载预约记录
  const loadAppointments = async () => {
    loading.value = true;
    try {
      const res = await getMyAppointments();
      console.log('预约记录API响应:', res);
      if (res.code === 200 && res.data) {
        // 转换数据格式以匹配前端显示
        allRecords.value = res.data.map(appointment => {
          // 格式化时间：将日期和时间段组合
          const timeStr = appointment.visitDate 
            ? `${appointment.visitDate} ${appointment.timeSlot || ''}`.trim()
            : (appointment.createdAt ? appointment.createdAt.split('T')[0] : '');
          
          // 格式化价格
          const priceStr = appointment.price 
            ? appointment.price.toFixed(2) 
            : '30.00';
          
          return {
            id: appointment.appointmentId,
            campus: appointment.hospitalName || '未知院区',
            time: timeStr,
            status: appointment.status || '未知',
            doctor: appointment.doctorName || '未知',
            title: appointment.doctorTitle || '',
            dept: appointment.departmentName || '未知科室', // 从医生职称或简介中提取的科室名称
            patientName: appointment.patientName || '未知',
            price: priceStr
          };
        });
        console.log('转换后的预约记录列表:', allRecords.value);
      } else {
        console.error('获取预约记录失败:', res.message);
        allRecords.value = [];
      }
    } catch (error) {
      console.error('获取预约记录失败:', error);
      allRecords.value = [];
    } finally {
      loading.value = false;
    }
  };
  
  const filteredRecords = computed(() => {
    return allRecords.value.filter(item => {
      if (currentTab.value !== 'all') {
        if (currentTab.value === 'unvisited' && item.status !== '未就诊') return false;
        if (currentTab.value === 'visited' && item.status !== '已就诊') return false;
        if (currentTab.value === 'canceled' && item.status !== '已取消') return false;
        if (currentTab.value === 'expired' && item.status !== '已过期') return false;
      }
      if (filters.value.patient !== 'all' && item.patientName !== filters.value.patient) return false;
      if (filters.value.campus !== 'all' && item.campus !== filters.value.campus) return false;
      if (filters.value.keyword && !item.dept.includes(filters.value.keyword) && !item.doctor.includes(filters.value.keyword)) return false;
      return true;
    });
  });
  
  const getStatusClass = (status) => {
    if (status === '未就诊') return 'status-blue';
    if (status === '已就诊') return 'status-green';
    if (status === '已取消') return 'status-gray';
    if (status === '已过期') return 'status-dark';
    return '';
  };
  
  // 取消预约
  const handleCancelAppointment = async (record) => {
    if (!confirm(`确定要取消预约吗？\n就诊人：${record.patientName}\n医生：${record.doctor}\n时间：${record.time}`)) {
      return;
    }
    
    loading.value = true;
    try {
      const res = await cancelAppointment(record.id);
      if (res.code === 200) {
        alert('预约已取消');
        // 重新加载预约记录列表
        await loadAppointments();
      } else {
        alert(res.message || '取消预约失败，请重试');
      }
    } catch (error) {
      console.error('取消预约失败:', error);
      alert(error.message || '取消预约失败，请检查网络连接');
    } finally {
      loading.value = false;
    }
  };
  
  // 初始化加载数据
  onMounted(() => {
    loadPatients();
    loadAppointments();
  });
  </script>
  
  <style scoped>
  /* 基础布局复用 */
  .report-page { min-height: 100vh; background: #f4f6f9; font-family: 'Helvetica Neue', Arial, sans-serif; display: flex; flex-direction: column; }
  .main-header { height: 80px; background: white; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #ddd; }
  .header-inner { width: 100%; max-width: 1200px; padding: 0 40px; display: flex; justify-content: space-between; align-items: center; }
  .logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
  .logo-icon { font-size: 2.2rem; }
  .logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
  .logo-text small { font-size: 0.6rem; color: #666; }
  .back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
  
  /* Banner */
  .top-banner-section { background: white; }
  .banner-bg { height: 160px; background: linear-gradient(rgba(0,0,0,0.6), rgba(0,0,0,0.6)), url('https://images.unsplash.com/photo-1516549655169-df83a09295ba?q=80&w=2000'); background-size: cover; background-position: center; display: flex; align-items: center; padding-left: 10%; }
  .banner-text h1 { color: white; font-size: 2.2rem; }
  .breadcrumb-strip { background: #f0ad4e; height: 50px; display: flex; align-items: center; position: relative; padding-left: 10%; color: white; }
  .breadcrumb-strip .container { display: flex; align-items: center; gap: 10px; z-index: 2; }
  .strip-shape { position: absolute; right: 0; top: 0; border-top: 50px solid #f0ad4e; border-left: 50px solid transparent; }
  
  /* 主体内容 */
  .main-content { flex: 1; padding: 40px 0; }
  .content-container { max-width: 1200px; margin: 0 auto; padding: 0 40px; }
  
  /* 筛选工具栏 */
  .filter-toolbar { background: white; padding: 20px; border-radius: 8px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 10px rgba(0,0,0,0.03); margin-bottom: 20px; }
  .filter-left { display: flex; gap: 30px; }
  .filter-item { display: flex; align-items: center; gap: 10px; font-size: 0.95rem; color: #666; }
  .filter-item select { padding: 8px 15px; border: 1px solid #ddd; border-radius: 4px; outline: none; min-width: 150px; color: #333; }
  .filter-right { display: flex; gap: 15px; }
  .search-input { position: relative; width: 250px; }
  .search-input input { width: 100%; padding: 8px 10px 8px 35px; border: 1px solid #ddd; border-radius: 20px; outline: none; }
  .s-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #999; }
  .btn-query { background: #004ea2; color: white; border: none; padding: 8px 25px; border-radius: 20px; cursor: pointer; transition: 0.2s; }
  .btn-query:hover { background: #003d80; }
  
  /* Tabs */
  .status-tabs { display: flex; margin-bottom: 20px; border-bottom: 2px solid #e0e0e0; }
  .tab-item { padding: 12px 30px; cursor: pointer; font-weight: bold; color: #666; position: relative; top: 2px; transition: 0.2s; display: flex; align-items: center; gap: 5px; }
  .tab-item:hover { color: #004ea2; }
  .tab-item.active { color: #004ea2; border-bottom: 3px solid #004ea2; background: white; border-radius: 8px 8px 0 0; }
  .tab-item .count { background: #ff4d4f; color: white; font-size: 0.7rem; padding: 1px 6px; border-radius: 10px; font-weight: normal; }
  
  /* 记录列表 */
  .record-list-area { min-height: 400px; }
  
  /* 空状态 */
  .empty-state { text-align: center; padding: 60px 0; }
  .empty-img { width: 150px; margin-bottom: 20px; opacity: 0.8; }
  .empty-state p { color: #999; margin-bottom: 20px; }
  .btn-go-book { background: white; border: 1px solid #004ea2; color: #004ea2; padding: 10px 30px; border-radius: 20px; cursor: pointer; font-weight: bold; transition: 0.2s; }
  .btn-go-book:hover { background: #eef6ff; }
  
  /* 记录卡片 */
  .record-grid { display: flex; flex-direction: column; gap: 20px; }
  .record-card { background: white; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid #eee; transition: 0.2s; }
  .record-card:hover { transform: translateY(-3px); box-shadow: 0 8px 25px rgba(0,0,0,0.08); border-color: #004ea2; }
  
  .card-header { background: #fcfcfc; padding: 15px 25px; border-bottom: 1px solid #eee; display: flex; align-items: center; gap: 15px; font-size: 0.9rem; }
  .campus-tag { background: #e3f2fd; color: #004ea2; padding: 2px 8px; border-radius: 4px; font-size: 0.8rem; }
  .record-time { color: #666; font-weight: bold; flex: 1; }
  .status-badge { font-size: 0.85rem; font-weight: bold; }
  
  /* 状态颜色 */
  .status-blue { color: #004ea2; }
  .status-green { color: #28a745; }
  .status-gray { color: #999; }
  .status-dark { color: #666; background: #eee; padding: 2px 8px; border-radius: 4px; }
  
  .card-body { padding: 20px 25px; display: flex; justify-content: space-between; align-items: center; }
  .doctor-info { flex: 2; }
  .doc-name { font-size: 1.2rem; font-weight: bold; color: #333; margin-bottom: 5px; }
  .doc-name small { font-size: 0.85rem; color: #666; font-weight: normal; margin-left: 5px; }
  .dept-name { color: #666; font-size: 0.95rem; }
  .patient-info { flex: 1; color: #666; font-size: 0.95rem; text-align: center; }
  .price-info { flex: 1; text-align: right; }
  .price-info .val { font-size: 1.3rem; color: #ff4d4f; font-weight: bold; font-family: Arial; }
  
  .card-footer { padding: 15px 25px; border-top: 1px dashed #eee; display: flex; justify-content: flex-end; gap: 12px; }
  .btn-outline { background: white; border: 1px solid #ccc; color: #666; padding: 6px 18px; border-radius: 4px; cursor: pointer; transition: 0.2s; font-size: 0.9rem; }
  .btn-outline:hover { border-color: #004ea2; color: #004ea2; }
  .btn-primary { background: #004ea2; border: 1px solid #004ea2; color: white; padding: 6px 18px; border-radius: 4px; cursor: pointer; transition: 0.2s; font-size: 0.9rem; }
  .btn-primary:hover { background: #003d80; }
  .btn-disabled { background: #f5f5f5; border: 1px solid #ddd; color: #aaa; padding: 6px 18px; border-radius: 4px; cursor: not-allowed; }
  
  /* Footer */
  .app-footer { position: relative; width: 100%; background-color: #1a3a6e; color: white; padding-top: 60px; overflow: hidden; margin-top: auto; }
  .footer-bg-image { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(to top, rgba(26, 58, 110, 0.95), rgba(43, 90, 165, 0.8)), url('https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?q=80&w=2000&auto=format&fit=crop'); background-size: cover; opacity: 0.6; }
  .footer-content { position: relative; z-index: 1; max-width: 1400px; margin: 0 auto; padding: 0 40px; display: flex; justify-content: space-between; gap: 40px; padding-bottom: 40px; }
  .footer-col { flex: 1; }
  .col-left { flex: 1.2; }
  .footer-logos { margin-bottom: 30px; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 20px; }
  .logo-placeholder { display: flex; align-items: center; gap: 15px; }
  .logo-ico { font-size: 3.5rem; }
  .logo-txt h3 { margin: 0; font-size: 1.5rem; }
  .addr-item { margin-bottom: 15px; }
  .footer-title { font-size: 1.1rem; font-weight: bold; margin-bottom: 20px; border-left: 3px solid #00b0f0; padding-left: 10px; }
  .footer-link-list { list-style: none; padding: 0; }
  .footer-link-list li { margin-bottom: 10px; color: rgba(255,255,255,0.8); cursor: pointer; }
  
  @media(max-width: 900px) {
    .filter-toolbar { flex-direction: column; gap: 15px; align-items: flex-start; }
    .filter-left, .filter-right { width: 100%; flex-wrap: wrap; }
    .card-body { flex-direction: column; align-items: flex-start; gap: 10px; }
    .patient-info, .price-info { text-align: left; }
    .footer-content { flex-direction: column; }
  }
  </style>