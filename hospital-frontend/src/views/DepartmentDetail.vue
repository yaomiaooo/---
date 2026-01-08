<template>
  <div class="dept-detail-page">
    
    <header class="main-header">
      <div class="header-inner">
        <div class="logo-group" @click="router.push('/user')">
          <span class="logo-icon">🏥</span>
          <div class="logo-text">
            <h1>浙江工业大学健行医院</h1>
            <small>ZHEJIANG PROVINCIAL PEOPLE'S HOSPITAL</small>
          </div>
        </div>
        <div class="back-home" @click="router.push('/department')">
          <Icon icon="mdi:arrow-left" /> 返回科室导航
        </div>
      </div>
    </header>

    <div class="breadcrumb-bar">
      <div class="bar-content">
        <span @click="router.push('/user')">网站首页</span> 
        <Icon icon="mdi:chevron-right" class="sep" />
        <span @click="router.push('/department')">科室导航</span>
        <Icon icon="mdi:chevron-right" class="sep" />
        <span class="current">{{ departmentData.departmentName || '科室详情' }}</span>
      </div>
      <div class="bar-shape"></div>
    </div>

    <div class="dept-title-section">
      <div class="container">
        <h1 class="big-title">{{ departmentData.departmentName || '加载中...' }}</h1>
      </div>
    </div>

    <main class="main-content">
      <div class="content-container">
        
        <!-- 医院介绍部分 -->
        <div class="section-card">
          <div class="section-header">
            <Icon icon="mdi:hospital-building" class="section-icon" />
            <h2>医院介绍</h2>
          </div>
          <div class="section-content">
            <p class="lead-text">
              {{ hospitalData.desc || '浙江工业大学健行医院成立于1984年，是浙江省卫生健康委直属的集医疗、科研、教学、预防、保健、康复于一体的大型综合性三级甲等医院。' }}
            </p>
            <div class="rich-text" v-if="hospitalData.paragraphs && hospitalData.paragraphs.length > 0">
              <p v-for="(para, index) in hospitalData.paragraphs" :key="index">
                {{ para }}
              </p>
            </div>
            <div class="hospital-info">
              <p><strong>院区名称：</strong>{{ hospitalData.name }}</p>
              <p><strong>地址：</strong>{{ hospitalData.address }}</p>
              <p><strong>电话：</strong>{{ hospitalData.phone }}</p>
            </div>
          </div>
        </div>

        <!-- 科室介绍部分 -->
        <div class="section-card">
          <div class="section-header">
            <Icon icon="mdi:stethoscope" class="section-icon" />
            <h2>科室介绍</h2>
          </div>
          <div class="section-content">
            <div class="dept-intro-text" v-if="departmentData.departmentIntro">
              <p v-for="(para, index) in splitIntoParagraphs(departmentData.departmentIntro)" :key="index">
                {{ para }}
              </p>
            </div>
            <div v-else class="no-intro">
              <p>暂无科室介绍信息</p>
            </div>
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
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { getDepartmentDetail, getHospitalIntro } from '../api/hospital';

const route = useRoute();
const router = useRouter();

const departmentData = ref({});
const hospitalData = ref({});
const loading = ref(true);

// 从科室介绍文本拆分段落
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

