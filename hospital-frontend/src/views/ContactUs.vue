<template>
    <div class="contact-page">
      
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
            <h1>联系我们</h1>
          </div>
        </div>
        <div class="breadcrumb-strip">
          <div class="container">
            <span @click="router.push('/user')" style="cursor:pointer">网站首页</span> 
            <Icon icon="mdi:chevron-right" />
            <span class="current">联系我们</span>
          </div>
          <div class="strip-shape"></div>
        </div>
      </div>
  
      <main class="main-content">
        <div class="content-container">
          
          <h2 class="section-title">联系方式</h2>
          
          <div class="contact-card">
            <div class="info-side">
              <h3 class="campus-name">浙江工业大学健行医院朝晖院区</h3>
              <div class="info-list">
                <div class="info-item">
                  <span class="label">地址：</span>
                  <span class="value">浙江省杭州市上塘路158号</span>
                </div>
                <div class="info-item">
                  <span class="label">邮编：</span>
                  <span class="value">310014</span>
                </div>
                <div class="info-item">
                  <span class="label">总机：</span>
                  <span class="value">0571-87666666</span>
                </div>
                <div class="info-item">
                  <span class="label">传真：</span>
                  <span class="value">0571-85335800</span>
                </div>
                <div class="info-item">
                  <span class="label">监督（投诉）电话：</span>
                  <span class="value">0571-85893025</span>
                </div>
              </div>
            </div>
            <div class="image-side">
              <img src="https://images.unsplash.com/photo-1587351021759-3e566b9af9ef?q=80&w=1000&auto=format&fit=crop" alt="医院大门">
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
              <div class="addr-item"><h4>越城院区</h4><p>地址：绍兴市越城区举崇街道敬宾路299号</p></div>
            </div>
          </div>
          <div class="footer-col col-mid">
            <h3 class="footer-title">托管医院</h3>
            <ul class="footer-link-list">
              <li><span class="dot">●</span> 浙江工业大学健行医院淳安分院</li>
              <li><span class="dot">●</span> 浙江工业大学健行医院天台分院</li>
              <li><span class="dot">●</span> 浙江工业大学健行医院浙东南院区</li>
              <li><span class="dot">●</span> 浙江工业大学健行医院定海分院</li>
            </ul>
          </div>
          <div class="footer-col col-right">
            <h3 class="footer-title">卫生系统网站</h3>
            <ul class="footer-link-list">
              <li><span class="dot">●</span> 国家卫生健康委员会</li>
              <li><span class="dot">●</span> 浙江省卫生健康委员会</li>
              <li><span class="dot">●</span> 杭州医学院</li>
            </ul>
            <div class="social-icons">
              <div class="icon-box"><Icon icon="mdi:qrcode-scan" /></div>
              <div class="icon-box"><Icon icon="mdi:heart-outline" /></div>
            </div>
          </div>
        </div>
        <div class="footer-bottom-bar">
          Copyright © 2025 浙江工业大学健行医院网站版权所有 | 浙ICP备06015436号
          <span class="tech-support">技术支持：杭州触梦智能科技有限公司</span>
        </div>
      </footer>
  
    </div>
  </template>
  
  <script setup>
  import { useRouter } from 'vue-router';
  import { Icon } from '@iconify/vue';
  
  const router = useRouter();
  </script>
  
  <style scoped>
  .contact-page { min-height: 100vh; background: #f4f6f9; font-family: 'Helvetica Neue', Arial, sans-serif; padding-bottom: 0; }
  
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
    background: linear-gradient(rgba(0,0,0,0.3), rgba(0,0,0,0.3)), url('https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=2000&auto=format&fit=crop');
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
  
  /* 主体内容 */
  .main-content { padding: 50px 0 80px 0; background: #eef2f6; }
  .content-container { max-width: 1200px; margin: 0 auto; padding: 0 40px; text-align: center; }
  
  .section-title { font-size: 2rem; color: #004ea2; margin-bottom: 40px; font-weight: bold; }
  
  /* 联系卡片 (核心样式) */
  .contact-card {
    background: white;
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0,0,0,0.08);
    display: flex;
    text-align: left;
    min-height: 400px;
  }
  .info-side {
    flex: 1;
    padding: 50px;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
  .campus-name {
    color: #004ea2;
    font-size: 1.8rem;
    margin-top: 0;
    margin-bottom: 40px;
    font-weight: bold;
  }
  .info-list { display: flex; flex-direction: column; gap: 20px; }
  .info-item { font-size: 1.1rem; color: #333; display: flex; }
  .info-item .label { width: 160px; color: #666; font-weight: bold; text-align: right; margin-right: 10px; }
  .info-item .value { flex: 1; }
  
  .image-side { flex: 1.2; position: relative; }
  .image-side img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    clip-path: polygon(10% 0, 100% 0, 100% 100%, 0% 100%); /* 制作左侧的斜切效果 */
  }
  
  /* Footer */
  .app-footer { position: relative; width: 100%; background-color: #1a3a6e; color: white; padding-top: 60px; overflow: hidden; }
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
  .social-icons { display: flex; gap: 15px; margin: 30px 0; }
  .icon-box { width: 40px; height: 40px; border: 1px solid rgba(255,255,255,0.3); border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; cursor: pointer; }
  .tech-support { float: right; margin-right: 40px; }
  
  /* 响应式适配 */
  @media (max-width: 900px) {
    .contact-card { flex-direction: column; }
    .image-side img { clip-path: none; height: 300px; }
    .info-side { padding: 30px; }
    .info-item { flex-direction: column; }
    .info-item .label { text-align: left; width: 100%; margin-bottom: 5px; }
  }
  </style>