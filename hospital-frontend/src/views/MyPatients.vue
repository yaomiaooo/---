<template>
    <div class="patient-manage-page">
      
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
          <div class="banner-text"><h1>我的就诊人</h1></div>
        </div>
        <div class="breadcrumb-strip">
          <div class="container">
            <span @click="router.push('/user')" style="cursor: pointer">网站首页</span> 
            <Icon icon="mdi:chevron-right" />
            <span class="current">我的就诊人</span>
          </div>
          <div class="strip-shape"></div>
        </div>
      </div>
  
      <main class="main-content">
        <div class="content-container">
          
          <div class="page-header-row">
            <h2 class="section-title">就诊人管理</h2>
            <button class="btn-add-main" @click="openModal('add')">
              <Icon icon="mdi:plus" /> 新增就诊人
            </button>
          </div>
  
          <div class="patient-grid" v-if="patientList.length > 0 || loading">
            <div v-for="p in patientList" :key="p.patientId" class="patient-card">
              <div class="card-top">
                <div class="p-avatar">
                  {{ (p.name || '').charAt(0) || '?' }}
                </div>
                <div class="p-info-main">
                  <div class="p-name-row">
                    <span class="p-name">{{ p.name || '未知' }}</span>
                    <span class="p-tag" :class="p.relation === '本人' ? 'tag-blue' : 'tag-gray'">{{ p.relation || '其他' }}</span>
                  </div>
                  <div class="p-detail-row">
                    <Icon icon="mdi:gender-male-female" /> {{ p.gender || '未知' }}
                    <span class="sep">|</span>
                    {{ calculateAge(p.dob) }}岁
                  </div>
                </div>
              </div>
              
              <div class="card-mid">
                <div class="info-line">
                  <span class="label">身份证号：</span>
                  <span class="val">{{ maskIdCard(p.idCard) }}</span>
                </div>
                <div class="info-line">
                  <span class="label">手机号码：</span>
                  <span class="val">{{ maskPhone(p.phone) }}</span>
                </div>
              </div>
  
              <div class="card-bottom">
                <button class="btn-action edit" @click="openModal('edit', p)">
                  <Icon icon="mdi:pencil" /> 编辑
                </button>
                <button class="btn-action delete" @click="confirmDelete(p.patientId)">
                  <Icon icon="mdi:delete" /> 删除
                </button>
              </div>
            </div>
  
            <div class="patient-card add-card" @click="openModal('add')">
              <div class="add-content">
                <Icon icon="mdi:plus-circle-outline" class="add-icon" />
                <span>添加就诊人</span>
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="!loading && patientList.length === 0" class="empty-state">
            <Icon icon="mdi:account-off-outline" class="empty-icon" />
            <p>暂无就诊人信息</p>
            <button class="btn-add-empty" @click="openModal('add')">
              <Icon icon="mdi:plus" /> 添加就诊人
            </button>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-state">
            <p>加载中...</p>
          </div>
  
        </div>
      </main>
  
      <div class="modal-overlay" v-if="showModal">
        <div class="modal-box fade-in-up">
          <div class="modal-header">
            <h3>{{ modalType === 'add' ? '新增就诊人' : '编辑就诊人' }}</h3>
            <button class="btn-close" @click="closeModal"><Icon icon="mdi:close" /></button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>姓名 <span class="required">*</span></label>
              <input type="text" v-model="formData.name" placeholder="请输入真实姓名" />
            </div>
            <div class="form-group">
              <label>关系 <span class="required">*</span></label>
              <select v-model="formData.relation">
                <option value="本人">本人</option>
                <option value="父母">父母</option>
                <option value="子女">子女</option>
                <option value="配偶">配偶</option>
                <option value="其他">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label>身份证号 <span class="required">*</span></label>
              <input type="text" v-model="formData.idCard" placeholder="请输入18位身份证号" />
            </div>
            <div class="form-group">
              <label>手机号码 <span class="required">*</span></label>
              <input type="text" v-model="formData.phone" placeholder="请输入手机号码" />
            </div>
            <div class="form-row">
              <div class="form-group half">
                <label>性别</label>
                <select v-model="formData.gender">
                  <option value="男">男</option>
                  <option value="女">女</option>
                </select>
              </div>
              <div class="form-group half">
                <label>出生日期 <span class="required">*</span></label>
                <input type="date" v-model="formData.dob" placeholder="选择出生日期" />
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="closeModal">取消</button>
            <button class="btn-confirm" @click="savePatient">保存</button>
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
  
    </div>
  </template>
  
<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { getMyPatients, addPatient, updatePatient, deletePatient } from '../api/patient';

