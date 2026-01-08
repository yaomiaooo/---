<template>
  <div class="app-layout">
    
    <header class="header-section">
      <div class="container-fluid header-top">
        <div class="logo-group">
          <div class="logo-icon">🏥</div>
          <div class="logo-text">
            <h1>浙江工业大学健行医院</h1>
            <small>ZHEJIANG PROVINCIAL PEOPLE'S HOSPITAL</small>
          </div>
        </div>

        <div class="header-right">
          <div class="search-box">
            <input type="text" placeholder="🔍 搜索科室、医生..." />
          </div>
          <span class="divider">|</span>
          <span class="lang-text">EN / 中文</span>
          <button class="btn-login" @click="router.push('/login')">
             <Icon icon="mdi:account-circle-outline" /> 退出登录
          </button>
        </div>
      </div>

      <div class="nav-section">
        <div class="container-fluid nav-inner">
          <a @click="router.push('/user')" class="nav-item active">首页</a>
          <a @click="router.push('/intro')" class="nav-item">医院介绍</a>
          <a href="#" class="nav-item">党建工作</a>
          
          <div class="nav-dropdown-wrapper">
            <a class="nav-item guide-trigger">就诊指南</a>
            <div class="guide-dropdown">
              <div class="dropdown-content">
                <div class="links-grid">
                  <ul>
                    <li @click="router.push('/appointment')"><span class="bullet">•</span> 预约挂号</li>
                    <li @click="router.push('/schedule')"><span class="bullet">•</span> 门诊排班</li>
                    <li @click="router.push('/specialist')"><span class="bullet">•</span> 专家介绍</li>    
                  </ul>
                  <ul>
                    <li @click="router.push('/department')"><span class="bullet">•</span> 科室导航</li>
                    <li @click="router.push('/reports')"><span class="bullet">•</span> 预约记录</li>
                    <li @click="router.push('/visit-records')"><span class="bullet">•</span> 就诊记录</li>
                  </ul>
                  <ul>
                    <li><span class="bullet">•</span> 特色诊疗</li>
                    <li><span class="bullet">•</span> 就诊指南</li>
                    <li><span class="bullet">•</span> PET-CT</li>
                    
                  </ul>
                  <ul>
                    <li><span class="bullet">•</span> 医保政策</li>
                    <li><span class="bullet">•</span> 对外协作</li>
                    <li><span class="bullet">•</span> 收费项目</li>
                  </ul>
                </div>
                <div class="dropdown-image">
                  <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTOwYErgRNn8TkM94WK15My2b1d2-p3clXL9w&s" alt="Doctor"/>
                </div>
              </div>
            </div>
          </div>
          <a href="#" class="nav-item">健康科普</a>
          <a href="#" class="nav-item">科研教学</a>
          <a href="#" class="nav-item">进修培训</a>
          <a href="#" class="nav-item">医院动态</a>
          <a href="#" class="nav-item">通知公告</a>
          <a @click="router.push('/contact')" class="nav-item" style="cursor: pointer">联系我们</a>
        </div>
      </div>
    </header>

    <main class="main-section">
      <div class="container-fluid content-row">
        
        <div class="hero-carousel" @mouseenter="stopAutoPlay" @mouseleave="startAutoPlay">
          <div class="carousel-track" :style="{ transform: `translateX(-${currentIndex * 100}%)` }">
            <div 
              v-for="(slide, index) in slides" 
              :key="index" 
              class="carousel-slide"
            >
              <img :src="slide.image" class="slide-image" alt="banner" />
              <div class="slide-content">
                <span class="slide-badge">{{ slide.tag }}</span>
                <h2 class="slide-title">{{ slide.title }}</h2>
                <p class="slide-desc">{{ slide.desc }}</p>
                <button class="btn-detail">查看详情 &rarr;</button>
              </div>
            </div>
          </div>
          <div class="indicators">
            <span 
              v-for="(n, index) in slides.length" 
              :key="index" 
              :class="['dot', { active: currentIndex === index }]"
              @click="goToSlide(index)"
            ></span>
          </div>
          <button class="arrow arrow-left" @click="prevSlide">‹</button>
          <button class="arrow arrow-right" @click="nextSlide">›</button>
        </div>

        <!-- 三行两列布局，每行独立手风琴效果 -->
        <div class="grid-menu">
          <!-- 第一行 -->
          <div class="menu-row">
            <div class="menu-item blue" @click="router.push('/appointment')">
              <div class="icon-wrap"><Icon icon="mdi:calendar-check" /></div>
              <div class="text-wrap">
                <h3>预约挂号</h3>
                <small>APPOINTMENT</small>
              </div>
              <Icon icon="mdi:calendar-blank" class="bg-icon" />
            </div>
            <div class="menu-item purple" @click="router.push('/intro')">
              <div class="icon-wrap"><Icon icon="mdi:newspaper-variant-outline" /></div>
              <div class="text-wrap">
                <h3>医院动态</h3>
                <small>NEWS</small>
              </div>
              <Icon icon="mdi:newspaper" class="bg-icon" />
            </div>
          </div>

          <!-- 第二行 -->
          <div class="menu-row">
            <div class="menu-item green" @click="router.push('/schedule')">
              <div class="icon-wrap"><Icon icon="mdi:calendar-clock" /></div>
              <div class="text-wrap">
                <h3>排班查询</h3>
                <small>SCHEDULE</small>
              </div>
              <Icon icon="mdi:calendar-search" class="bg-icon" />
            </div>
            <div class="menu-item dark" @click="router.push('/profile')">
              <div class="icon-wrap"><Icon icon="mdi:account-cog-outline" /></div>
              <div class="text-wrap">
                <h3>我的信息</h3>
                <small>PROFILE</small>
              </div>
              <Icon icon="mdi:account-details" class="bg-icon" />
            </div>
          </div>

          <!-- 第三行 -->
          <div class="menu-row">
            <div class="menu-item teal" @click="router.push('/patients')">
              <div class="icon-wrap"><Icon icon="mdi:account-heart-outline" /></div>
              <div class="text-wrap">
                <h3>我的就诊人</h3>
                <small>PATIENTS</small>
              </div>
              <Icon icon="mdi:account-group" class="bg-icon" />
            </div>
            <div class="menu-item orange" @click="router.push('/specialist')">
              <div class="icon-wrap"><Icon icon="mdi:doctor" /></div>
              <div class="text-wrap">
                <h3>专家介绍</h3>
                <small>DOCTORS</small>
              </div>
              <Icon icon="mdi:stethoscope" class="bg-icon" />
            </div>
          </div>
        </div>

      </div>

      <div class="container-fluid action-bar-row">
        <div class="action-big-btn btn-orange-gradient" @click="handleLink('科室导航')">
          <Icon icon="mdi:map-marker-path" class="action-icon" />
          <div class="action-info">
            <h3>科室导航</h3>
            <span>Department Navigation</span>
          </div>
          <Icon icon="mdi:map-search" class="bg-deco" />
        </div>
        <div class="action-big-btn btn-blue-gradient" @click="handleLink('预约记录')">
          <Icon icon="mdi:calendar-clock" class="action-icon" />
          <div class="action-info">
            <h3>预约记录</h3>
            <span>Appointment Records</span>
          </div>
          <Icon icon="mdi:clipboard-list" class="bg-deco" />
        </div>
        <div class="action-big-btn btn-dark-gradient" @click="handleLink('就诊记录')">
          <Icon icon="mdi:file-document-edit" class="action-icon" />
          <div class="action-info">
            <h3>就诊记录</h3>
            <span>Visit Records</span>
          </div>
          <Icon icon="mdi:medical-bag" class="bg-deco" />
        </div>
      </div>

      <div class="container-fluid dept-section">
        <div class="dept-header-wrapper">
          <div class="title-group">
            <h2 class="section-title">科室导航</h2>
            <span class="subtitle">Departments Navigation</span>
          </div>
          <div class="search-container">
            <Icon icon="mdi:magnify" class="search-icon" />
            <input type="text" placeholder="输入科室或疾病名称..." />
            <button class="btn-search">搜索</button>
          </div>
        </div>

        <div class="campus-tab-bar">
          <div 
            v-for="campus in campuses" 
            :key="campus"
            class="tab-pill"
            :class="{ active: activeCampus === campus }"
            @click="activeCampus = campus; handleCampusChange()"
          >
            {{ campus }}
          </div>
        </div>

        <div class="dept-container">
          <div class="bg-pattern"></div>
          
          <div class="dept-group">
            <div class="group-header">
              <div class="header-icon-box blue">
                <Icon icon="mdi:stethoscope" />
              </div>
              <h3>临床科室 <small>Clinical Departments</small></h3>
            </div>
            <div class="dept-card-grid">
              <div 
                v-for="(dept, idx) in clinicalDepts" 
                :key="dept.departmentId || idx" 
                class="entry-card"
                :class="{ 'is-more': (dept.departmentName || dept).includes('更多') }"
                @click="handleDeptClick(dept)"
              >
                <span class="dept-name">{{ dept.departmentName || dept }}</span>
                <Icon icon="mdi:arrow-right" class="entry-arrow" />
              </div>
            </div>
          </div>

          <div class="divider-line"></div>


        </div>
      </div>

      <div class="campus-intro-wrapper">
        <div class="bg-watermark-text">院区介绍</div>
        <div class="campus-content-inner">
          <div class="campus-nav-list">
            <div 
              v-for="(item, index) in campusList" 
              :key="index"
              class="campus-nav-item"
              :class="{ active: currentCampusIndex === index }"
              @mouseenter="handleCampusHover(index)"
            >
              <div class="nav-dot"></div>
              <div class="nav-text-group">
                <span class="nav-name">{{ item.name }}</span>
                <div 
                  class="nav-pill-hint" 
                  v-show="currentCampusIndex === index"
                  @click="router.push(`/campus/${item.id}`)"
                >
                  <Icon icon="mdi:domain" /> 点击查看{{ item.name }}详情
                </div>
              </div>
            </div>
          </div>
          <div class="campus-image-box">
            <div 
              class="image-layer"
              v-for="(item, index) in campusList"
              :key="index"
              :style="{ 
                backgroundImage: `url(${item.image})`,
                opacity: currentCampusIndex === index ? 1 : 0 
              }"
            ></div>
            <div class="image-corner-deco">
              <span>{{ campusList[currentCampusIndex].desc }}</span>
            </div>
          </div>
        </div>
      </div>

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
              <div v-for="(item, idx) in footerAddresses" :key="idx" class="addr-item">
                <h4>{{ item.name }}</h4>
                <p>地址：{{ item.addr }}</p>
              </div>
            </div>
          </div>
          <div class="footer-col col-mid">
            <h3 class="footer-title">托管医院</h3>
            <ul class="footer-link-list">
              <li v-for="(name, idx) in managedHospitals" :key="idx">
                <span class="dot">●</span> {{ name }}
              </li>
            </ul>
          </div>
          <div class="footer-col col-right">
            <h3 class="footer-title">卫生系统网站</h3>
            <ul class="footer-link-list">
              <li v-for="(link, idx) in systemLinks" :key="idx">
                <span class="dot">●</span> {{ link }}
              </li>
            </ul>
            <div class="social-icons">
              <div class="icon-box"><Icon icon="mdi:qrcode-scan" /></div>
              <div class="icon-box"><Icon icon="mdi:heart-outline" /></div>
              <div class="icon-box"><Icon icon="mdi:video-outline" /></div>
              <div class="icon-box"><Icon icon="mdi:wechat" /></div>
            </div>
            <div class="copyright-text">
              <p>帮助信息 > 隐私安全 > 版权与免责声明 ></p>
              <p style="opacity: 0.5; margin-top: 5px; font-size: 12px;">浙ICP备05015436号 | 浙公网安备 33010302000771号</p>
            </div>
          </div>
        </div>
      </footer>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { getDepartmentList } from '../api/hospital'; 

