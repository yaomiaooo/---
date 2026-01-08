<template>
    <div class="visit-record-page">
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
        <div class="banner-bg"><div class="banner-text"><h1>就诊记录档案</h1></div></div>
        <div class="breadcrumb-strip">
          <div class="container">
            <span @click="router.push('/user')" style="cursor: pointer">网站首页</span> 
            <Icon icon="mdi:chevron-right" /><span>就诊指南</span><Icon icon="mdi:chevron-right" /><span class="current">就诊记录</span>
          </div>
          <div class="strip-shape"></div>
        </div>
      </div>
  
      <main class="main-content">
        <div class="content-container">
          <div class="search-panel">
            <div class="panel-title"><Icon icon="mdi:filter-variant" /> 数据筛选</div>
            <div class="search-inputs">
              <div class="input-group"><label>就诊人姓名：</label><input type="text" v-model="filters.patientName" placeholder="请输入姓名"></div>
              <div class="input-group"><label>就诊日期：</label><input type="date" v-model="filters.date"></div>
              <button class="btn-search" @click="handleSearch"><Icon icon="mdi:magnify" /> 查询</button>
              <button class="btn-reset" @click="resetSearch">重置</button>
            </div>
          </div>
  
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>就诊记录编号</th><th>就诊时间</th><th>就诊人</th><th>医生</th><th>院区</th><th width="200">诊断结果</th><th>评价状态</th><th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="loading"><td colspan="8" class="empty-cell"><div class="empty-box"><Icon icon="mdi:loading" class="empty-icon" /><p>加载中...</p></div></td></tr>
                <tr v-else-if="filteredList.length === 0"><td colspan="8" class="empty-cell"><div class="empty-box"><Icon icon="mdi:database-off" class="empty-icon" /><p>暂无相关记录</p></div></td></tr>
                <tr v-else v-for="item in filteredList" :key="item.recordId">
                  <td class="col-id">{{ item.recordId }}</td>
                  <td class="col-time">{{ item.visitTime }} {{ item.timeSlot || '' }}</td>
                  <td class="col-name">{{ item.patientName }}</td>
                  <td class="col-doc">{{ item.doctorName }} {{ item.doctorTitle ? `(${item.doctorTitle})` : '' }}</td>
                  <td class="col-id">{{ item.campusName }}</td>
                  <td class="col-diag">
                    <span class="diag-tag" v-if="item.diagnosis">{{ item.diagnosis }}</span>
                    <span class="diag-tag" v-else style="background: #f0f0f0; color: #999;">待诊断</span>
                  </td>
                  <td>
                    <span v-if="hasReviewed(item)" class="status-reviewed"><Icon icon="mdi:check-circle" /> 已评价</span>
                    <span v-else class="status-pending">未评价</span>
                  </td>
                  <td>
                    <button v-if="!hasReviewed(item)" class="btn-review" @click="openReviewModal(item)">去评价</button>
                    <button v-else class="btn-link disabled">查看评价</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </main>
  
      <div class="modal-overlay" v-if="showReviewModal">
        <div class="modal-box fade-in-up">
          <div class="modal-header"><h3>就诊评价</h3><button class="btn-close" @click="closeReviewModal"><Icon icon="mdi:close" /></button></div>
          <div class="modal-body">
            <p class="review-target">您正在评价 <strong>{{ currentReviewItem.doctorName }}</strong> 医生的诊疗服务</p>
            <div class="form-group">
              <label>评分：</label>
              <div class="star-rating">
                <Icon 
                  v-for="n in 5" :key="n" 
                  :icon="n <= reviewForm.rating ? 'mdi:star' : 'mdi:star-outline'" 
                  class="star-icon" 
                  :class="{ active: n <= reviewForm.rating }"
                  @click="!currentReviewItem.review && (reviewForm.rating = n)"
                  :style="{ cursor: currentReviewItem.review ? 'default' : 'pointer' }"
                />
                <span class="rating-text">{{ reviewForm.rating }}分</span>
              </div>
            </div>
            <div class="form-group">
              <label>评价内容：</label>
              <textarea 
                v-model="reviewForm.content" 
                rows="4" 
                placeholder="医生专业吗？服务态度好吗？请分享您的就医体验..."
                :disabled="!!currentReviewItem.review"
              ></textarea>
            </div>
            <div v-if="currentReviewItem.review" class="review-note">
              <Icon icon="mdi:information-outline" /> 该就诊记录已评价，无法修改
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="closeReviewModal">关闭</button>
            <button v-if="!currentReviewItem.review" class="btn-confirm" @click="submitReview" :disabled="loading">提交评价</button>
          </div>
        </div>
      </div>
  
      <footer class="app-footer">
        <div class="footer-bottom-bar">Copyright © 2025 浙江工业大学健行医院网站版权所有</div>
      </footer>
    </div>
  </template>
  
