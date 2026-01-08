<template>
    <div class="campus-page">
      
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
          <span>医院介绍</span>
          <Icon icon="mdi:chevron-right" class="sep" />
          <span class="current">院区介绍</span>
        </div>
        <div class="bar-shape"></div>
      </div>
  
      <div class="campus-title-section">
        <div class="container">
          <h1 class="big-title">{{ currentCampus.name }}</h1>
        </div>
      </div>
  
      <main class="main-content">
        <div class="content-container">
          
          <div class="text-article">
            <p class="lead-text">
              {{ currentCampus.desc }}
            </p>
            
            <div class="rich-text">
              <p v-for="(para, index) in currentCampus.paragraphs" :key="index">
                {{ para }}
              </p>
            </div>
  
            <div class="highlight-box" v-if="currentCampus.highlights">
               <div class="hl-title">拥有{{ currentCampus.highlights.title }}</div>
               <div class="hl-items">
                 <span v-for="item in currentCampus.highlights.items" :key="item">{{ item }}</span>
               </div>
            </div>
          </div>
  
          <div class="media-side">
            <div class="img-card">
              <img :src="currentCampus.image" alt="院区实景" />
              <div class="img-caption">院区实景图</div>
            </div>
            
            <div class="contact-card">
              <h3><Icon icon="mdi:map-marker" /> 交通指南</h3>
              <p><strong>地址：</strong>{{ currentCampus.address }}</p>
              <p><strong>电话：</strong>{{ currentCampus.phone }}</p>
              <button class="btn-map">查看电子地图</button>
            </div>
          </div>
  
        </div>
      </main>
  
      <footer class="simple-footer">
        <p>© 2025 浙江工业大学健行医院 | 智慧医疗系统设计</p>
      </footer>
  
    </div>
  </template>
  
<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { getHospitalIntro } from '../api/hospital';

const route = useRoute();
const router = useRouter();

// 获取路由参数中的 id (例如 'zhaohui' 或 'pingfeng')
const campusId = route.params.id;

// 将路由参数映射到 hospital_id
const hospitalIdMap = {
  'zhaohui': '1',  // 朝晖院区
  'pingfeng': '2'  // 屏峰院区
};

// 响应式数据
const hospitalData = ref(null);
const loading = ref(true);

// 从 hospital_intro 字段拆分段落（按句号拆分）
const splitIntoParagraphs = (introText) => {
  if (!introText) return [];
  
  // 按句号拆分，保留句号，过滤空字符串
  const paragraphs = introText
    .split(/[。！？]/)
    .map(s => s.trim())
    .filter(s => s.length > 0)
    .map(s => s + '。');
  
  // 如果拆分后没有段落，返回原文本
  if (paragraphs.length === 0) {
    return [introText];
  }
  
  return paragraphs;
};

// 获取医院介绍数据
const fetchHospitalIntro = async () => {
  const hospitalId = hospitalIdMap[campusId];
  if (!hospitalId) {
    loading.value = false;
    return;
  }
  
  try {
    loading.value = true;
    const response = await getHospitalIntro(hospitalId);
    if (response.code === 200 && response.data) {
      const hospital = response.data;
      
      // 构建院区数据
      hospitalData.value = {
        name: hospital.hospitalName || '未知院区',
        desc: '浙江工业大学健行医院成立于1984年，是浙江省卫生健康委直属的集医疗、科研、教学、预防、保健、康复于一体的大型综合性三级甲等医院。',
        address: hospital.hospitalAddress || '浙江省杭州市拱墅区潮王路158号',
        phone: hospital.hospitalPhone || '0571-88880001',
        image: 'https://images.unsplash.com/photo-1587351021759-3e566b9af9ef?q=80&w=1000',
        paragraphs: splitIntoParagraphs(hospital.hospitalIntro),
        highlights: {
          title: hospitalId === '1' ? '4个国家临床重点专科 (建设项目)' : '2个省级重点实验室',
          items: hospitalId === '1' 
            ? ['普通外科', '肿瘤科', '泌尿外科', '临床护理']
            : ['智能康复工程', '老年医学中心']
        }
      };
    }
  } catch (error) {
    console.error('获取医院介绍失败:', error);
    // 使用默认数据作为兜底
    hospitalData.value = {
      name: campusId === 'zhaohui' ? '朝晖院区' : '屏峰院区',
      desc: '暂无该院区详细介绍信息。',
      address: '未知',
      phone: '--',
      image: '',
      paragraphs: []
    };
  } finally {
    loading.value = false;
  }
};

// 组件挂载时获取数据
onMounted(() => {
  fetchHospitalIntro();
});