const router = useRouter();

// --- 轮播图 ---
const currentIndex = ref(0);
let timer = null;
const slides = [
  {
    image: 'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?q=80&w=2000&auto=format&fit=crop',
    tag: '🏥 医院要闻',
    title: '浙江工业大学健行医院夜间门诊正式开诊',
    desc: '为方便上班族及学生群体就医，我院特开设夜间门诊服务，覆盖内科、外科及专家门诊。'
  },
  {
    image: 'https://images.unsplash.com/photo-1579684385136-137af18db235?q=80&w=2000&auto=format&fit=crop',
    tag: '🔬 科研突破',
    title: '我院在心血管微创手术领域取得新进展',
    desc: '心内科团队成功完成首例高难度微创搭桥手术，标志着我院在心脏医学领域迈上新台阶。'
  },
  {
    image: 'https://images.unsplash.com/photo-1505751172876-fa1923c5c528?q=80&w=2000&auto=format&fit=crop',
    tag: '💙 公益活动',
    title: '“送医下乡”专家团走进山区',
    desc: '这一周，我们的专家团队深入基层，为偏远地区的老年人提供免费体检和健康咨询服务。'
  }
];

const nextSlide = () => currentIndex.value = (currentIndex.value + 1) % slides.length;
const prevSlide = () => currentIndex.value = (currentIndex.value - 1 + slides.length) % slides.length;
const goToSlide = (index) => currentIndex.value = index;
const startAutoPlay = () => { if (timer) clearInterval(timer); timer = setInterval(nextSlide, 4000); };
const stopAutoPlay = () => { if (timer) clearInterval(timer); };