<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { getMyVisits } from '../api/visit';
import { createReview, getReviewByAppointmentId } from '../api/review';

const router = useRouter();
const filters = ref({ patientName: '', date: '' });
const showReviewModal = ref(false);
const currentReviewItem = ref({});
const reviewForm = ref({ rating: 5, content: '' });
const myReviews = ref([]);
const loading = ref(false);

// 就诊记录列表
const visitList = ref([]);

// 加载就诊记录
const loadVisits = async () => {
  loading.value = true;
  try {
    const res = await getMyVisits();
    console.log('就诊记录API响应:', res);
    if (res.code === 200 && res.data) {
      console.log('就诊记录数据:', res.data);
      // 转换数据格式以匹配前端显示
      visitList.value = res.data.map(visit => {
        const visitTime = visit.visitTime ? 
          (typeof visit.visitTime === 'string' ? visit.visitTime.split('T')[0] : visit.visitTime.toString().split('T')[0]) : '';
        return {
          visitId: visit.visitId,
          appointmentId: visit.appointmentId,
          recordId: visit.visitId, // 用于兼容现有代码
          visitTime: visitTime,
          patientName: visit.patientName || '未知',
          doctorName: visit.doctorName || '未知',
          doctorTitle: visit.doctorTitle || '',
          campusName: visit.hospitalName || '未知院区',
          diagnosis: visit.diagnosis || '待诊断',
          timeSlot: visit.timeSlot || '',
          review: visit.review // 评价信息
        };
      });
      console.log('转换后的就诊记录列表:', visitList.value);
    } else {
      console.error('获取就诊记录失败:', res.message);
      visitList.value = [];
    }
  } catch (error) {
    console.error('获取就诊记录失败:', error);
    visitList.value = [];
  } finally {
    loading.value = false;
  }
};

// 初始化
onMounted(() => {
  loadVisits();
});

// 检查是否已评价
const hasReviewed = (item) => {
  // 如果就诊记录中有评价信息，说明已评价
  return item.review != null && item.review.reviewId != null;
};

const filteredList = computed(() => {
  let result = visitList.value;
  
  // 按就诊人姓名筛选
  if (filters.value.patientName && filters.value.patientName.trim()) {
    result = result.filter(item => 
      item.patientName.includes(filters.value.patientName.trim())
    );
  }
  
  // 按日期筛选
  if (filters.value.date) {
    result = result.filter(item => item.visitTime === filters.value.date);
  }
  
  return result;
});

// 打开评价弹窗
const openReviewModal = (item) => {
  currentReviewItem.value = item;
  // 如果已有评价，显示评价内容
  if (item.review) {
    reviewForm.value = { 
      rating: item.review.rating || 5, 
      content: item.review.content || '' 
    };
  } else {
    reviewForm.value = { rating: 5, content: '' };
  }
  showReviewModal.value = true;
};

const closeReviewModal = () => showReviewModal.value = false;