// 计算属性：返回当前院区数据
const currentCampus = computed(() => {
  if (loading.value) {
    return {
      name: '加载中...',
      desc: '正在加载院区信息...',
      address: '',
      phone: '',
      image: 'https://images.unsplash.com/photo-1586773860418-d37222d8fce3?q=80&w=1173&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
      paragraphs: []
    };
  }
  
  return hospitalData.value || {
    name: '未知院区',
    desc: '暂无该院区详细介绍信息。',
    address: '未知',
    phone: '--',
    image: 'https://images.unsplash.com/photo-1586773860418-d37222d8fce3?q=80&w=1173&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    paragraphs: []
  };
});
</script>
  
  <style scoped>
  .campus-page {
    min-height: 100vh;
    background: white;
    font-family: 'Helvetica Neue', Arial, sans-serif;
  }
  
  /* 顶部 Header */
  .main-header {
    height: 80px; background: white; border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: center;
  }
  .header-inner {
    width: 100%; max-width: 1200px; padding: 0 20px; display: flex; justify-content: space-between; align-items: center;
  }
  .logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
  .logo-icon { font-size: 2.2rem; }
  .logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
  .logo-text small { font-size: 0.6rem; color: #666; }
  .back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
  .back-home:hover { color: #004ea2; }
  
  /* --- 2. 橙色导航条 (核心视觉) --- */
  .breadcrumb-bar {
    background: #f0ad4e; /* 截图中的橙黄色 */
    height: 50px;
    position: relative;
    display: flex;
    align-items: center;
    padding-left: calc(50vw - 600px + 20px); /* 居中内容的左边距 */
    overflow: hidden;
  }
  .bar-content {
    color: white; font-size: 0.9rem; display: flex; align-items: center; gap: 8px; z-index: 2;
  }
  .bar-content span { cursor: pointer; opacity: 0.9; }
  .bar-content span:hover { opacity: 1; text-decoration: underline; }
  .bar-content .sep { font-size: 1.2rem; opacity: 0.6; }
  .bar-content .current { font-weight: bold; opacity: 1; }
  
  /* --- 3. 标题区域 --- */
  .campus-title-section {
    padding-top: 20px;
    border-bottom: 3px solid #004ea2; /* 标题下方的蓝线 */
  }
  .container { max-width: 1200px; margin: 0 auto; padding: 0 20px; text-align: right; /* 截图里标题靠右或很大 */ }
  
  .big-title {
    font-size: 3.5rem;
    color: #4a90e2; /* 截图中的亮蓝色 */
    margin: 0;
    font-weight: 800;
    letter-spacing: 2px;
    text-transform: uppercase;
    /* 加上一点文字描边或阴影模仿图片效果 */
    text-shadow: 2px 2px 0px rgba(0,0,0,0.05);
    line-height: 1;
    position: relative;
    top: 10px; /* 让字稍微压一点线 */
    background: white;
    display: inline-block;
    padding: 0 20px;
  }
  
  /* --- 4. 内容区域 --- */
  .main-content {
    background: #fff;
    padding: 60px 0;
  }
  .content-container {
    max-width: 1200px; margin: 0 auto; padding: 0 20px;
    display: flex; gap: 50px;
  }
  
  /* 左侧文本 */
  .text-article { flex: 2; }
  .lead-text {
    font-size: 1.1rem; font-weight: bold; color: #333; line-height: 1.8; margin-bottom: 30px;
  }
  .rich-text p {
    font-size: 1rem; color: #555; line-height: 1.8; margin-bottom: 20px; text-indent: 2em; text-align: justify;
  }
  
  /* 黄色高亮块 */
  .highlight-box {
    margin-top: 40px; text-align: center;
  }
  .hl-title {
    display: inline-block; background: #ffecb3; color: #000; font-weight: bold; padding: 8px 20px; font-size: 1.1rem; margin-bottom: 20px;
  }
  .hl-items {
    font-size: 1.2rem; font-weight: bold; color: #333; display: flex; gap: 20px; justify-content: center;
  }
  
  /* 右侧媒体 */
  .media-side { flex: 1; display: flex; flex-direction: column; gap: 30px; }
  
  .img-card img { width: 100%; border-radius: 4px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
  .img-caption { text-align: center; color: #999; font-size: 0.85rem; margin-top: 8px; }
  
  .contact-card {
    background: #f9f9f9; padding: 25px; border-radius: 8px; border-left: 4px solid #004ea2;
  }
  .contact-card h3 { margin-top: 0; color: #004ea2; font-size: 1.2rem; display: flex; align-items: center; gap: 8px; }
  .contact-card p { margin: 10px 0; font-size: 0.95rem; color: #555; }
  .btn-map {
    width: 100%; margin-top: 15px; padding: 10px; background: #004ea2; color: white; border: none; border-radius: 4px; cursor: pointer;
  }
  .btn-map:hover { background: #003d80; }
  
  .simple-footer {
    text-align: center; padding: 30px; color: #999; font-size: 0.9rem; border-top: 1px solid #eee;
  }
  
  /* 移动端适配 */
  @media (max-width: 768px) {
    .breadcrumb-bar { padding-left: 20px; }
    .content-container { flex-direction: column; }
    .big-title { font-size: 2.5rem; }
  }
  </style>