// --- 科室导航 (仅保留2个院区) ---
const activeCampus = ref('朝晖院区');
const campuses = ['朝晖院区', '屏峰院区'];
const clinicalDepts = ref([]); 
const techDepts = ref([]);

// 院区ID映射
const campusIdMap = {
  '朝晖院区': '1',
  '屏峰院区': '2'
};

// --- 院区介绍数据 (仅保留2个院区) ---
const currentCampusIndex = ref(0);
const campusList = [
  {
    id: 'zhaohui',
    name: '朝晖院区',
    desc: 'Zhaohui Campus',
    image: 'https://media.istockphoto.com/id/2215828058/ja/%E3%82%B9%E3%83%88%E3%83%83%E3%82%AF%E3%83%95%E3%82%A9%E3%83%88/%E6%98%8E%E3%82%8B%E3%81%84%E8%89%B2%E3%81%AE%E6%9C%A8%E8%A3%BD%E3%81%AE%E3%83%89%E3%82%A2%E3%81%A8%E9%9D%92%E3%81%84%E5%BA%A7%E5%B8%AD%E3%82%92%E5%82%99%E3%81%88%E3%81%9F%E3%83%A2%E3%83%80%E3%83%B3%E3%81%AA%E7%97%85%E9%99%A2%E3%81%AE%E5%BB%8A%E4%B8%8B%E3%81%AF%E3%81%99%E3%81%A3%E3%81%8D%E3%82%8A%E3%81%A8%E3%81%97%E3%81%9F%E3%83%9F%E3%83%8B%E3%83%9E%E3%83%AB%E3%81%AA%E3%83%98%E3%83%AB%E3%82%B9%E3%82%B1%E3%82%A2%E3%82%A4%E3%83%B3%E3%83%86%E3%83%AA%E3%82%A2%E3%83%87%E3%82%B6%E3%82%A4%E3%83%B3%E3%81%AE%E3%82%B3%E3%83%B3%E3%82%BB%E3%83%97%E3%83%88%E3%82%92%E7%A4%BA%E3%81%97%E3%81%A6%E3%81%84%E3%81%BE%E3%81%993d%E3%83%AC%E3%83%B3%E3%83%80%E3%83%AA%E3%83%B3%E3%82%B0.jpg?s=1024x1024&w=is&k=20&c=-AlCHCO4B37iyuc97dbejP_usxjoAGPKCMnU73cn3dI=',
    link: '#'
  },
  {
    id: 'pingfeng',
    name: '屏峰院区',
    desc: 'Pingfeng Campus',
    image: 'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?q=80&w=1920',
    link: '#'
  }
];
const handleCampusHover = (index) => { currentCampusIndex.value = index; };