// 提交评价
const submitReview = async () => {
  if (!reviewForm.value.content || !reviewForm.value.content.trim()) {
    alert('请输入评价内容');
    return;
  }

  // 如果已有评价，提示不能重复评价
  if (currentReviewItem.value.review) {
    alert('该就诊记录已评价，无法重复评价');
    return;
  }

  loading.value = true;
  try {
    const res = await createReview({
      appointmentId: currentReviewItem.value.appointmentId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content.trim()
    });

    if (res.code === 200 && res.data) {
      alert('评价提交成功！');
      // 更新当前项的评价信息
      currentReviewItem.value.review = res.data;
      // 重新加载就诊记录列表
      await loadVisits();
      closeReviewModal();
    } else {
      alert(res.message || '评价提交失败，请重试');
    }
  } catch (error) {
    console.error('提交评价失败:', error);
    alert(error.message || '评价提交失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  // 搜索逻辑已在 computed 中实现
};

const resetSearch = () => { 
  filters.value.patientName = ''; 
  filters.value.date = '';
};
</script>
  
  <style scoped>
  /* 复用之前的 CSS 结构，新增评价相关样式 */
  .visit-record-page { min-height: 100vh; background: #f4f6f9; font-family: 'Helvetica Neue', Arial, sans-serif; }
  .main-header { height: 80px; background: white; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #ddd; }
  .header-inner { width: 100%; max-width: 1400px; padding: 0 40px; display: flex; justify-content: space-between; align-items: center; }
  .logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
  .logo-icon { font-size: 2.2rem; }
  .logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
  .logo-text small { font-size: 0.6rem; color: #666; }
  .back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
  .top-banner-section { background: white; }
  .banner-bg { height: 160px; background: linear-gradient(rgba(0,0,0,0.7), rgba(0,0,0,0.7)), url('https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?q=80&w=2000'); background-size: cover; background-position: center; display: flex; align-items: center; padding-left: 10%; }
  .banner-text h1 { color: white; font-size: 2.2rem; }
  .breadcrumb-strip { background: #f0ad4e; height: 50px; display: flex; align-items: center; padding-left: 10%; color: white; }
  .breadcrumb-strip .container { display: flex; align-items: center; gap: 10px; }
  .main-content { padding: 40px 0; }
  .content-container { max-width: 1400px; margin: 0 auto; padding: 0 40px; }
  .search-panel { background: white; padding: 25px; border-radius: 8px; margin-bottom: 30px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
  .search-inputs { display: flex; gap: 20px; align-items: center; flex-wrap: nowrap; }
  .input-group { display: flex; align-items: center; gap: 8px; white-space: nowrap; }
  .input-group label { font-size: 0.9rem; color: #666; }
  .input-group input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; min-width: 150px; }
  .btn-search { background: #004ea2; color: white; border: none; padding: 8px 25px; border-radius: 4px; cursor: pointer; display: flex; align-items: center; gap: 5px; white-space: nowrap; flex-shrink: 0; }
  .btn-reset { background: white; border: 1px solid #ddd; padding: 8px 20px; border-radius: 4px; cursor: pointer; white-space: nowrap; flex-shrink: 0; }
  .table-container { background: white; border-radius: 8px; overflow: hidden; min-height: 400px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
  .data-table { width: 100%; border-collapse: collapse; text-align: left; }
  .data-table th { background: #f8f9fa; padding: 15px 20px; font-weight: bold; color: #444; border-bottom: 2px solid #eee; }
  .data-table td { padding: 15px 20px; border-bottom: 1px solid #f0f0f0; color: #555; }
  .col-id { font-family: monospace; font-weight: bold; }
  .col-doc { color: #004ea2; font-weight: bold; }
  .diag-tag { background: #e3f2fd; color: #004ea2; padding: 4px 10px; border-radius: 20px; font-size: 0.85rem; }
  .status-reviewed { color: #28a745; display: flex; align-items: center; gap: 5px; }
  .status-pending { color: #999; }
  .btn-review { background: #ff9800; color: white; border: none; padding: 6px 15px; border-radius: 4px; cursor: pointer; transition: 0.2s; }
  .btn-review:hover { background: #f57c00; }
  .btn-link.disabled { color: #ccc; background: none; border: none; cursor: default; }
  .review-note { margin-top: 15px; padding: 10px; background: #fff3cd; color: #856404; border-radius: 4px; display: flex; align-items: center; gap: 5px; font-size: 0.9rem; }
  
  /* 弹窗样式 */
  .modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 2000; display: flex; align-items: center; justify-content: center; }
  .modal-box { background: white; width: 500px; border-radius: 12px; padding: 0; overflow: hidden; }
  .modal-header { padding: 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; background: #fcfcfc; }
  .modal-header h3 { margin: 0; }
  .modal-body { padding: 30px; }
  .review-target { margin-bottom: 20px; color: #666; }
  .star-rating { display: flex; gap: 5px; align-items: center; font-size: 2rem; color: #ddd; cursor: pointer; }
  .star-icon.active { color: #ffca28; }
  .rating-text { font-size: 1rem; color: #666; margin-left: 10px; }
  .form-group textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; margin-top: 10px; }
  .modal-footer { padding: 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 15px; }
  .btn-confirm { background: #004ea2; color: white; border: none; padding: 8px 25px; border-radius: 6px; cursor: pointer; }
  .app-footer { background: #1a3a6e; color: rgba(255,255,255,0.6); text-align: center; padding: 20px; margin-top: 50px; }
  </style>