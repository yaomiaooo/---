<template>
  <div class="specialist-page">
    <header class="main-header">
      <div class="header-inner">
        <div class="logo-group" @click="router.push('/user')">
          <span class="logo-icon">🏥</span>
          <div class="logo-text"><h1>浙江工业大学健行医院</h1><small>ZHEJIANG PROVINCIAL PEOPLE'S HOSPITAL</small></div>
        </div>
        <div class="back-home" @click="router.push('/user')"><Icon icon="mdi:home" /> 返回首页</div>
      </div>
    </header>

    <div class="top-banner-section">
      <div class="banner-bg"><div class="banner-text"><h1>专家介绍</h1></div></div>
      <div class="breadcrumb-strip">
        <div class="container">
          <span @click="router.push('/user')">网站首页</span><Icon icon="mdi:chevron-right" /><span>就诊指南</span><Icon icon="mdi:chevron-right" /><span class="current">专家介绍</span>
        </div>
        <div class="strip-shape"></div>
      </div>
    </div>

    <div class="campus-tabs-bar">
      <div class="tabs-inner">
        <div class="tabs-list">
          <div v-for="campus in ['朝晖院区', '屏峰院区']" :key="campus" class="tab-item" :class="{ active: activeCampus === campus }" @click="activeCampus = campus">
            {{ campus }}<div class="triangle" v-if="activeCampus === campus"></div>
          </div>
        </div>
        <div class="tab-search-box">
          <div class="input-inner">
            <Icon icon="mdi:magnify" class="search-icon" />
            <input 
              type="text" 
              placeholder="请输入专家姓名" 
              v-model="searchKeyword"
              @keyup.enter="handleSearch"
            />
          </div>
          <button class="btn-tab-search" @click="handleSearch">搜索</button>
        </div>
      </div>
    </div>

    <main class="main-content">
      <div class="content-container">
        <div v-if="loading" class="loading-state">
          <p>加载中...</p>
        </div>
        <div v-else>
          <div class="dept-section" v-for="dept in departmentList" :key="dept.departmentId">
            <div class="dept-header">
              <div class="dept-info">
                <h2 class="dept-title-border">{{ dept.departmentName }}</h2>
                <p class="dept-desc">{{ dept.departmentIntro || '暂无介绍' }}</p>
              </div>
              <div class="see-more">查看更多 <Icon icon="mdi:arrow-right-thin" /></div>
            </div>
            <div class="doctor-grid" v-if="dept.doctors && dept.doctors.length > 0">
              <div class="doctor-card" v-for="doc in dept.doctors" :key="doc.doctorId || doc.name">
                <div class="doc-photo">
                  <img :src="doc.photo" :alt="doc.name" @error="handleImageError">
                </div>
                <div class="doc-info">
                  <div class="doc-top">
                    <span class="doc-name">{{ doc.name }}</span>
                    <span class="doc-title">{{ doc.title }}</span>
                  </div>
                  <div class="doc-dept">{{ dept.departmentName }}</div>
                  <div class="doc-skill">
                    <span class="label">擅长：</span>{{ doc.skill || '暂无' }}
                  </div>
                  <div class="doc-action">
                    <button class="btn-see-reviews" @click="openReviews(doc)">
                      <Icon icon="mdi:comment-quote-outline" /> 查看评价
                    </button>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="no-doctors">
              <p>该科室暂无医生</p>
            </div>
          </div>
          <div v-if="departmentList.length === 0 && !loading" class="empty-state">
            <p>暂无科室信息</p>
          </div>
        </div>
      </div>
    </main>

    <div class="modal-overlay" v-if="showReviewModal">
      <div class="modal-box review-list-modal fade-in-up">
        <div class="modal-header">
          <div class="doctor-header-info">
            <h3>{{ currentDoctorName }} 的患者评价</h3>
            <div v-if="averageRating > 0" class="rating-summary">
              <div class="rating-stars">
                <Icon v-for="n in 5" :key="n" :icon="n <= Math.round(averageRating) ? 'mdi:star' : 'mdi:star-outline'" :class="n <= Math.round(averageRating) ? 'star-yellow' : 'star-gray'" />
              </div>
              <span class="rating-score">{{ averageRating.toFixed(1) }}</span>
              <span class="rating-count">（{{ currentDoctorReviews.length }}条评价）</span>
            </div>
          </div>
          <button class="btn-close" @click="showReviewModal = false"><Icon icon="mdi:close" /></button>
        </div>
        <div class="modal-body review-body">
          <div v-if="loadingReviews" class="loading-reviews">
            <Icon icon="mdi:loading" class="loading-icon" />
            <p>加载中...</p>
          </div>
          <div v-else-if="currentDoctorReviews.length === 0" class="no-reviews">
            <Icon icon="mdi:message-off-outline" class="gray-icon" />
            <p>该医生暂无评价</p>
          </div>
          <div v-else class="review-scroll">
            <div v-for="rev in currentDoctorReviews" :key="rev.reviewId" class="review-item">
              <div class="rev-header">
                <span class="rev-user">患者{{ rev.reviewId.substring(0, 6) }}</span>
                <div class="rev-stars">
                  <Icon v-for="n in 5" :key="n" :icon="n <= rev.rating ? 'mdi:star' : 'mdi:star-outline'" :class="n <= rev.rating ? 'star-yellow' : 'star-gray'" />
                </div>
                <span class="rev-time">{{ formatReviewDate(rev.createdAt) }}</span>
              </div>
              <div class="rev-content">{{ rev.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <footer class="app-footer">
      <div class="footer-bottom-bar">Copyright © 2025 浙江工业大学健行医院网站版权所有</div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { getDoctors } from '../api/doctor';
import { getDepartmentList } from '../api/hospital';
import { getReviewsByDoctorId } from '../api/review';

const router = useRouter();
const activeCampus = ref('朝晖院区');
const searchKeyword = ref('');
const showReviewModal = ref(false);
const currentDoctorName = ref('');
const currentDoctorId = ref('');
const allReviews = ref([]);
const loading = ref(false);
const loadingReviews = ref(false);

// 院区ID映射
const campusIdMap = {
  '朝晖院区': '1',
  '屏峰院区': '2'
};

// 科室列表和医生数据
const departmentList = ref([]);
const allDoctors = ref([]);

// 打开评价弹窗
const openReviews = async (doctor) => {
  currentDoctorName.value = doctor.name;
  currentDoctorId.value = doctor.doctorId;
  showReviewModal.value = true;
  await loadDoctorReviews(doctor.doctorId);
};

// 加载医生的评价
const loadDoctorReviews = async (doctorId) => {
  if (!doctorId) {
    allReviews.value = [];
    return;
  }

  loadingReviews.value = true;
  try {
    const res = await getReviewsByDoctorId(doctorId);
    console.log('获取医生评价API响应:', res);
    if (res.code === 200 && res.data) {
      allReviews.value = res.data.map(review => ({
        reviewId: review.reviewId,
        rating: review.rating || 5,
        content: review.content || '',
        createdAt: review.createdAt,
        doctorName: review.doctorName || currentDoctorName.value
      }));
      console.log('转换后的评价列表:', allReviews.value);
    } else {
      console.error('获取医生评价失败:', res.message);
      allReviews.value = [];
    }
  } catch (error) {
    console.error('获取医生评价失败:', error);
    alert(error.message || '获取评价失败，请检查网络连接');
    allReviews.value = [];
  } finally {
    loadingReviews.value = false;
  }
};

// 筛选当前医生的评价
const currentDoctorReviews = computed(() => {
  return allReviews.value;
});

// 计算平均评分
const averageRating = computed(() => {
  if (currentDoctorReviews.value.length === 0) {
    return 0;
  }
  const sum = currentDoctorReviews.value.reduce((acc, rev) => acc + (rev.rating || 0), 0);
  return sum / currentDoctorReviews.value.length;
});

// 格式化评价日期
const formatReviewDate = (dateStr) => {
  if (!dateStr) return '';
  try {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  } catch (e) {
    return dateStr;
  }
};

// 加载科室列表
const loadDepartments = async () => {
  const hospitalId = campusIdMap[activeCampus.value];
  if (!hospitalId) return;
  
  try {
    const res = await getDepartmentList(hospitalId);
    if (res.code === 200 && res.data) {
      departmentList.value = res.data.map(dept => ({
        ...dept,
        doctors: []
      }));
    }
  } catch (error) {
    console.error('获取科室列表失败:', error);
    departmentList.value = [];
  }
};

// 加载医生列表
const loadDoctors = async () => {
  loading.value = true;
  const hospitalId = campusIdMap[activeCampus.value];
  if (!hospitalId) {
    loading.value = false;
    return;
  }
  
  try {
    const keyword = searchKeyword.value && searchKeyword.value.trim() 
      ? searchKeyword.value.trim() 
      : null;
    
    const res = await getDoctors(hospitalId, null, keyword);
    if (res.code === 200 && res.data) {
      allDoctors.value = res.data;
      // 按科室分组
      groupDoctorsByDepartment();
    } else {
      allDoctors.value = [];
      departmentList.value.forEach(dept => {
        dept.doctors = [];
      });
    }
  } catch (error) {
    console.error('获取医生列表失败:', error);
    allDoctors.value = [];
  } finally {
    loading.value = false;
  }
};

// 按科室分组医生
const groupDoctorsByDepartment = () => {
  // 为每个科室分配医生
  departmentList.value.forEach(dept => {
    // 如果医生有 departmentId，按科室过滤；否则显示所有医生
    if (allDoctors.value.length > 0 && allDoctors.value[0].departmentId) {
      dept.doctors = allDoctors.value
        .filter(doctor => doctor.departmentId === dept.departmentId)
        .map(doctor => ({
          doctorId: doctor.doctorId,
          name: doctor.doctorName,
          title: doctor.title || '医师',
          photo: doctor.avatarUrl || 'https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?q=80&w=200&auto=format&fit=crop',
          skill: doctor.doctorIntro || '暂无介绍'
        }));
    } else {
      // 暂时将所有医生都显示，因为医生表中没有 departmentId
      dept.doctors = allDoctors.value.map(doctor => ({
        doctorId: doctor.doctorId,
        name: doctor.doctorName,
        title: doctor.title || '医师',
        photo: doctor.avatarUrl || 'https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?q=80&w=200&auto=format&fit=crop',
        skill: doctor.doctorIntro || '暂无介绍'
      }));
    }
  });
};

// 搜索处理
const handleSearch = () => {
  loadDoctors();
};

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.src = 'https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?q=80&w=200&auto=format&fit=crop';
};

// 监听院区切换
watch(activeCampus, () => {
  searchKeyword.value = '';
  loadDepartments();
  loadDoctors();
});

// 组件挂载时加载数据
onMounted(() => {
  loadDepartments();
  loadDoctors();
});
</script>

<style scoped>
/* 复用大部分样式，增加 Modal 样式 */
.specialist-page { min-height: 100vh; background: #f4f6f9; font-family: 'Helvetica Neue', Arial, sans-serif; }
.main-header { height: 80px; background: white; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #ddd; }
.header-inner { width: 100%; max-width: 1400px; padding: 0 40px; display: flex; justify-content: space-between; align-items: center; }
.logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.logo-icon { font-size: 2.2rem; }
.logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
.logo-text small { font-size: 0.6rem; color: #666; }
.back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
.top-banner-section { background: white; }
.banner-bg { height: 200px; background: linear-gradient(rgba(0,0,0,0.3), rgba(0,0,0,0.3)), url('https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?q=80&w=2000&auto=format&fit=crop'); background-size: cover; background-position: center; display: flex; align-items: center; padding-left: 10%; }
.banner-text h1 { color: white; font-size: 2.5rem; text-shadow: 2px 2px 4px rgba(0,0,0,0.5); }
.breadcrumb-strip { background: #f0ad4e; height: 50px; display: flex; align-items: center; padding-left: 10%; color: white; }
.breadcrumb-strip .container { display: flex; align-items: center; gap: 10px; }
.campus-tabs-bar { background: #2f80ed; height: 60px; }
.tabs-inner { max-width: 1400px; margin: 0 auto; padding: 0 40px; height: 100%; display: flex; justify-content: space-between; align-items: center; }
.tabs-list { display: flex; height: 100%; }
.tab-item { color: rgba(255,255,255,0.7); padding: 0 30px; font-size: 1.1rem; font-weight: bold; cursor: pointer; position: relative; height: 100%; display: flex; align-items: center; }
.tab-item:hover, .tab-item.active { background: #216bd4; color: white; }
.tab-search-box { display: flex; align-items: center; gap: 10px; }
.input-inner { position: relative; width: 220px; }
.input-inner input { width: 100%; padding: 6px 15px 6px 35px; border-radius: 20px; border: 1px solid rgba(255,255,255,0.4); background: rgba(255,255,255,0.15); color: white; outline: none; }
.search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: rgba(255,255,255,0.7); }
.btn-tab-search { background: white; color: #2f80ed; border: none; padding: 6px 20px; border-radius: 20px; font-weight: bold; cursor: pointer; }
.main-content { padding: 40px 0; background: #f4f6f9; }
.content-container { max-width: 1400px; margin: 0 auto; padding: 0 40px; }
.dept-section { margin-bottom: 30px; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.dept-header { background: linear-gradient(90deg, #2385fc 0%, #4facfe 100%); color: white; padding: 25px 30px; display: flex; justify-content: space-between; }
.dept-title-border { margin: 0 0 10px 0; font-size: 1.3rem; border-left: 4px solid white; padding-left: 10px; }
.dept-desc { margin: 0; font-size: 0.9rem; opacity: 0.9; }
.see-more { cursor: pointer; display: flex; align-items: center; gap: 5px; opacity: 0.9; }
.doctor-grid { padding: 30px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; background: #fcfcfc; }
.doctor-card { background: white; border: 1px solid #eee; border-radius: 4px; padding: 20px; display: flex; gap: 15px; transition: 0.3s; }
.doctor-card:hover { box-shadow: 0 10px 20px rgba(0,0,0,0.08); transform: translateY(-2px); border-color: #2385fc; }
.doc-photo img { width: 80px; height: 100px; object-fit: cover; border-radius: 4px; }
.doc-info { flex: 1; display: flex; flex-direction: column; }
.doc-name { font-size: 1.1rem; font-weight: bold; color: #333; }
.doc-title { font-size: 0.8rem; color: #666; margin-left: 5px; }
.doc-dept { font-size: 0.85rem; color: #999; margin-bottom: 5px; }
.doc-skill { font-size: 0.8rem; color: #555; height: 35px; overflow: hidden; margin-bottom: 10px; }
.doc-action { margin-top: auto; }
.btn-see-reviews { width: 100%; background: #e3f2fd; color: #004ea2; border: none; padding: 5px; border-radius: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 5px; font-size: 0.8rem; }
.btn-see-reviews:hover { background: #bbdefb; }

/* 弹窗样式 */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 2000; display: flex; align-items: center; justify-content: center; }
.modal-box { background: white; width: 600px; border-radius: 12px; overflow: hidden; display: flex; flex-direction: column; max-height: 80vh; }
.modal-header { padding: 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: flex-start; background: #fcfcfc; }
.modal-header h3 { margin: 0; font-size: 1.2rem; }
.btn-close { background: none; border: none; cursor: pointer; font-size: 1.5rem; color: #999; }
.review-body { padding: 20px; overflow-y: auto; max-height: 500px; }
.loading-reviews { text-align: center; padding: 40px; color: #999; }
.loading-icon { font-size: 2rem; animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.no-reviews { text-align: center; padding: 40px; color: #999; }
.gray-icon { font-size: 3rem; margin-bottom: 10px; }
.review-item { border-bottom: 1px dashed #eee; padding-bottom: 15px; margin-bottom: 15px; }
.review-item:last-child { border-bottom: none; }
.rev-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.rev-user { font-weight: bold; color: #333; }
.rev-stars { display: flex; gap: 2px; }
.star-yellow { color: #ffca28; font-size: 0.9rem; }
.star-gray { color: #ddd; font-size: 0.9rem; }
.rev-time { color: #999; font-size: 0.85rem; margin-left: auto; }
.rev-content { color: #666; line-height: 1.6; }
.doctor-header-info { flex: 1; }
.rating-summary { display: flex; align-items: center; gap: 10px; margin-top: 10px; }
.rating-stars { display: flex; gap: 2px; }
.rating-score { font-size: 1.2rem; font-weight: bold; color: #ff9800; }
.rating-count { font-size: 0.9rem; color: #666; }
.rev-time { margin-left: auto; font-size: 0.8rem; color: #999; }
.rev-content { color: #555; font-size: 0.95rem; line-height: 1.5; }
.app-footer { background: #1a3a6e; color: rgba(255,255,255,0.6); text-align: center; padding: 20px; margin-top: 50px; }

/* 加载和空状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 1.1rem;
}
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}
.no-doctors {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  background: #fcfcfc;
}

/* 加载和空状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 1.1rem;
}
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}
.no-doctors {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  background: #fcfcfc;
}
</style>

<!-- <template>
    <div class="specialist-page">
      
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
            <h1>专家介绍</h1>
          </div>
        </div>
        
        <div class="breadcrumb-strip">
          <div class="container">
            <span @click="router.push('/user')" style="cursor:pointer">网站首页</span> 
            <Icon icon="mdi:chevron-right" />
            <span>就诊指南</span>
            <Icon icon="mdi:chevron-right" />
            <span class="current">专家介绍</span>
          </div>
          <div class="strip-shape"></div>
        </div>
      </div>
  
      <div class="campus-tabs-bar">
        <div class="tabs-inner">
          
          <div class="tabs-list">
            <div 
              v-for="campus in ['朝晖院区', '望江山院区', '越城院区']" 
              :key="campus"
              class="tab-item"
              :class="{ active: activeCampus === campus }"
              @click="activeCampus = campus"
            >
              {{ campus }}
              <div class="triangle" v-if="activeCampus === campus"></div>
            </div>
          </div>
  
          <div class="tab-search-box">
            <div class="input-inner">
              <Icon icon="mdi:magnify" class="search-icon" />
              <input type="text" placeholder="请输入专家姓名" />
            </div>
            <button class="btn-tab-search">搜索</button>
          </div>
  
        </div>
      </div>
  
      <main class="main-content">
        <div class="content-container">
          <div class="dept-section" v-for="dept in departmentList" :key="dept.id">
            <div class="dept-header">
              <div class="dept-info">
                <h2 class="dept-title-border">{{ dept.name }}</h2>
                <p class="dept-desc">{{ dept.desc }}</p>
              </div>
              <div class="see-more">
                查看更多 <Icon icon="mdi:arrow-right-thin" />
              </div>
            </div>
            <div class="doctor-grid">
              <div class="doctor-card" v-for="doc in dept.doctors" :key="doc.name">
                <div class="doc-photo">
                  <img :src="doc.photo" :alt="doc.name">
                </div>
                <div class="doc-info">
                  <div class="doc-top">
                    <span class="doc-name">{{ doc.name }}</span>
                    <span class="doc-title">{{ doc.title }}</span>
                  </div>
                  <div class="doc-dept">{{ dept.name }}</div>
                  <div class="doc-skill">
                    <span class="label">擅长：</span>
                    {{ doc.skill }}
                  </div>
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
  
      <div class="float-back-top" @click="scrollToTop">
         <Icon icon="mdi:arrow-up" />
         <span>顶部</span>
      </div>
  
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { Icon } from '@iconify/vue';
  
  const router = useRouter();
  const activeCampus = ref('朝晖院区');
  
  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };
  
  // --- 模拟数据 ---
  const departmentList = [
    {
      id: 1,
      name: '急诊医学科',
      desc: '专业优势：严重多发伤、重症急性胰腺炎、心肺脑复苏、中毒、脓毒症、多脏器功能衰竭等急危重症救治。',
      doctors: [
        { name: '蔡文伟', title: '主任医师', photo: 'https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?q=80&w=200&auto=format&fit=crop', skill: '急性腹痛、严重多发伤、急性胰腺炎...' },
        { name: '李茜', title: '主任医师', photo: 'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?q=80&w=200&auto=format&fit=crop', skill: '各种重症疾病如多脏器功能衰竭...' },
        { name: '郑悦亮', title: '主任医师', photo: 'https://images.unsplash.com/photo-1537368910025-700350fe46c7?q=80&w=200&auto=format&fit=crop', skill: '危重患者的诊治，对重症胰腺炎...' },
        { name: '张可', title: '副主任医师', photo: 'https://images.unsplash.com/photo-1622253692010-333f2da6031d?q=80&w=200&auto=format&fit=crop', skill: '危重患者的诊治，对心脏骤停的抢救...' }
      ]
    },
    {
      id: 2,
      name: '重症医学科',
      desc: '专业优势：集急性肾损伤(AKI)的早期预警、诊断与治疗(全方位血液净化)于一体的精准AKI诊治。',
      doctors: [
        { name: '杨向红', title: '主任医师', photo: 'https://images.unsplash.com/photo-1594824476969-519478cae374?q=80&w=200&auto=format&fit=crop', skill: '各种急危重病人的抢救治疗及多脏器...' },
        { name: '孙仁华', title: '主任医师', photo: 'https://images.unsplash.com/photo-1612531386530-97286d74c2ea?q=80&w=200&auto=format&fit=crop', skill: '各种急重病人的诊治，尤其在ECMO...' },
        { name: '呼邦传', title: '主任医师', photo: 'https://images.unsplash.com/photo-1537368910025-700350fe46c7?q=80&w=200&auto=format&fit=crop', skill: '从事重症临床工作20余年...' },
        { name: '洪军', title: '主任医师', photo: 'https://images.unsplash.com/photo-1622253692010-333f2da6031d?q=80&w=200&auto=format&fit=crop', skill: '临床危重症患者的生命支持技术...' }
      ]
    }
  ];
  </script>
  
  <style scoped>
  .specialist-page { min-height: 100vh; background: #f4f6f9; font-family: 'Helvetica Neue', Arial, sans-serif; padding-bottom: 0; }
  
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
    background: linear-gradient(rgba(0,0,0,0.3), rgba(0,0,0,0.3)), url('https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?q=80&w=2000&auto=format&fit=crop');
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
  
  /* ★★★ 新增：Tabs + Search Bar 混合布局 ★★★ */
  .campus-tabs-bar { background: #2f80ed; height: 60px; /* 设定高度 */ }
  .tabs-inner { 
    max-width: 1400px; margin: 0 auto; padding: 0 40px; 
    height: 100%;
    display: flex; 
    justify-content: space-between; /* 左右分布 */
    align-items: center; /* 垂直居中 */
  }
  .tabs-list { display: flex; height: 100%; }
  .tab-item { 
    color: rgba(255,255,255,0.7); 
    padding: 0 30px; /* 减少内边距 */
    font-size: 1.1rem; 
    font-weight: bold; 
    cursor: pointer; 
    position: relative; 
    transition: 0.3s; 
    height: 100%;
    display: flex; align-items: center; /* 文字垂直居中 */
  }
  .tab-item:hover, .tab-item.active { background: #216bd4; color: white; }
  .triangle { position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 0; height: 0; border-left: 8px solid transparent; border-right: 8px solid transparent; border-top: 8px solid #216bd4; }
  
  /* 搜索框 (嵌入在右侧) */
  .tab-search-box { display: flex; align-items: center; gap: 10px; }
  .input-inner { position: relative; width: 220px; }
  .input-inner input {
    width: 100%;
    padding: 6px 15px 6px 35px;
    border-radius: 20px;
    border: 1px solid rgba(255,255,255,0.4);
    background: rgba(255,255,255,0.15); /* 半透明背景 */
    color: white;
    outline: none;
    font-size: 0.9rem;
    transition: 0.3s;
  }
  .input-inner input::placeholder { color: rgba(255,255,255,0.6); }
  .input-inner input:focus { background: white; color: #333; }
  .search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: rgba(255,255,255,0.7); }
  /* 输入框激活时图标变黑 */
  .input-inner input:focus + .search-icon { color: #666; } 
  
  .btn-tab-search {
    background: white; color: #2f80ed; border: none; 
    padding: 6px 20px; border-radius: 20px; 
    font-weight: bold; cursor: pointer; transition: 0.2s;
  }
  .btn-tab-search:hover { background: #e6f0ff; }
  
  
  /* 内容区 */
  .main-content { padding: 40px 0; background: #f4f6f9; }
  .content-container { max-width: 1400px; margin: 0 auto; padding: 0 40px; }
  .dept-section { margin-bottom: 30px; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.05); display: flex; flex-direction: column; }
  .dept-header { background: linear-gradient(90deg, #2385fc 0%, #4facfe 100%); color: white; padding: 25px 30px; display: flex; justify-content: space-between; align-items: flex-start; }
  .dept-info { max-width: 80%; }
  .dept-title-border { margin: 0 0 10px 0; font-size: 1.3rem; border-left: 4px solid white; padding-left: 10px; line-height: 1; }
  .dept-desc { margin: 0; font-size: 0.9rem; opacity: 0.9; line-height: 1.5; }
  .see-more { cursor: pointer; font-size: 0.9rem; display: flex; align-items: center; gap: 5px; opacity: 0.9; }
  
  /* 医生列表 */
  .doctor-grid { padding: 30px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; background: #fcfcfc; }
  .doctor-card { background: white; border: 1px solid #eee; border-radius: 4px; padding: 20px; display: flex; gap: 15px; transition: 0.3s; }
  .doctor-card:hover { box-shadow: 0 10px 20px rgba(0,0,0,0.08); transform: translateY(-2px); border-color: #2385fc; }
  .doc-photo img { width: 80px; height: 100px; object-fit: cover; border-radius: 4px; background: #eee; }
  .doc-info { flex: 1; display: flex; flex-direction: column; }
  .doc-top { margin-bottom: 5px; display: flex; align-items: baseline; gap: 10px; flex-wrap: wrap; }
  .doc-name { font-size: 1.1rem; font-weight: bold; color: #333; }
  .doc-title { font-size: 0.8rem; color: #666; }
  .doc-dept { font-size: 0.85rem; color: #999; margin-bottom: 10px; }
  .doc-skill { font-size: 0.8rem; color: #555; line-height: 1.5; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; }
  .doc-skill .label { color: #2385fc; font-weight: bold; }
  
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
  .footer-link-list li:hover { color: white; transform: translateX(5px); }
  .social-icons { display: flex; gap: 15px; margin: 30px 0; }
  .icon-box { width: 40px; height: 40px; border: 1px solid rgba(255,255,255,0.3); border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; cursor: pointer; }
  .footer-bottom-bar { position: relative; z-index: 2; background: #132b52; text-align: center; padding: 20px; font-size: 0.8rem; color: rgba(255,255,255,0.5); }
  .tech-support { float: right; margin-right: 40px; }
  
  /* 悬浮按钮 */
  .float-back-top {
    position: fixed; right: 30px; bottom: 100px; width: 50px; height: 50px; background: rgba(0,0,0,0.5); color: white;
    display: flex; flex-direction: column; align-items: center; justify-content: center; font-size: 0.8rem; cursor: pointer; border-radius: 4px;
  }
  .float-back-top:hover { background: #004ea2; }
  </style> -->