// 加载科室详情和医院信息
const loadData = async () => {
  const departmentId = route.params.id;
  console.log('开始加载科室详情，departmentId:', departmentId);
  
  if (!departmentId) {
    console.error('缺少科室ID');
    return;
  }

  loading.value = true;
  try {
    // 获取科室详情
    console.log('调用 getDepartmentDetail API，departmentId:', departmentId);
    const deptRes = await getDepartmentDetail(departmentId);
    console.log('收到科室详情响应:', deptRes);
    if (deptRes.code === 200 && deptRes.data) {
      departmentData.value = deptRes.data;
      
      // 获取医院信息
      const hospitalId = deptRes.data.hospitalId;
      if (hospitalId) {
        const hospitalRes = await getHospitalIntro(hospitalId);
        if (hospitalRes.code === 200 && hospitalRes.data) {
          const hospital = hospitalRes.data;
          
          // 构建医院数据
          hospitalData.value = {
            name: hospital.hospitalName || '未知院区',
            desc: '浙江工业大学健行医院成立于1984年，是浙江省卫生健康委直属的集医疗、科研、教学、预防、保健、康复于一体的大型综合性三级甲等医院。',
            address: hospital.hospitalAddress || '未知地址',
            phone: hospital.hospitalPhone || '--',
            paragraphs: splitIntoParagraphs(hospital.hospitalIntro)
          };
        }
      }
    } else {
      console.error('获取科室详情失败');
    }
  } catch (error) {
    console.error('加载数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 组件挂载时加载数据
onMounted(() => {
  loadData();
});
</script>

<style scoped>
.dept-detail-page {
  min-height: 100vh;
  background: white;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

/* 顶部 Header */
.main-header {
  height: 80px;
  background: white;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: center;
}
.header-inner {
  width: 100%;
  max-width: 1200px;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.logo-group {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.logo-icon {
  font-size: 2.2rem;
}
.logo-text h1 {
  margin: 0;
  font-size: 1.4rem;
  color: #004ea2;
}
.logo-text small {
  font-size: 0.6rem;
  color: #666;
}
.back-home {
  cursor: pointer;
  color: #666;
  display: flex;
  align-items: center;
  gap: 5px;
}
.back-home:hover {
  color: #004ea2;
}

/* 面包屑导航 */
.breadcrumb-bar {
  background: #f0ad4e;
  height: 50px;
  position: relative;
  display: flex;
  align-items: center;
  padding-left: calc(50vw - 600px + 20px);
  overflow: hidden;
}
.bar-content {
  color: white;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 2;
}
.bar-content span {
  cursor: pointer;
  opacity: 0.9;
}
.bar-content span:hover {
  opacity: 1;
  text-decoration: underline;
}
.bar-content .sep {
  font-size: 1.2rem;
  opacity: 0.6;
}
.bar-content .current {
  font-weight: bold;
  opacity: 1;
  cursor: default;
}
.bar-shape {
  position: absolute;
  right: 0;
  top: 0;
  border-top: 50px solid #f0ad4e;
  border-left: 50px solid transparent;
}

/* 标题区域 */
.dept-title-section {
  padding-top: 20px;
  border-bottom: 3px solid #004ea2;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: right;
}
.big-title {
  font-size: 3.5rem;
  color: #4a90e2;
  margin: 0;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 2px 2px 0px rgba(0,0,0,0.05);
  line-height: 1;
  position: relative;
  top: 10px;
  background: white;
  display: inline-block;
  padding: 0 20px;
}

/* 内容区域 */
.main-content {
  background: #fff;
  padding: 60px 0;
}
.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 卡片样式 */
.section-card {
  background: white;
  border: 1px solid #e0e6ed;
  border-radius: 12px;
  padding: 40px;
  margin-bottom: 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.section-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}
.section-icon {
  font-size: 2rem;
  color: #004ea2;
}
.section-header h2 {
  margin: 0;
  font-size: 1.8rem;
  color: #333;
}
.section-content {
  line-height: 1.8;
}
.lead-text {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}
.rich-text p {
  font-size: 1rem;
  color: #555;
  line-height: 1.8;
  margin-bottom: 15px;
  text-indent: 2em;
  text-align: justify;
}
.hospital-info {
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}
.hospital-info p {
  margin: 10px 0;
  font-size: 1rem;
  color: #555;
}
.dept-intro-text p {
  font-size: 1rem;
  color: #555;
  line-height: 1.8;
  margin-bottom: 15px;
  text-indent: 2em;
  text-align: justify;
}
.no-intro {
  text-align: center;
  padding: 40px;
  color: #999;
}

.simple-footer {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 0.9rem;
  border-top: 1px solid #eee;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .breadcrumb-bar {
    padding-left: 20px;
  }
  .big-title {
    font-size: 2.5rem;
  }
  .section-card {
    padding: 20px;
  }
}
</style>

