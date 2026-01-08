<template>
    <div class="login-container">
      <div class="login-box">
        <div class="login-left">
          <div class="logo-area">
            <span class="logo-icon">🏥</span>
            <span class="logo-text">浙江工业大学健行医院</span>
          </div>
          <div class="illustration">
            <h3>智慧医疗 · 守护健康</h3>
            <p>Distributed Medical Information System</p>
            <div class="big-icon">👨‍⚕️</div>
          </div>
        </div>
  
        <div class="login-right">
          <h2>欢迎登录</h2>
          <p class="sub-title">请选择您的身份进行登录</p>
  
          <form @submit.prevent="handleLogin">
            <div class="form-group">
              <label>登录身份</label>
              <div class="role-selector">
                <div 
                  class="role-item" 
                  :class="{ active: form.role === 'user' }"
                  @click="form.role = 'user'"
                >
                  患者/用户
                </div>
                <div 
                  class="role-item" 
                  :class="{ active: form.role === 'doctor' }"
                  @click="form.role = 'doctor'"
                >
                  医生
                </div>
                <div 
                  class="role-item" 
                  :class="{ active: form.role === 'admin' }"
                  @click="form.role = 'admin'"
                >
                  管理员
                </div>
              </div>
            </div>
  
            <div class="form-group">
              <label>账号</label>
              <input type="text" v-model="form.username" placeholder="请输入账号/手机号" />
            </div>
  
            <div class="form-group">
              <label>密码</label>
              <input type="password" v-model="form.password" placeholder="请输入密码" />
            </div>
  
            <button type="submit" class="btn-login">登 录</button>
          </form>
          
          <div class="login-footer">
            <span>忘记密码?</span>
            <span @click="showRegisterModal = true" style="cursor: pointer;">注册新账号</span>
          </div>

          <!-- 注册弹窗 -->
          <div v-if="showRegisterModal" class="modal-overlay" @click.self="closeRegisterModal">
            <div class="modal-content">
              <div class="modal-header">
                <h3>注册新账号</h3>
                <span class="close-btn" @click="closeRegisterModal">&times;</span>
              </div>
              <form @submit.prevent="handleRegister">
                <div class="form-group">
                  <label>手机号</label>
                  <input 
                    type="text" 
                    v-model="registerForm.userPhone" 
                    placeholder="请输入11位手机号（以1开头）"
                    maxlength="11"
                  />
                </div>
                <div class="form-group">
                  <label>密码</label>
                  <input 
                    type="password" 
                    v-model="registerForm.userPassword" 
                    placeholder="请输入密码（至少6位）"
                  />
                </div>
                <div class="form-group">
                  <label>确认密码</label>
                  <input 
                    type="password" 
                    v-model="registerForm.confirmPassword" 
                    placeholder="请再次输入密码"
                  />
                </div>
                <button type="submit" class="btn-register" :disabled="registering">
                  {{ registering ? '注册中...' : '注册' }}
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { loginApi, registerApi } from '../api/auth';
  
  const router = useRouter();
  const form = ref({
    role: 'user', // 默认选中用户
    username: '',
    password: ''
  });

  // 注册相关
  const showRegisterModal = ref(false);
  const registering = ref(false);
  const registerForm = ref({
    userPhone: '',
    userPassword: '',
    confirmPassword: ''
  });

  const handleLogin = async () => {
    if (!form.value.username || !form.value.password) {
      alert("请输入账号和密码");
      return;
    }

    try {
      const res = await loginApi({
        userPhone: form.value.username,
        userPassword: form.value.password,
        role: form.value.role
      });

      // 登录成功，后端返回 { code: 200, data: { token: '...' } }
      const { token } = res.data;
      // 将Token保存到localStorage，以便后续请求使用
      localStorage.setItem('hospital_token', token);

      // 根据角色跳转到不同页面
      if (form.value.role === 'user') {
        router.push('/user');
      } else if (form.value.role === 'doctor') {
        router.push('/doctor');
      } else if (form.value.role === 'admin') {
        router.push('/admin');
      }
    } catch (error) {
      // 登录失败，错误信息已由 src/utils/request.js 中的响应拦截器统一处理（alert）
      console.error('登录失败:', error);
    }
  };

  const handleRegister = async () => {
    const { userPhone, userPassword, confirmPassword } = registerForm.value;

    // 验证表单
    if (!userPhone || !userPhone.trim()) {
      alert('请输入手机号');
      return;
    }

    // 验证手机号格式
    const phoneRegex = /^1\d{10}$/;
    if (!phoneRegex.test(userPhone)) {
      alert('请输入正确的手机号格式（11位数字，以1开头）');
      return;
    }

    if (!userPassword || !userPassword.trim()) {
      alert('请输入密码');
      return;
    }

    if (userPassword.length < 6) {
      alert('密码长度至少为6位');
      return;
    }

    if (!confirmPassword || !confirmPassword.trim()) {
      alert('请确认密码');
      return;
    }

    if (userPassword !== confirmPassword) {
      alert('两次输入的密码不一致');
      return;
    }

    registering.value = true;
    try {
      const res = await registerApi({
        userPhone: userPhone.trim(),
        userPassword: userPassword.trim()
      });

      if (res.code === 200) {
        alert('注册成功！请使用新账号登录。');
        closeRegisterModal();
        // 自动填充登录表单
        form.value.username = userPhone.trim();
        form.value.password = '';
      } else {
        alert(res.message || '注册失败，请重试');
      }
    } catch (error) {
      console.error('注册失败:', error);
      alert(error.message || '注册失败，请检查网络连接');
    } finally {
      registering.value = false;
    }
  };

  const closeRegisterModal = () => {
    showRegisterModal.value = false;
    registerForm.value = {
      userPhone: '',
      userPassword: '',
      confirmPassword: ''
    };
  };
  </script>
  
  <style scoped>
  .login-container {
    height: 100vh;
    width: 100vw;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .login-box {
    width: 900px;
    height: 550px;
    background: white;
    border-radius: 20px;
    box-shadow: 0 20px 50px rgba(0,0,0,0.1);
    display: flex;
    overflow: hidden;
  }
  
  /* 左侧样式 */
  .login-left {
    flex: 1;
    background: linear-gradient(135deg, #0056b3, #004ea2);
    padding: 40px;
    display: flex;
    flex-direction: column;
    color: white;
    position: relative;
  }
  .logo-area { display: flex; align-items: center; gap: 10px; font-size: 1.2rem; font-weight: bold; }
  .illustration { flex: 1; display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center; }
  .illustration h3 { font-size: 2rem; margin-bottom: 10px; }
  .illustration p { opacity: 0.8; }
  .big-icon { font-size: 8rem; margin-top: 30px; opacity: 0.9; }
  
  /* 右侧样式 */
  .login-right {
    flex: 1;
    padding: 50px;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
  .login-right h2 { color: #333; margin-bottom: 10px; font-size: 1.8rem; }
  .sub-title { color: #999; margin-bottom: 30px; font-size: 0.9rem; }
  
  .form-group { margin-bottom: 20px; }
  .form-group label { display: block; margin-bottom: 8px; color: #666; font-size: 0.9rem; }
  .form-group input {
    width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px; outline: none; transition: 0.3s;
  }
  .form-group input:focus { border-color: #0056b3; }
  
  /* 角色选择器 */
  .role-selector {
    display: flex; background: #f0f2f5; border-radius: 8px; padding: 4px;
  }
  .role-item {
    flex: 1; text-align: center; padding: 8px; font-size: 0.9rem; color: #666; cursor: pointer; border-radius: 6px; transition: 0.3s;
  }
  .role-item.active { background: white; color: #0056b3; font-weight: bold; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
  
  .btn-login {
    width: 100%; background: #0056b3; color: white; padding: 12px; border: none; border-radius: 8px; font-size: 1rem; cursor: pointer; margin-top: 10px; transition: 0.3s;
  }
  .btn-login:hover { background: #004494; }
  
  .login-footer { margin-top: 20px; display: flex; justify-content: space-between; font-size: 0.85rem; color: #0056b3; cursor: pointer; }
  
  /* 注册弹窗样式 */
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
  }
  .modal-content {
    background: white;
    border-radius: 12px;
    padding: 30px;
    width: 400px;
    max-width: 90vw;
    box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  }
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;
  }
  .modal-header h3 {
    margin: 0;
    color: #333;
    font-size: 1.5rem;
  }
  .close-btn {
    font-size: 2rem;
    color: #999;
    cursor: pointer;
    line-height: 1;
    transition: 0.3s;
  }
  .close-btn:hover {
    color: #333;
  }
  .btn-register {
    width: 100%;
    background: #0056b3;
    color: white;
    padding: 12px;
    border: none;
    border-radius: 8px;
    font-size: 1rem;
    cursor: pointer;
    margin-top: 10px;
    transition: 0.3s;
  }
  .btn-register:hover:not(:disabled) {
    background: #004494;
  }
  .btn-register:disabled {
    background: #ccc;
    cursor: not-allowed;
  }
  </style>