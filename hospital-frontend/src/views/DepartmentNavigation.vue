<template>
    <div class="dept-page">
      
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
          <div class="banner-text">
            <h1>科室导航</h1>
          </div>
        </div>
        <div class="breadcrumb-strip">
          <div class="container">
            <span @click="router.push('/user')" style="cursor:pointer">网站首页</span> 
            <Icon icon="mdi:chevron-right" />
            <span>就诊指南</span>
            <Icon icon="mdi:chevron-right" />
            <span class="current">科室导航</span>
          </div>
          <div class="strip-shape"></div>
        </div>
      </div>
  
      <div class="search-section">
        <div class="search-box-centered">
          <Icon icon="mdi:magnify" class="search-icon" />
          <input 
            type="text" 
            placeholder="请输入疾病/科室名称" 
            v-model="searchKeyword"
            @keyup.enter="handleSearch"
            @input="handleInputChange"
          />
          <button type="button" class="btn-search" @click.stop="handleSearch">搜索</button>
        </div>
      </div>
  
      <div class="campus-tabs-bar">
        <div class="tabs-inner">
          <div 
            v-for="campus in ['朝晖院区', '屏峰院区']" 
            :key="campus"
            class="tab-item"
            :class="{ active: activeCampus === campus }"
            @click="activeCampus = campus; loadDepartmentData()"
          >
            {{ campus }}
            <div class="triangle" v-if="activeCampus === campus"></div>
          </div>
        </div>
      </div>
  
      <main class="main-content">
        <div class="content-container">
          
          <div class="dept-category-group">
            <div class="category-header">
              <Icon icon="mdi:stethoscope" class="cat-icon" />
              <h2>临床科室</h2>
            </div>
            
            <div class="dept-grid" v-if="displayedDepts.length > 0">
              <div 
                v-for="dept in displayedDepts" 
                :key="dept.departmentId || dept" 
                class="dept-card"
                @click="handleDeptClick(dept)"
              >
                {{ dept.departmentName || dept }}
              </div>
            </div>
            <div v-else class="no-result">
              <Icon icon="mdi:information-outline" />
              <p>未找到相关科室，请尝试其他关键词</p>
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
              <div class="addr-item"><h4>望江山院区</h4><p>地址：杭州市西湖区望江山路1号</p></div>
            </div>
          </div>
          <div class="footer-col col-mid">
            <h3 class="footer-title">托管医院</h3>
            <ul class="footer-link-list">
              <li><span class="dot">●</span> 浙江工业大学健行医院淳安分院</li>
              <li><span class="dot">●</span> 浙江工业大学健行医院天台分院</li>
            </ul>
          </div>
          <div class="footer-col col-right">
            <h3 class="footer-title">卫生系统网站</h3>
            <ul class="footer-link-list">
              <li><span class="dot">●</span> 国家卫生健康委员会</li>
              <li><span class="dot">●</span> 浙江省卫生健康委员会</li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom-bar">
          Copyright © 2025 浙江工业大学健行医院网站版权所有 | 浙ICP备06015436号
        </div>
      </footer>
  
      <div class="side-anchor-nav">
        <div class="anchor-item active">
          <Icon icon="mdi:stethoscope" /> 临床科室
        </div>
        <div class="anchor-item">
          <Icon icon="mdi:microscope" /> 医技科室
        </div>
        <div class="anchor-item back-top" @click="scrollToTop">
          <Icon icon="mdi:arrow-up" /> 返回顶部
        </div>
      </div>
  
    </div>
  </template>
  
<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { getDepartmentList } from '../api/hospital';

const router = useRouter();
const activeCampus = ref('朝晖院区');
const searchKeyword = ref('');
const allDepartments = ref([]);
const loading = ref(false);

// 院区ID映射
const campusIdMap = {
  '朝晖院区': '1',
  '屏峰院区': '2'
};

// 计算属性：直接显示从后端获取的科室数据（后端已处理搜索）
const displayedDepts = computed(() => {
  return allDepartments.value;
});

// 加载科室数据
const loadDepartmentData = async () => {
  const hospitalId = campusIdMap[activeCampus.value];
  if (!hospitalId) {
    console.warn('未找到院区ID映射:', activeCampus.value);
    return;
  }
  
  loading.value = true;
  try {
    // 如果有搜索关键词，传递给后端进行搜索
    const keyword = searchKeyword.value && searchKeyword.value.trim() 
      ? searchKeyword.value.trim() 
      : null;
    
    console.log('开始加载科室数据，hospitalId:', hospitalId, 'keyword:', keyword);
    const res = await getDepartmentList(hospitalId, keyword);
    console.log('收到响应:', res);
    
    if (res.code === 200 && res.data) {
      allDepartments.value = res.data;
      console.log('成功加载科室数据，数量:', res.data.length);
    } else {
      console.warn('响应数据异常:', res);
      allDepartments.value = [];
    }
  } catch (error) {
    console.error('获取科室数据失败:', error);
    allDepartments.value = [];
  } finally {
    loading.value = false;
  }
};