// --- 页脚数据 (仅保留2个院区) ---
const footerAddresses = [
  { name: '朝晖院区', addr: '杭州市上塘路158号' },
  { name: '屏峰院区', addr: '杭州市西湖区留和路288号' }
];
const managedHospitals = [
  '浙江工业大学健行医院淳安分院', '浙江工业大学健行医院天台分院', '浙江工业大学健行医院浙东南院区',
  '浙江工业大学健行医院定海分院', '浙江工业大学健行医院海宁医院', '浙江工业大学健行医院南浔院区'
];
const systemLinks = ['国家卫生健康委员会', '浙江省卫生健康委员会', '杭州医学院'];

// --- 逻辑 ---
const handleLink = (name) => {
  console.log('跳转:', name);
  // 根据名称跳转到对应的路由
  switch (name) {
    case '科室导航':
      router.push('/department');
      break;
    case '预约记录':
      router.push('/reports');
      break;
    case '就诊记录':
      router.push('/visit-records');
      break;
    default:
      console.log('未知的链接:', name);
  }
};

// 加载当前院区的科室数据
const loadDepartmentData = async () => {
  const hospitalId = campusIdMap[activeCampus.value];
  if (!hospitalId) return;
  
  try {
    const res = await getDepartmentList(hospitalId);
    if (res.code === 200 && res.data) {
      // 存储完整的科室对象，包含 departmentId
      clinicalDepts.value = res.data;
      // 医技科室暂时为空，或者可以根据科室名称判断
      techDepts.value = [];
    }
  } catch (error) {
    console.error('获取科室数据失败:', error);
    clinicalDepts.value = [];
    techDepts.value = [];
  }
};

// 处理科室点击事件
const handleDeptClick = (dept) => {
  // 如果 dept 是对象，获取 departmentId；如果是字符串，说明是旧数据格式
  if (dept && typeof dept === 'object' && dept.departmentId) {
    // 跳转到科室详情页
    router.push(`/department/${dept.departmentId}`);
  } else if (typeof dept === 'string') {
    // 如果是字符串（旧数据格式），尝试通过科室名称查找
    console.warn('科室数据格式为字符串，无法跳转:', dept);
  } else {
    console.warn('科室数据格式错误:', dept);
  }
};

// 监听院区切换
const handleCampusChange = () => {
  loadDepartmentData();
};

