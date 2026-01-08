<template>
    <div class="profile-page">
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
        <div class="banner-bg"><div class="banner-text"><h1>我的信息</h1></div></div>
        <div class="breadcrumb-strip">
          <div class="container">
            <span @click="router.push('/user')">网站首页</span><Icon icon="mdi:chevron-right" /><span class="current">个人中心</span>
          </div>
          <div class="strip-shape"></div>
        </div>
      </div>
  
      <main class="main-content">
        <div class="content-container">
          <div class="profile-layout">
            <div class="profile-sidebar">
              <div class="avatar-box">
                <div class="avatar-circle"><img :src="userInfo.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'" alt="avatar"></div>
                <h3 class="user-name">{{ userInfo.userId || '加载中...' }}</h3>
                <span class="user-role">{{ userInfo.role === 'admin' ? '管理员' : userInfo.role === 'doctor' ? '医生' : '普通用户' }}</span>
              </div>
              <div class="sidebar-menu">
                <div class="menu-item" :class="{ active: currentTab === 'info' }" @click="currentTab = 'info'"><Icon icon="mdi:account-details" /> 基本资料</div>
                <div class="menu-item" :class="{ active: currentTab === 'password' }" @click="currentTab = 'password'"><Icon icon="mdi:lock-reset" /> 修改密码</div>
                <div class="menu-item" :class="{ active: currentTab === 'reviews' }" @click="currentTab = 'reviews'"><Icon icon="mdi:comment-quote" /> 我的评价</div>
                <div class="menu-item" @click="logout"><Icon icon="mdi:logout" /> 退出登录</div>
              </div>
            </div>
  
            <div class="profile-main">
              <div v-if="currentTab === 'info'">
                <div class="panel-header">
                  <h2>基本资料</h2>
                  <button class="btn-edit" @click="toggleEdit"><Icon :icon="isEditing ? 'mdi:content-save' : 'mdi:pencil'" /> {{ isEditing ? '保存修改' : '编辑资料' }}</button>
                </div>
                <div class="form-container">
                  <div class="form-group">
                    <label>用户编号</label>
                    <input type="text" v-model="userInfo.userId" disabled class="input-disabled">
                  </div>
                  <div class="form-group">
                    <label>手机号码</label>
                    <input type="text" v-model="userInfo.userPhone" :disabled="!isEditing" :class="{ 'input-edit': isEditing }" placeholder="请输入手机号码">
                  </div>
                </div>
              </div>

              <div v-if="currentTab === 'password'">
                <div class="panel-header">
                  <h2>修改密码</h2>
                </div>
                <div class="form-container">
                  <div class="form-group">
                    <label>旧密码</label>
                    <input type="password" v-model="passwordForm.oldPassword" class="input-edit" placeholder="请输入旧密码">
                  </div>
                  <div class="form-group">
                    <label>新密码</label>
                    <input type="password" v-model="passwordForm.newPassword" class="input-edit" placeholder="请输入新密码（至少6位）">
                    <small class="form-hint">密码长度至少为6位</small>
                  </div>
                  <div class="form-group">
                    <label>确认新密码</label>
                    <input type="password" v-model="passwordForm.confirmPassword" class="input-edit" placeholder="请再次输入新密码">
                  </div>
                  <div class="form-actions">
                    <button class="btn-submit" @click="submitPasswordChange" :disabled="changingPassword">
                      <Icon icon="mdi:content-save" /> {{ changingPassword ? '修改中...' : '确认修改' }}
                    </button>
                    <button class="btn-cancel" @click="resetPasswordForm">重置</button>
                  </div>
                </div>
              </div>

              <div v-if="currentTab === 'reviews'">
                <div class="panel-header"><h2>我的评价记录</h2></div>
                <div v-if="loadingReviews" class="empty-reviews">
                  <Icon icon="mdi:loading" class="empty-icon" /><p>加载中...</p>
                </div>
                <div v-else-if="myReviews.length === 0" class="empty-reviews">
                  <Icon icon="mdi:comment-remove-outline" class="empty-icon" /><p>您还没有发表过任何评价</p>
                </div>
                <div v-else class="reviews-list">
                  <div v-for="rev in myReviews" :key="rev.reviewId" class="my-review-card">
                    <div class="rev-header">
                      <span class="rev-doc">评价对象：{{ rev.doctorName || '未知医生' }} 医生</span>
                      <span class="rev-date">{{ formatReviewDate(rev.createdAt) }}</span>
                    </div>
                    <div class="rev-rating">
                      <Icon v-for="n in 5" :key="n" :icon="n <= rev.rating ? 'mdi:star' : 'mdi:star-outline'" :class="n <= rev.rating ? 'star-yellow' : 'star-gray'" />
                    </div>
                    <p class="rev-text">{{ rev.content }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
  
      <footer class="app-footer">
        <div class="footer-bottom-bar">Copyright © 2025 浙江工业大学健行医院网站版权所有</div>
      </footer>
    </div>
  </template>
  
<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { getUserProfile, updateUserProfile, changePassword } from '../api/user';
import { getMyReviews } from '../api/review';

const router = useRouter();
const currentTab = ref('info');
const isEditing = ref(false);
const myReviews = ref([]);
const loading = ref(false);
const loadingReviews = ref(false);

const userInfo = ref({
  userId: '',
  userPhone: '',
  role: ''
});

// 修改密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});
const changingPassword = ref(false);

// 加载用户信息
const loadUserInfo = async () => {
  loading.value = true;
  try {
    const res = await getUserProfile();
    if (res.code === 200 && res.data) {
      userInfo.value = {
        userId: res.data.userId || '',
        userPhone: res.data.userPhone || '',
        role: res.data.role || 'user'
      };
      // 保存原始手机号，用于判断是否修改
      userInfo.value.originalPhone = res.data.userPhone || '';
    } else {
      console.error('获取用户信息失败:', res.message);
      alert('获取用户信息失败，请重新登录');
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    alert('获取用户信息失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

// 保存用户信息
const saveUserInfo = async () => {
  const phone = userInfo.value.userPhone?.trim();
  
  if (!phone || phone === '') {
    alert('请输入手机号码');
    return;
  }

  // 简单的手机号格式验证：11位数字，以1开头
  const phoneRegex = /^1\d{10}$/;
  if (!phoneRegex.test(phone)) {
    alert('请输入正确的手机号码格式（11位数字，以1开头）');
    return;
  }

  // 如果手机号没有变化，直接退出编辑模式
  const originalPhone = userInfo.value.originalPhone || '';
  if (phone === originalPhone) {
    isEditing.value = false;
    return;
  }

  loading.value = true;
  try {
    const res = await updateUserProfile({
      phone: phone
    });
    
    if (res.code === 200) {
      alert('信息保存成功！');
      isEditing.value = false;
      // 重新加载用户信息以确保数据同步
      await loadUserInfo();
    } else {
      // 显示后端返回的错误信息
      alert(res.message || '保存失败，请重试');
    }
  } catch (error) {
    console.error('保存用户信息失败:', error);
    // 尝试从错误中获取错误信息
    // 如果是在响应拦截器中 reject 的错误，message 已经包含错误信息
    const errorMessage = error.message || error.response?.data?.message || '保存失败，请检查网络连接';
    alert(errorMessage);
  } finally {
    loading.value = false;
  }
};

const toggleEdit = () => {
  if (isEditing.value) {
    // 保存修改
    saveUserInfo();
  } else {
    // 进入编辑模式，保存当前手机号作为原始值
    userInfo.value.originalPhone = userInfo.value.userPhone;
    isEditing.value = true;
  }
};

// 提交密码修改
const submitPasswordChange = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value;

  // 验证表单
  if (!oldPassword || !oldPassword.trim()) {
    alert('请输入旧密码');
    return;
  }

  if (!newPassword || !newPassword.trim()) {
    alert('请输入新密码');
    return;
  }

  if (newPassword.length < 6) {
    alert('新密码长度至少为6位');
    return;
  }

  if (!confirmPassword || !confirmPassword.trim()) {
    alert('请确认新密码');
    return;
  }

  if (newPassword !== confirmPassword) {
    alert('两次输入的新密码不一致');
    return;
  }

  if (oldPassword === newPassword) {
    alert('新密码不能与旧密码相同');
    return;
  }

  changingPassword.value = true;
  try {
    const res = await changePassword({
      oldPassword: oldPassword.trim(),
      newPassword: newPassword.trim()
    });

    if (res.code === 200) {
      alert('密码修改成功！');
      resetPasswordForm();
    } else {
      alert(res.message || '修改密码失败，请重试');
    }
  } catch (error) {
    console.error('修改密码失败:', error);
    alert(error.message || '修改密码失败，请检查网络连接');
  } finally {
    changingPassword.value = false;
  }
};

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
};

// 加载我的评价
const loadMyReviews = async () => {
  loadingReviews.value = true;
  try {
    const res = await getMyReviews();
    console.log('获取评价列表API响应:', res);
    if (res.code === 200 && res.data) {
      myReviews.value = res.data.map(review => ({
        reviewId: review.reviewId,
        doctorName: review.doctorName || '未知医生',
        rating: review.rating || 5,
        content: review.content || '',
        createdAt: review.createdAt
      }));
      console.log('转换后的评价列表:', myReviews.value);
    } else {
      console.error('获取评价列表失败:', res.message);
      myReviews.value = [];
    }
  } catch (error) {
    console.error('获取评价列表失败:', error);
    alert(error.message || '获取评价列表失败，请检查网络连接');
    myReviews.value = [];
  } finally {
    loadingReviews.value = false;
  }
};

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

// 监听 currentTab 变化，当切换到评价页面时加载数据
watch(currentTab, (newTab) => {
  if (newTab === 'reviews') {
    loadMyReviews();
  }
});

const logout = () => {
  if(confirm('确定要退出登录吗？')) {
    // 清除本地存储的 token 等信息
    localStorage.removeItem('hospital_token');
    router.push('/login');
  }
};

onMounted(() => {
  // 加载用户信息
  loadUserInfo();
  
  // 如果当前在评价页面，加载评价记录
  if (currentTab.value === 'reviews') {
    loadMyReviews();
  }
});
</script>
  
  <style scoped>
  /* 基础样式复用 */
  .profile-page { min-height: 100vh; background: #f4f6f9; font-family: 'Helvetica Neue', Arial, sans-serif; display: flex; flex-direction: column; }
  .main-header { height: 80px; background: white; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #ddd; }
  .header-inner { width: 100%; max-width: 1200px; padding: 0 40px; display: flex; justify-content: space-between; align-items: center; }
  .logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
  .logo-icon { font-size: 2.2rem; }
  .logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
  .logo-text small { font-size: 0.6rem; color: #666; }
  .back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
  .top-banner-section { background: white; }
  .banner-bg { height: 160px; background: linear-gradient(rgba(0,0,0,0.7), rgba(0,0,0,0.7)), url('https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?q=80&w=2000'); background-size: cover; background-position: center; display: flex; align-items: center; padding-left: 10%; }
  .banner-text h1 { color: white; font-size: 2.2rem; }
  .breadcrumb-strip { background: #f0ad4e; height: 50px; display: flex; align-items: center; padding-left: 10%; color: white; }
  .breadcrumb-strip .container { display: flex; align-items: center; gap: 10px; }
  .main-content { flex: 1; padding: 40px 0; }
  .content-container { max-width: 1200px; margin: 0 auto; padding: 0 40px; }
  .profile-layout { display: flex; gap: 30px; }
  .profile-sidebar { width: 280px; background: white; border-radius: 12px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); overflow: hidden; height: fit-content; }
  .avatar-box { background: #2c3e50; padding: 40px 20px; display: flex; flex-direction: column; align-items: center; color: white; }
  .avatar-circle { width: 100px; height: 100px; border-radius: 50%; border: 4px solid rgba(255,255,255,0.2); overflow: hidden; margin-bottom: 15px; background: white; }
  .avatar-circle img { width: 100%; height: 100%; object-fit: cover; }
  .user-name { margin: 0; font-size: 1.4rem; font-weight: bold; }
  .user-role { font-size: 0.8rem; opacity: 0.8; margin-top: 5px; background: rgba(255,255,255,0.2); padding: 2px 10px; border-radius: 10px; }
  .sidebar-menu { padding: 10px 0; }
  .menu-item { padding: 15px 30px; display: flex; align-items: center; gap: 10px; color: #666; cursor: pointer; transition: 0.2s; font-weight: 500; }
  .menu-item:hover { background: #f5f7fa; color: #004ea2; }
  .menu-item.active { border-left: 4px solid #004ea2; background: #eef3fb; color: #004ea2; }
  .profile-main { flex: 1; background: white; border-radius: 12px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); padding: 40px; }
  .panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; padding-bottom: 15px; border-bottom: 1px solid #eee; }
  .panel-header h2 { margin: 0; font-size: 1.5rem; color: #333; border-left: 5px solid #2c3e50; padding-left: 15px; }
  .btn-edit { background: #004ea2; color: white; border: none; padding: 8px 20px; border-radius: 6px; cursor: pointer; display: flex; align-items: center; gap: 5px; }
  .form-container { max-width: 600px; }
  .form-group { margin-bottom: 25px; }
  .form-group label { display: block; margin-bottom: 8px; color: #666; font-weight: bold; }
  .form-group input { width: 100%; padding: 12px 15px; border: 1px solid #ddd; border-radius: 6px; outline: none; font-size: 1rem; transition: 0.3s; background: #fff; }
  .input-disabled { background: #f5f5f5 !important; color: #999; cursor: not-allowed; }
  .input-edit { border-color: #004ea2; background: #fbfdff; }
  .form-hint { display: block; margin-top: 5px; color: #999; font-size: 0.85rem; }
  .form-actions { margin-top: 30px; display: flex; gap: 15px; }
  .btn-submit { background: #004ea2; color: white; border: none; padding: 12px 30px; border-radius: 6px; cursor: pointer; display: flex; align-items: center; gap: 5px; font-size: 1rem; transition: 0.3s; }
  .btn-submit:hover:not(:disabled) { background: #003d82; }
  .btn-submit:disabled { background: #ccc; cursor: not-allowed; }
  .btn-cancel { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 12px 30px; border-radius: 6px; cursor: pointer; font-size: 1rem; transition: 0.3s; }
  .btn-cancel:hover { background: #e8e8e8; }
  
  /* 我的评价列表样式 */
  .empty-reviews { text-align: center; color: #999; padding: 40px; }
  .empty-icon { font-size: 3rem; margin-bottom: 10px; }
  .my-review-card { border: 1px solid #eee; border-radius: 8px; padding: 20px; margin-bottom: 20px; background: #fafafa; }
  .rev-header { display: flex; justify-content: space-between; margin-bottom: 10px; font-weight: bold; color: #333; }
  .rev-date { font-weight: normal; color: #999; font-size: 0.85rem; }
  .star-yellow { color: #ffca28; }
  .star-gray { color: #ddd; }
  .rev-text { color: #666; line-height: 1.5; margin-top: 10px; }
  .app-footer { background: #1a3a6e; color: rgba(255,255,255,0.6); text-align: center; padding: 20px; margin-top: 50px; }
  </style>