// 输入框变化处理（可选：实时搜索）
const handleInputChange = () => {
  // 可以在这里实现实时搜索，或者留空
};

// 搜索处理
const handleSearch = (e) => {
  if (e) {
    e.preventDefault();
    e.stopPropagation();
  }
  console.log('搜索按钮被点击，关键词:', searchKeyword.value);
  loadDepartmentData();
};

const handleDeptClick = (dept) => {
  console.log('点击科室:', dept);
  // dept 应该是从数据库加载的对象，包含 departmentId
  if (dept && dept.departmentId) {
    const departmentId = dept.departmentId;
    console.log('准备跳转到科室详情页，departmentId:', departmentId);
    // 跳转到科室详情页
    router.push(`/department/${departmentId}`).catch(err => {
      console.error('路由跳转失败:', err);
    });
  } else {
    console.warn('科室数据格式错误，缺少 departmentId:', dept);
    console.warn('科室对象内容:', JSON.stringify(dept, null, 2));
  }
};

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// 组件挂载时加载数据
onMounted(() => {
  loadDepartmentData();
});
</script>
  
  <style scoped>
  .dept-page { min-height: 100vh; background: #fff; font-family: 'Helvetica Neue', Arial, sans-serif; padding-bottom: 0; }
  
  /* Header 复用 */
  .main-header { height: 80px; background: white; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #ddd; }
  .header-inner { width: 100%; max-width: 1400px; padding: 0 40px; display: flex; justify-content: space-between; align-items: center; }
  .logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
  .logo-icon { font-size: 2.2rem; }
  .logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
  .logo-text small { font-size: 0.6rem; color: #666; }
  .back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
  
  /* Banner + 面包屑 */
  .top-banner-section { background: white; }
  .banner-bg {
    height: 200px;
    background: linear-gradient(rgba(0,0,0,0.2), rgba(0,0,0,0.2)), url('https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?q=80&w=2000&auto=format&fit=crop');
    background-size: cover; background-position: center;
    display: flex; align-items: center; padding-left: 10%;
  }
  .banner-text h1 { color: white; font-size: 2.5rem; text-shadow: 2px 2px 4px rgba(0,0,0,0.5); }
  .breadcrumb-strip {
    background: #f0ad4e; height: 50px; display: flex; align-items: center; position: relative; overflow: hidden;
    padding-left: 10%; color: white; font-size: 0.95rem;
  }
  .breadcrumb-strip .container { display: flex; align-items: center; gap: 10px; z-index: 2; }
  .breadcrumb-strip .current { font-weight: bold; }
  .strip-shape { position: absolute; right: 0; top: 0; border-top: 50px solid #f0ad4e; border-left: 50px solid transparent; }
  
  /* 搜索区 (简洁居中) */
  .search-section { padding: 30px 0; display: flex; justify-content: center; background: #fff; }
  .search-box-centered { 
    position: relative; 
    width: 600px; 
    display: flex; 
    align-items: center; 
    z-index: 1; /* 确保搜索框在最上层 */
  }
  .search-box-centered input {
    width: 100%; 
    padding: 12px 120px 12px 45px; /* 右侧留出按钮空间 */
    border: 1px solid #ddd; 
    border-radius: 30px; 
    outline: none; 
    font-size: 1rem; 
    transition: 0.3s;
    position: relative;
    z-index: 1;
  }
  .search-box-centered input:focus { border-color: #2f80ed; box-shadow: 0 0 0 3px rgba(47, 128, 237, 0.1); }
  .search-icon { 
    position: absolute; 
    left: 15px; 
    color: #999; 
    font-size: 1.2rem; 
    z-index: 2;
    pointer-events: none; /* 图标不拦截点击 */
  }
  .btn-search {
    position: absolute; 
    right: 5px; 
    top: 5px; 
    bottom: 5px;
    background: #2385fc; 
    color: white; 
    border: none; 
    padding: 0 30px; 
    border-radius: 25px; 
    cursor: pointer; 
    font-weight: bold;
    z-index: 3; /* 确保按钮在最上层 */
    pointer-events: auto; /* 确保可以点击 */
    transition: background 0.2s;
  }
  .btn-search:hover { background: #1a6cdb; }
  .btn-search:active { background: #1556a8; transform: scale(0.98); }
  
  /* Tabs */
  .campus-tabs-bar { background: #0e5ebd; height: 60px; /* 深蓝色背景 */ }
  .tabs-inner { max-width: 1400px; margin: 0 auto; padding: 0 40px; height: 100%; display: flex; }
  .tab-item { 
    color: rgba(255,255,255,0.8); padding: 0 40px; font-size: 1.1rem; font-weight: bold; cursor: pointer; position: relative; height: 100%; display: flex; align-items: center; transition: 0.3s;
  }
  .tab-item:hover { color: white; background: rgba(255,255,255,0.1); }
  .tab-item.active { color: white; background: #2385fc; /* 激活态亮蓝 */ }
  .triangle { position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 0; height: 0; border-left: 8px solid transparent; border-right: 8px solid transparent; border-bottom: 8px solid #fff; /* 改为白色三角，或者指向内容的颜色 */ }
  
  /* 内容区 */
  .main-content { padding: 60px 0; background: #fff; min-height: 500px; }
  .content-container { max-width: 1400px; margin: 0 auto; padding: 0 40px; }
  
  .category-header { display: flex; align-items: center; gap: 10px; margin-bottom: 30px; border-bottom: 2px solid #f0f0f0; padding-bottom: 15px; }
  .category-header h2 { margin: 0; font-size: 1.8rem; color: #333; }
  .cat-icon { font-size: 2.2rem; color: #2385fc; }
  .category-header.green .cat-icon { color: #28a745; }
  
  .dept-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 20px; }
  .dept-card {
    background: #f8f9fa; border: 1px solid #eee; padding: 20px; text-align: center; font-size: 1.1rem; color: #555; border-radius: 6px; cursor: pointer; transition: 0.2s;
  }
  .dept-card:hover { background: #fff; color: #2385fc; border-color: #2385fc; box-shadow: 0 5px 15px rgba(35, 133, 252, 0.15); transform: translateY(-3px); font-weight: bold; }
  
  /* 无结果提示 */
  .no-result {
    text-align: center; padding: 60px 20px; color: #999;
    display: flex; flex-direction: column; align-items: center; gap: 15px;
  }
  .no-result .iconify { font-size: 3rem; opacity: 0.5; }
  .no-result p { margin: 0; font-size: 1.1rem; }
  
  /* 悬浮楼层导航 */
  .side-anchor-nav { position: fixed; right: 30px; bottom: 150px; display: flex; flex-direction: column; gap: 10px; z-index: 100; }
  .anchor-item {
    width: 60px; height: 60px; background: white; border: 1px solid #ddd; border-radius: 8px; color: #666;
    display: flex; flex-direction: column; align-items: center; justify-content: center; font-size: 0.75rem; cursor: pointer; transition: 0.3s;
    box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  }
  .anchor-item:hover, .anchor-item.active { background: #2385fc; color: white; border-color: #2385fc; }
  .anchor-item .iconify { font-size: 1.5rem; margin-bottom: 3px; }
  
  /* Footer */
  .app-footer { position: relative; width: 100%; background-color: #1a3a6e; color: white; padding-top: 60px; overflow: hidden; margin-top: 50px; }
  .footer-bg-image { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(to top, rgba(26, 58, 110, 0.95), rgba(43, 90, 165, 0.8)), url('https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?q=80&w=2000&auto=format&fit=crop'); background-size: cover; opacity: 0.6; }
  .footer-content { position: relative; z-index: 1; max-width: 1400px; margin: 0 auto; padding: 0 40px; display: flex; justify-content: space-between; gap: 40px; padding-bottom: 40px; }
  .footer-col { flex: 1; }
  .col-left { flex: 1.2; }
  .footer-logos { margin-bottom: 30px; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 20px; }
  .logo-placeholder { display: flex; align-items: center; gap: 15px; }
  .logo-ico { font-size: 3.5rem; }
  .logo-txt h3 { margin: 0; font-size: 1.5rem; }
  .addr-item { margin-bottom: 15px; }
  .addr-item h4 { margin: 0 0 5px 0; font-size: 1rem; color: #fff; }
  .addr-item p { margin: 0; font-size: 0.85rem; color: rgba(255,255,255,0.7); }
  .footer-title { font-size: 1.1rem; font-weight: bold; margin-bottom: 20px; border-left: 3px solid #00b0f0; padding-left: 10px; }
  .footer-link-list { list-style: none; padding: 0; }
  .footer-link-list li { margin-bottom: 10px; color: rgba(255,255,255,0.8); cursor: pointer; display: flex; align-items: center; gap: 8px; }
  .footer-bottom-bar { position: relative; z-index: 2; background: #132b52; text-align: center; padding: 20px; font-size: 0.8rem; color: rgba(255,255,255,0.5); }
  
  @media(max-width: 1200px) { .dept-grid { grid-template-columns: repeat(3, 1fr); } }
  </style>