onMounted(() => { 
  startAutoPlay(); 
  loadDepartmentData(); 
});
onUnmounted(() => { stopAutoPlay(); });
</script>

<style scoped>
/* --------------------------------------
   全局布局
-------------------------------------- */
.app-layout {
  min-height: 100vh;
  display: flex; flex-direction: column; background-color: #f7f9fc;
  font-family: 'Helvetica Neue', Arial, sans-serif; overflow-x: hidden;
}
.container-fluid { width: 100%; padding: 0 40px; box-sizing: border-box; margin: 0 auto; }

/* 1. Header */
.header-section { background-color: #0056b3; color: white; width: 100%; }
.header-top { height: 80px; display: flex; justify-content: space-between; align-items: center; }
.logo-group { display: flex; align-items: center; gap: 15px; }
.logo-icon { font-size: 2.5rem; background: rgba(255,255,255,0.2); padding: 5px; border-radius: 8px; }
.logo-text h1 { margin: 0; font-size: 1.6rem; letter-spacing: 1px; }
.logo-text small { font-size: 0.75rem; opacity: 0.85; }
.header-right { display: flex; align-items: center; gap: 20px; }
.search-box input { background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.3); color: white; padding: 8px 15px; border-radius: 20px; outline: none; width: 250px; }
.divider { opacity: 0.5; }
.btn-login { background: transparent; border: 1px solid white; color: white; padding: 6px 18px; border-radius: 20px; cursor: pointer; display: flex; align-items: center; gap: 5px; }
.nav-section { background: rgba(0,0,0,0.15); width: 100%; }
.nav-inner { display: flex; height: 50px; align-items: center; overflow-x: auto; position: relative; /* 关键：为下拉菜单提供定位上下文 */ }
.nav-item { color: white; text-decoration: none; padding: 0 25px; height: 100%; display: flex; align-items: center; font-size: 1rem; white-space: nowrap; cursor: pointer; }
.nav-item:hover, .nav-item.active { background: #004494; font-weight: bold; }

/* ★★★★★ Mega Menu 样式 ★★★★★ */
.nav-dropdown-wrapper { position: relative; height: 100%; display: flex; align-items: center; }
.guide-trigger { cursor: pointer; height: 100%; display: flex; align-items: center; padding: 0 25px; color: white; text-decoration: none; font-size: 1rem; transition: all 0.3s; }
.nav-dropdown-wrapper:hover .guide-trigger { background-color: #f0ad4e; font-weight: bold; }
.nav-dropdown-wrapper:hover .guide-dropdown { visibility: visible; opacity: 1; transform: translateY(0); }
.guide-dropdown { position: absolute; top: 100%; left: -100px; width: 800px; background: white; box-shadow: 0 10px 30px rgba(0,0,0,0.15); visibility: hidden; opacity: 0; transform: translateY(10px); transition: all 0.3s ease; z-index: 999; border-top: 3px solid #f0ad4e; }
.dropdown-content { display: flex; padding: 30px; gap: 30px; }
.links-grid { flex: 2; display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.links-grid ul { list-style: none; padding: 0; margin: 0; }
.links-grid li { margin-bottom: 12px; color: #666; font-size: 0.9rem; cursor: pointer; transition: 0.2s; white-space: nowrap; }
.links-grid li:hover { color: #f0ad4e; }
.bullet { color: #ccc; margin-right: 5px; font-size: 0.8rem; }
.dropdown-image {
  flex: 1;
  overflow: hidden;
  border-radius: 4px;
  /* 新增：给一个最小高度，确保没图时也有位置 */
  min-height: 200px; 
  background-color: #f0f0f0; /* 给个灰色背景，图裂了也能看到块 */
}

.dropdown-image img { width: 100%; height: 100%; object-fit: cover; }
/* ★★★★★ Mega Menu 样式结束 ★★★★★ */

/* 2. Main Section */
.main-section { flex: 1; padding: 30px 0; display: flex; flex-direction: column; align-items: center; justify-content: flex-start; }
.content-row { display: flex; gap: 20px; height: 500px; width: 100%; }

/* 轮播图 */
.hero-carousel { flex: 1.6; position: relative; border-radius: 12px; overflow: hidden; box-shadow: 0 5px 20px rgba(0,0,0,0.08); background: #000; }
.carousel-track { display: flex; height: 100%; transition: transform 0.6s ease-in-out; }
.carousel-slide { min-width: 100%; height: 100%; position: relative; }
.slide-image { width: 100%; height: 100%; object-fit: cover; opacity: 0.85; }
.slide-content { position: absolute; bottom: 0; left: 0; width: 100%; padding: 40px; background: linear-gradient(to top, rgba(0,0,0,0.9), transparent); color: white; display: flex; flex-direction: column; align-items: flex-start; }
.slide-badge { background: #0056b3; color: white; padding: 5px 12px; border-radius: 4px; margin-bottom: 10px; }
.slide-title { font-size: 2.2rem; margin-bottom: 15px; font-weight: bold; }
.slide-desc { font-size: 1.1rem; margin-bottom: 20px; max-width: 80%; opacity: 0.9; }
.btn-detail { background: white; color: #333; border: none; padding: 10px 25px; border-radius: 30px; font-weight: bold; cursor: pointer; }
.indicators { position: absolute; bottom: 20px; right: 30px; display: flex; gap: 8px; z-index: 10; }
.dot { width: 10px; height: 10px; background: rgba(255,255,255,0.4); border-radius: 50%; cursor: pointer; }
.dot.active { background: white; width: 25px; border-radius: 10px; }
.arrow { position: absolute; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.3); color: white; border: none; font-size: 2rem; padding: 10px 15px; cursor: pointer; opacity: 0; }
.hero-carousel:hover .arrow { opacity: 1; }
.arrow-left { left: 0; } .arrow-right { right: 0; }

/* 三行两列布局 - 每行独立横向手风琴效果 */
.grid-menu { 
  flex: 1; 
  display: flex; 
  flex-direction: column; 
  gap: 15px; 
}

/* 每一行是一个独立的 Flex 容器 */
.menu-row {
  display: flex;
  gap: 15px;
  flex: 1;
  min-height: 0; /* 允许 flex 项目收缩 */
}

/* 默认状态：每行的两个卡片等宽（各占50%） */
.menu-item { 
  flex: 1 1 0; /* flex-grow: 1, flex-shrink: 1, flex-basis: 0 */
  position: relative; 
  border-radius: 10px; 
  color: white; 
  padding: 0 25px; 
  display: flex; 
  align-items: center; 
  gap: 15px; 
  cursor: pointer; 
  transition: flex 0.4s ease-in-out, transform 0.3s ease, box-shadow 0.3s ease; 
  overflow: hidden; 
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  min-width: 0; /* 防止内容溢出 */
}

/* 当鼠标悬停在某一行的某个卡片上时，该卡片展开到55%，同行的另一个卡片收缩到45% */
.menu-row:hover .menu-item {
  flex: 1 1 0; /* 默认收缩到45% */
}

.menu-row:hover .menu-item:hover {
  flex: 1.22 1 0; /* 悬停的卡片展开到55% (1.22 / (1.22 + 1) ≈ 55%) */
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.15);
}

/* 图标和文字容器 */
.icon-wrap { 
  font-size: 2.2rem; 
  display: flex; 
  align-items: center; 
  flex-shrink: 0; /* 图标不收缩 */
}

.text-wrap { 
  flex: 1;
  min-width: 0; /* 允许文字容器收缩 */
  white-space: nowrap; /* 防止文字换行 */
  overflow: hidden; /* 隐藏溢出内容 */
}

.text-wrap h3 { 
  margin: 0; 
  font-size: 1.2rem; 
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.text-wrap small { 
  font-size: 0.75rem; 
  opacity: 0.8; 
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bg-icon { 
  position: absolute; 
  right: 10px; 
  bottom: 10px; 
  font-size: 4.5rem; 
  opacity: 0.15; 
  transform: rotate(-15deg); 
  transition: 0.3s;
  pointer-events: none; /* 防止图标阻挡点击 */
}

.menu-item:hover .bg-icon { 
  transform: rotate(0) scale(1.1); 
  opacity: 0.25; 
}
.blue { background: linear-gradient(135deg, #4481eb, #04befe); }
.purple { background: linear-gradient(135deg, #a55eea, #8854d0); }
.green { background: linear-gradient(135deg, #26de81, #20bf6b); }
.dark { background: linear-gradient(135deg, #4b6584, #394a61); }
.teal { background: linear-gradient(135deg, #2bcbba, #0fb9b1); }
.orange { background: linear-gradient(135deg, #fd9644, #fa8231); }

/* 3. 底部三按钮 - 横向手风琴效果 */
.action-bar-row { 
  display: flex; 
  gap: 20px; 
  margin-top: 20px; 
  height: 100px; 
  width: 100%; 
}

/* 默认状态：三个卡片等宽 */
.action-big-btn { 
  flex: 1 1 0; /* flex-grow: 1, flex-shrink: 1, flex-basis: 0 */
  border-radius: 10px; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  gap: 15px; 
  color: white; 
  position: relative; 
  overflow: hidden; 
  cursor: pointer; 
  transition: flex-grow 0.4s cubic-bezier(0.4, 0, 0.2, 1), transform 0.3s ease;
  min-width: 0; /* 防止内容溢出 */
}

/* 当鼠标悬停在父容器上时，所有卡片收缩到各占约30% */
.action-bar-row:hover .action-big-btn {
  flex-grow: 0.3;
}

/* 当鼠标悬停在某个卡片上时，该卡片展开到约40%，其他卡片保持约30% */
.action-bar-row:hover .action-big-btn:hover {
  flex-grow: 0.4; /* 悬停的卡片占据约40%空间 */
  transform: translateY(-5px);
}

/* 防止内容换行和错位 */
.action-big-btn .action-info {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

.action-big-btn .action-info h3 {
  margin: 0; 
  font-size: 1.4rem; 
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-big-btn .action-info span {
  font-size: 0.8rem; 
  opacity: 0.8; 
  text-transform: uppercase;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.action-icon { 
  font-size: 2.8rem; 
  background: rgba(255,255,255,0.2); 
  padding: 8px; 
  border-radius: 50%; 
  flex-shrink: 0; /* 图标不收缩 */
}

.bg-deco { 
  position: absolute; 
  right: -10px; 
  bottom: -15px; 
  font-size: 5rem; 
  opacity: 0.15; 
  transform: rotate(-15deg);
  pointer-events: none; /* 防止图标阻挡点击 */
}

.btn-orange-gradient { background: linear-gradient(135deg, #f1c40f, #e67e22); }
.btn-blue-gradient { background: linear-gradient(135deg, #5dade2, #3498db); }
.btn-dark-gradient { background: linear-gradient(135deg, #34495e, #1a252f); }

/* 4. 科室导航 */
.dept-section { margin-top: 50px; width: 100%; margin-bottom: 60px; }
.dept-header-wrapper { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 25px; }
.section-title { font-size: 2rem; color: #333; margin: 0; font-weight: 700; }
.subtitle { font-size: 0.9rem; color: #999; text-transform: uppercase; margin-top: 5px; display: block; }
.search-container { background: white; border: 1px solid #e0e6ed; border-radius: 50px; padding: 5px 5px 5px 20px; display: flex; align-items: center; width: 350px; transition: 0.3s; }
.search-container:hover { border-color: #004ea2; }
.search-icon { color: #999; margin-right: 10px; }
.search-container input { border: none; outline: none; width: 100%; }
.btn-search { background: #004ea2; color: white; border: none; padding: 8px 24px; border-radius: 40px; cursor: pointer; }

.campus-tab-bar { display: flex; gap: 10px; padding-left: 10px; }
.tab-pill { padding: 12px 30px; background: #f0f4f8; color: #666; border-radius: 12px 12px 0 0; cursor: pointer; font-weight: 600; }
.tab-pill.active { background: white; color: #004ea2; position: relative; top: 1px; z-index: 2; border: 1px solid #e0e6ed; border-bottom: none; }

.dept-container { background: white; border: 1px solid #e0e6ed; border-radius: 0 12px 12px 12px; padding: 50px; position: relative; overflow: hidden; }
.bg-pattern { position: absolute; top: 0; right: 0; width: 300px; height: 300px; background: radial-gradient(circle, rgba(0,78,162,0.03) 0%, transparent 70%); }
.dept-group { position: relative; z-index: 1; }
.group-header { display: flex; align-items: center; gap: 15px; margin-bottom: 25px; }
.header-icon-box { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; }
.header-icon-box.blue { background: #e3f2fd; color: #004ea2; }
.header-icon-box.green { background: #e8f5e9; color: #2e7d32; }
.dept-card-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.entry-card { background: #fff; border: 1px solid #eee; padding: 20px 25px; border-radius: 8px; display: flex; justify-content: space-between; align-items: center; cursor: pointer; transition: 0.25s; }
.entry-card:hover { border-color: #004ea2; transform: translateY(-3px); box-shadow: 0 8px 20px rgba(0, 78, 162, 0.1); }
.entry-card:hover .dept-name { color: #004ea2; font-weight: 600; }
.entry-arrow { opacity: 0; transition: 0.2s; }
.entry-card:hover .entry-arrow { opacity: 1; color: #004ea2; }
.entry-card.is-more { background: #f8fbff; }
.divider-line { height: 1px; background: #eee; margin: 40px 0; }

/* 5. 院区介绍 */
.campus-intro-wrapper { position: relative; width: 100vw; margin-left: calc(-50vw + 50%); background-color: #2b5aa5; color: white; padding: 80px 0; display: flex; justify-content: center; overflow: hidden; }
.bg-watermark-text { position: absolute; left: 5%; bottom: -20px; font-size: 12rem; font-weight: 900; color: rgba(255,255,255,0.03); pointer-events: none; }
.campus-content-inner { width: 100%; max-width: 1400px; padding: 0 40px; display: flex; align-items: center; justify-content: space-between; gap: 60px; position: relative; z-index: 1; }
.campus-nav-list { flex: 1; display: flex; flex-direction: column; gap: 40px; }
.campus-nav-item { display: flex; align-items: center; gap: 20px; cursor: pointer; opacity: 0.6; transition: 0.3s; padding-left: 20px; }
.campus-nav-item:hover, .campus-nav-item.active { opacity: 1; }
.nav-dot { width: 12px; height: 12px; background: rgba(255,255,255,0.5); border-radius: 50%; transition: 0.3s; }
.campus-nav-item.active .nav-dot { background: #fff; transform: scale(1.2); }
.nav-text-group { display: flex; flex-direction: column; align-items: flex-start; }
.nav-name { font-size: 1.8rem; font-weight: bold; }

/* 胶囊按钮 */
.nav-pill-hint { margin-top: 10px; background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.3); padding: 6px 16px; border-radius: 20px; font-size: 0.9rem; display: flex; align-items: center; gap: 6px; backdrop-filter: blur(5px); cursor: pointer; animation: fadeInSlide 0.3s ease-out; }
@keyframes fadeInSlide { from { opacity: 0; transform: translateX(-10px); } to { opacity: 1; transform: translateX(0); } }

.campus-image-box { flex: 1.5; height: 500px; position: relative; border-radius: 24px; overflow: hidden; background: #1a3a6e; box-shadow: 0 20px 50px rgba(0,0,0,0.2); border: 4px solid rgba(255,255,255,0.1); }
.image-layer { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background-size: cover; background-position: center; transition: opacity 0.6s ease-in-out; }
.image-corner-deco { position: absolute; bottom: 0; right: 0; background: rgba(0,0,0,0.6); padding: 10px 30px; border-radius: 20px 0 0 0; color: rgba(255,255,255,0.8); }

/* 6. Footer */
.app-footer { position: relative; width: 100vw; margin-left: calc(-50vw + 50%); background-color: #1a3a6e; color: white; padding: 60px 0 40px 0; overflow: hidden; }
.footer-bg-image { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(to top, rgba(26, 58, 110, 0.95), rgba(43, 90, 165, 0.8)), url('https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?q=80&w=2000&auto=format&fit=crop'); background-size: cover; opacity: 0.6; }
.footer-content { position: relative; z-index: 1; max-width: 1400px; margin: 0 auto; padding: 0 40px; display: flex; justify-content: space-between; gap: 40px; }
.footer-col { flex: 1; }
.col-left { flex: 1.2; }
.footer-logos { margin-bottom: 30px; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 20px; }
.logo-placeholder { display: flex; align-items: center; gap: 15px; }
.logo-ico { font-size: 3.5rem; }
.logo-txt h3 { margin: 0; font-size: 1.5rem; }
.addr-item h4 { margin: 0 0 5px 0; font-size: 1rem; color: #fff; }
.addr-item p { margin: 0; font-size: 0.85rem; color: rgba(255,255,255,0.7); }
.footer-title { font-size: 1.1rem; font-weight: bold; margin-bottom: 20px; border-left: 3px solid #00b0f0; padding-left: 10px; }
.footer-link-list { list-style: none; padding: 0; }
.footer-link-list li { margin-bottom: 10px; color: rgba(255,255,255,0.8); cursor: pointer; display: flex; align-items: center; gap: 8px; }
.footer-link-list li:hover { color: white; transform: translateX(5px); }
.social-icons { display: flex; gap: 15px; margin: 30px 0; }
.icon-box { width: 40px; height: 40px; border: 1px solid rgba(255,255,255,0.3); border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; cursor: pointer; }
.icon-box:hover { background: white; color: #1a3a6e; }

@media (max-width: 1024px) {
  .content-row { height: auto; flex-direction: column; }
  /* 响应式：小屏幕时每行仍保持2列，但可以调整间距 */
  .menu-row {
    gap: 10px;
  }
  .campus-content-inner { flex-direction: column-reverse; }
  .footer-content { flex-direction: column; }
}

.nav-section { 
  background: rgba(0,0,0,0.15); 
  width: 100%; 
  /* ★★★ 新增：提升层级，确保菜单盖在轮播图上面 ★★★ */
  position: relative;
  z-index: 1000; 
}

.nav-inner { 
  display: flex; 
  height: 50px; 
  align-items: center; 
  /* ★★★ 关键修改：删掉了 overflow-x: auto，改用 visible，否则菜单会被切掉 ★★★ */
  overflow: visible; 
  position: relative; 
}
</style>