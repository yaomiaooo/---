<template>
    <div class="page-wrapper">
      
      <header class="header-section">
        <div class="header-inner">
          <div class="logo-group" @click="router.push('/user')">
            <div class="logo-icon">🏥</div>
            <div class="logo-text">
              <h1>浙江工业大学健行医院</h1>
              <small>ZHEJIANG PROVINCIAL PEOPLE'S HOSPITAL</small>
            </div>
          </div>
          <nav class="nav-links">
            <a @click="router.push('/user')">首页</a>
            <a class="active">医院介绍</a> <a>党建工作</a>
            <a>就医指南</a>
            <a>人才招聘</a>
          </nav>
        </div>
      </header>
  
      <div class="intro-banner" :style="bannerStyle">
        <div class="banner-text">
          <h1>医院介绍</h1>
          <p>Hospital Introduction</p>
        </div>
      </div>
  
      <main class="main-container">
        
        <section class="overview-section">
          <h2 class="section-title">医院概况</h2>
          
          <div class="content-row">
            <div class="text-col">
              <div class="text-wrapper">
                <h3 class="hospital-name">浙江工业大学健行医院</h3>
                <p class="intro-p">
                  浙江工业大学健行医院成立于1984年，是浙江省卫生健康委直属的集医疗、科研、教学、预防、保健、康复于一体的大型综合性三级甲等医院。
                  目前有朝晖和屏峰2个已运行院区。
                </p>
                <p class="intro-p">
                  目前开放床位3600张，在职职工4578人，其中高级职称800余人。
                  医院学科齐全、设备先进、技术雄厚，以“院有品牌、科有特色、人有专长”享誉省内外。
                </p>
              </div>
            </div>
  
            <div class="img-col">
              <img src="https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?q=80&w=1000&auto=format&fit=crop" alt="医院大楼" />
            </div>
          </div>
        </section>
  
        <section class="honor-section">
          
          <div class="honor-item">
            <div class="highlight-title">拥有4个国家临床重点专科 (建设项目)</div>
            <div class="honor-list">
              <span>普通外科</span>
              <span>肿瘤科</span>
              <span>泌尿外科</span>
              <span>临床护理</span>
            </div>
          </div>
  
          <div class="honor-item">
            <div class="highlight-title">3个国家临床医学研究中心浙江省分中心</div>
            <div class="honor-list">
              <span>恶性肿瘤</span>
              <span>神经系统疾病</span>
              <span>慢性肾病</span>
            </div>
          </div>
  
          <div class="honor-item">
            <div class="highlight-title">10余个省级重点科创平台</div>
            <p class="honor-desc">
              省头颈部恶性肿瘤临床医学研究中心、全省头颈肿瘤精准医学研究重点实验室、
              全省眼病精准医疗重点实验室、乳腺癌上肢水肿症诊治重点实验室、
              全省神经系统疾病数智化康复装备研发重点实验室等
            </p>
          </div>
  
        </section>
  
      </main>
  
      <footer class="simple-footer">
        <p>© 2025 浙江工业大学健行医院 版权所有 | 浙ICP备05015436号</p>
      </footer>
  
      <div class="float-bar">
        <div class="float-btn orange">
          <span>继续<br>浏览</span>
          <Icon icon="mdi:arrow-down-circle" />
        </div>
        <div class="float-btn top" @click="scrollToTop">
          <Icon icon="mdi:arrow-up" />
          <span>顶部</span>
        </div>
      </div>
  
    </div>
  </template>
  
  <script setup>
  import { useRouter } from 'vue-router';
  import { Icon } from '@iconify/vue';
  import { computed } from 'vue';

  const router = useRouter();

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Banner 背景图片样式，使用 computed 确保图片链接正确
  const bannerStyle = computed(() => {
    const imageUrl = 'https://tse1.mm.bing.net/th/id/OIP.re4y6LHRso8g5v1yIOTcYgHaEq?rs=1&pid=ImgDetMain&o=7&rm=3';
    return {
      backgroundImage: `linear-gradient(rgba(0, 86, 179, 0.6), rgba(0, 86, 179, 0.4)), url('${imageUrl}')`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat'
    };
  });
  </script>
  
  <style scoped>
  .page-wrapper {
    background-color: #fff;
    min-height: 100vh;
    font-family: 'Helvetica Neue', Arial, sans-serif;
  }
  
  /* --- Header 复用样式 (简化版) --- */
  .header-section {
    background: #0056b3; color: white; padding: 0 40px; height: 80px; display: flex; align-items: center;
  }
  .header-inner {
    width: 100%; max-width: 1200px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center;
  }
  .logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
  .logo-icon { font-size: 2.5rem; }
  .logo-text h1 { margin: 0; font-size: 1.4rem; }
  .logo-text small { font-size: 0.6rem; opacity: 0.8; }
  .nav-links a { color: white; margin-left: 30px; text-decoration: none; cursor: pointer; opacity: 0.8; font-size: 1rem; }
  .nav-links a:hover, .nav-links a.active { opacity: 1; font-weight: bold; border-bottom: 2px solid white; padding-bottom: 5px; }
  
  /* --- Banner --- */
  .intro-banner {
    height: 350px;
    /* 背景图片通过内联样式动态设置，这里只设置备用背景色 */
    background-color: #0056b3;
    display: flex; 
    justify-content: center; 
    align-items: center; 
    text-align: center; 
    color: white;
    position: relative;
  }
  .banner-text h1 { font-size: 3rem; margin: 0; letter-spacing: 2px; }
  .banner-text p { font-size: 1.2rem; font-weight: 300; margin-top: 10px; letter-spacing: 1px; }
  
  /* --- Main Content --- */
  .main-container {
    max-width: 1200px; margin: 0 auto; padding: 60px 20px;
  }
  
  .section-title {
    text-align: center; color: #003d80; font-size: 2.2rem; margin-bottom: 60px; font-weight: bold;
  }
  
  /* 概况区域 */
  .content-row { display: flex; gap: 50px; align-items: stretch; margin-bottom: 80px; }
  .text-col { flex: 1; }
  .img-col { flex: 1; }
  .img-col img { width: 100%; height: 100%; object-fit: cover; border-radius: 4px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
  
  .text-wrapper {
    border-left: 5px solid #2f80ed; /* 左侧蓝线 */
    padding-left: 30px;
    height: 100%;
    display: flex; flex-direction: column; justify-content: center;
  }
  .hospital-name { font-size: 1.8rem; color: #0056b3; margin: 0 0 25px 0; }
  .intro-p { font-size: 1.1rem; line-height: 1.8; color: #444; margin-bottom: 20px; text-align: justify; }
  
  /* 重点学科 (黄色高亮) */
  .honor-section { text-align: center; max-width: 1000px; margin: 0 auto; }
  .honor-item { margin-bottom: 50px; }
  
  .highlight-title {
    display: inline-block;
    background-color: #ffecb3; /* 黄色高亮背景 */
    color: #000;
    font-weight: bold;
    font-size: 1.3rem;
    padding: 8px 20px;
    margin-bottom: 20px;
    position: relative;
    box-shadow: 2px 2px 0 rgba(0,0,0,0.05);
  }
  /* 列表 */
  .honor-list { display: flex; justify-content: center; gap: 30px; flex-wrap: wrap; }
  .honor-list span { font-size: 1.2rem; font-weight: bold; color: #333; }
  .honor-desc { font-size: 1.1rem; line-height: 1.8; color: #333; font-weight: bold; max-width: 900px; margin: 0 auto; }
  
  /* Footer */
  .simple-footer { background: #f0f2f5; padding: 30px; text-align: center; color: #666; font-size: 0.9rem; margin-top: 50px; }
  
  /* 悬浮按钮 */
  .float-bar { position: fixed; right: 20px; bottom: 100px; z-index: 100; display: flex; flex-direction: column; gap: 10px; }
  .float-btn {
    width: 50px; height: 50px; display: flex; flex-direction: column; align-items: center; justify-content: center;
    font-size: 0.8rem; cursor: pointer; border-radius: 4px; transition: 0.3s;
  }
  .float-btn.orange { background: #fa8c16; color: white; height: 60px; }
  .float-btn.top { background: #ddd; color: #666; }
  .float-btn:hover { transform: translateX(-5px); }
  
  /* 响应式 */
  @media (max-width: 768px) {
    .content-row { flex-direction: column; }
    .banner-text h1 { font-size: 2rem; }
  }
  </style>