const router = useRouter();

// --- 就诊人数据 ---
const patientList = ref([]);
const loading = ref(false);
  
  // --- 页脚数据 (新增) ---
  const footerAddresses = [
    { name: '朝晖院区', addr: '杭州市上塘路158号' },
    { name: '屏峰院区', addr: '杭州市西湖区留和路288号' }
  ];
  const managedHospitals = [
    '浙江工业大学健行医院淳安分院', '浙江工业大学健行医院天台分院', '浙江工业大学健行医院浙东南院区',
    '浙江工业大学健行医院定海分院', '浙江工业大学健行医院海宁医院', '浙江工业大学健行医院南浔院区'
  ];
  const systemLinks = ['国家卫生健康委员会', '浙江省卫生健康委员会', '杭州医学院'];
  
  // --- 弹窗状态 ---
  const showModal = ref(false);
  const modalType = ref('add'); // 'add' or 'edit'
  const formData = ref({
    patientId: null, 
    name: '', 
    relation: '其他', 
    idCard: '', 
    phone: '', 
    gender: '男', 
    dob: ''
  });
  
  // --- 工具方法 ---
  const maskIdCard = (str) => str ? str.replace(/(\d{4})\d{10}(\d{4})/, '$1**********$2') : '';
  const maskPhone = (str) => str ? str.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '';
  
  // 根据出生日期计算年龄
  const calculateAge = (dob) => {
    if (!dob) return '未知';
    const birthDate = new Date(dob);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  };
  
  // 将日期字符串转换为 YYYY-MM-DD 格式（用于 input[type="date"]）
  const formatDateForInput = (dateStr) => {
    if (!dateStr) return '';
    // 如果是 LocalDate 格式 (YYYY-MM-DD)，直接返回
    if (typeof dateStr === 'string' && dateStr.includes('-')) {
      return dateStr.split('T')[0]; // 处理可能带时间的格式
    }
    return dateStr;
  };
  
  // 加载就诊人列表
  const loadPatients = async () => {
    loading.value = true;
    try {
      const res = await getMyPatients();
      if (res.code === 200 && res.data) {
        patientList.value = res.data;
      } else {
        console.error('获取就诊人列表失败:', res.message);
        patientList.value = [];
      }
    } catch (error) {
      console.error('获取就诊人列表失败:', error);
      patientList.value = [];
    } finally {
      loading.value = false;
    }
  };
  
  const openModal = (type, data = null) => {
    modalType.value = type;
    if (type === 'edit' && data) {
      formData.value = {
        patientId: data.patientId,
        name: data.name || '',
        relation: data.relation || '其他',
        idCard: data.idCard || '',
        phone: data.phone || '',
        gender: data.gender || '男',
        dob: formatDateForInput(data.dob)
      };
    } else {
      formData.value = {
        patientId: null,
        name: '',
        relation: '其他',
        idCard: '',
        phone: '',
        gender: '男',
        dob: ''
      };
    }
    showModal.value = true;
  };
  
  const closeModal = () => { 
    showModal.value = false;
    formData.value = {
      patientId: null,
      name: '',
      relation: '其他',
      idCard: '',
      phone: '',
      gender: '男',
      dob: ''
    };
  };
  
  const savePatient = async () => {
    // 验证必填项
    if (!formData.value.name || !formData.value.idCard || !formData.value.phone || !formData.value.dob) {
      alert('请填写所有必填项！');
      return;
    }
    
    // 验证手机号格式
    const phoneRegex = /^1\d{10}$/;
    if (!phoneRegex.test(formData.value.phone)) {
      alert('请输入正确的手机号码格式（11位数字，以1开头）');
      return;
    }
    
    // 验证身份证号格式（18位）
    if (formData.value.idCard.length !== 18) {
      alert('请输入18位身份证号');
      return;
    }
    
    loading.value = true;
    try {
      const requestData = {
        name: formData.value.name,
        idCard: formData.value.idCard,
        phone: formData.value.phone,
        dob: formData.value.dob,
        gender: formData.value.gender,
        relation: formData.value.relation
      };
      
      if (modalType.value === 'add') {
        const res = await addPatient(requestData);
        if (res.code === 200) {
          alert('添加成功！');
          closeModal();
          await loadPatients(); // 重新加载列表
        } else {
          alert(res.message || '添加失败，请重试');
        }
      } else {
        const res = await updatePatient(formData.value.patientId, requestData);
        if (res.code === 200) {
          alert('修改成功！');
          closeModal();
          await loadPatients(); // 重新加载列表
        } else {
          alert(res.message || '修改失败，请重试');
        }
      }
    } catch (error) {
      console.error('保存就诊人失败:', error);
      const errorMessage = error.message || error.response?.data?.message || '保存失败，请检查网络连接';
      alert(errorMessage);
    } finally {
      loading.value = false;
    }
  };
  
  const confirmDelete = async (patientId) => {
    if (!confirm('确定要删除这位就诊人吗？删除后不可恢复。')) {
      return;
    }
    
    loading.value = true;
    try {
      const res = await deletePatient(patientId);
      if (res.code === 200) {
        alert('删除成功！');
        await loadPatients(); // 重新加载列表
      } else {
        alert(res.message || '删除失败，请重试');
      }
    } catch (error) {
      console.error('删除就诊人失败:', error);
      const errorMessage = error.message || error.response?.data?.message || '删除失败，请检查网络连接';
      alert(errorMessage);
    } finally {
      loading.value = false;
    }
  };
  
  // 组件挂载时加载数据
  onMounted(() => {
    loadPatients();
  });
  </script>
  
  <style scoped>
  /* 基础样式复用 */
  .patient-manage-page { min-height: 100vh; background: #f4f6f9; font-family: 'Helvetica Neue', Arial, sans-serif; display: flex; flex-direction: column; }
  .main-header { height: 80px; background: white; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #ddd; }
  .header-inner { width: 100%; max-width: 1200px; padding: 0 40px; display: flex; justify-content: space-between; align-items: center; }
  .logo-group { display: flex; align-items: center; gap: 10px; cursor: pointer; }
  .logo-icon { font-size: 2.2rem; }
  .logo-text h1 { margin: 0; font-size: 1.4rem; color: #004ea2; }
  .logo-text small { font-size: 0.6rem; color: #666; }
  .back-home { cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
  
  /* Banner */
  .top-banner-section { background: white; }
  .banner-bg { height: 160px; background: linear-gradient(rgba(0,0,0,0.6), rgba(0,0,0,0.6)), url('https://images.unsplash.com/photo-1532938911079-1b06ac7ceec7?q=80&w=2000'); background-size: cover; background-position: center; display: flex; align-items: center; padding-left: 10%; }
  .banner-text h1 { color: white; font-size: 2.2rem; }
  .breadcrumb-strip { background: #f0ad4e; height: 50px; display: flex; align-items: center; position: relative; padding-left: 10%; color: white; }
  .breadcrumb-strip .container { display: flex; align-items: center; gap: 10px; z-index: 2; }
  .strip-shape { position: absolute; right: 0; top: 0; border-top: 50px solid #f0ad4e; border-left: 50px solid transparent; }
  
  /* 主体内容 */
  .main-content { flex: 1; padding: 40px 0; }
  .content-container { max-width: 1200px; margin: 0 auto; padding: 0 40px; }
  
  .page-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
  .section-title { font-size: 1.8rem; color: #333; margin: 0; border-left: 5px solid #004ea2; padding-left: 15px; font-weight: bold; }
  .btn-add-main { background: #004ea2; color: white; border: none; padding: 10px 25px; border-radius: 30px; cursor: pointer; display: flex; align-items: center; gap: 5px; font-size: 1rem; transition: 0.2s; box-shadow: 0 4px 10px rgba(0,78,162,0.3); }
  .btn-add-main:hover { background: #003d80; transform: translateY(-2px); }
  
  /* 网格列表 */
  .patient-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 25px; }
  
  /* 卡片样式 */
  .patient-card { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 5px 15px rgba(0,0,0,0.05); transition: 0.3s; border: 1px solid #eee; display: flex; flex-direction: column; }
  .patient-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0,0,0,0.1); border-color: #004ea2; }
  
  /* 卡片上部：头像区 */
  .card-top { padding: 20px; display: flex; align-items: center; gap: 15px; border-bottom: 1px dashed #eee; background: #fcfcfc; }
  .p-avatar { width: 50px; height: 50px; background: #e3f2fd; color: #004ea2; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.4rem; font-weight: bold; border: 2px solid #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
  .p-info-main { flex: 1; }
  .p-name-row { display: flex; align-items: center; gap: 8px; margin-bottom: 5px; }
  .p-name { font-size: 1.2rem; font-weight: bold; color: #333; }
  .p-tag { font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; }
  .tag-blue { background: #e3f2fd; color: #004ea2; }
  .tag-gray { background: #f0f0f0; color: #666; }
  .p-detail-row { font-size: 0.85rem; color: #888; display: flex; align-items: center; gap: 5px; }
  .sep { color: #ddd; }
  
  /* 卡片中部：信息区 */
  .card-mid { padding: 20px; flex: 1; display: flex; flex-direction: column; gap: 10px; }
  .info-line { display: flex; justify-content: space-between; font-size: 0.9rem; }
  .info-line .label { color: #888; }
  .info-line .val { color: #333; font-weight: 500; font-family: monospace; }
  
  /* 卡片底部：操作区 */
  .card-bottom { display: flex; border-top: 1px solid #eee; }
  .btn-action { flex: 1; border: none; background: white; padding: 12px 0; cursor: pointer; font-size: 0.9rem; display: flex; align-items: center; justify-content: center; gap: 5px; transition: 0.2s; }
  .btn-action:hover { background: #f9f9f9; }
  .btn-action.edit { color: #004ea2; border-right: 1px solid #eee; }
  .btn-action.delete { color: #ff4d4f; }
  
  /* 添加卡片样式 */
  .add-card { border: 2px dashed #ddd; background: #fafafa; justify-content: center; align-items: center; cursor: pointer; min-height: 220px; }
  .add-card:hover { border-color: #004ea2; background: #f0f7ff; color: #004ea2; }
  .add-content { display: flex; flex-direction: column; align-items: center; gap: 10px; color: #999; }
  .add-icon { font-size: 3rem; }
  
  /* 弹窗样式 */
  .modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 2000; display: flex; align-items: center; justify-content: center; }
  .modal-box { background: white; width: 500px; border-radius: 12px; overflow: hidden; box-shadow: 0 20px 50px rgba(0,0,0,0.3); }
  .fade-in-up { animation: fadeInUp 0.3s ease-out; }
  @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
  
  .modal-header { padding: 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; background: #fcfcfc; }
  .modal-header h3 { margin: 0; color: #333; }
  .btn-close { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: #999; }
  .modal-body { padding: 30px; display: flex; flex-direction: column; gap: 20px; }
  .form-group { display: flex; flex-direction: column; gap: 8px; }
  .form-group label { font-size: 0.9rem; color: #666; font-weight: bold; }
  .required { color: red; }
  .form-group input, .form-group select { padding: 10px; border: 1px solid #ddd; border-radius: 6px; outline: none; }
  .form-group input:focus { border-color: #004ea2; }
  .form-row { display: flex; gap: 20px; }
  .form-group.half { flex: 1; }
  
  .modal-footer { padding: 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 15px; background: #fcfcfc; }
  .btn-cancel { background: white; border: 1px solid #ddd; padding: 8px 25px; border-radius: 6px; cursor: pointer; }
  .btn-confirm { background: #004ea2; color: white; border: none; padding: 8px 25px; border-radius: 6px; cursor: pointer; }
  
  /* ★★★ 标准 Footer 样式 (新增) ★★★ */
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
  .addr-item h4 { margin: 0 0 5px 0; font-size: 1rem; color: #fff; }
  .addr-item p { margin: 0; font-size: 0.85rem; color: rgba(255,255,255,0.7); }
  .footer-title { font-size: 1.1rem; font-weight: bold; margin-bottom: 20px; border-left: 3px solid #00b0f0; padding-left: 10px; }
  .footer-link-list { list-style: none; padding: 0; }
  .footer-link-list li { margin-bottom: 10px; color: rgba(255,255,255,0.8); cursor: pointer; display: flex; align-items: center; gap: 8px; }
  .footer-link-list li:hover { color: white; transform: translateX(5px); }
  .social-icons { display: flex; gap: 15px; margin: 30px 0; }
  .icon-box { width: 40px; height: 40px; border: 1px solid rgba(255,255,255,0.3); border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; cursor: pointer; }
  .icon-box:hover { background: white; color: #1a3a6e; }
  
  /* 空状态和加载状态 */
  .empty-state {
    text-align: center;
    padding: 80px 20px;
    color: #999;
  }
  .empty-icon {
    font-size: 4rem;
    margin-bottom: 20px;
    opacity: 0.5;
  }
  .empty-state p {
    font-size: 1.2rem;
    margin-bottom: 30px;
  }
  .btn-add-empty {
    background: #004ea2;
    color: white;
    border: none;
    padding: 12px 30px;
    border-radius: 30px;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 1rem;
  }
  .btn-add-empty:hover {
    background: #003d80;
  }
  .loading-state {
    text-align: center;
    padding: 60px 20px;
    color: #999;
    font-size: 1.1rem;
  }
  
  @media(max-width: 900px) {
    .patient-grid { grid-template-columns: 1fr; }
    .modal-box { width: 90%; }
    .footer-content { flex-direction: column; }
  }
  </style>