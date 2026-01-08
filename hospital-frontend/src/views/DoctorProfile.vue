<template>
  <div class="page">
    <!-- 顶部操作条 -->
    <div class="card head-card">
      <div class="left">
        <h3>个人信息管理 <small>Profile</small></h3>
      </div>

      <div class="right">
        <span v-if="error" class="error">{{ error }}</span>
        <button v-if="!editing" class="btn" @click="showChangePassword">修改密码</button>
        <button v-if="!editing" class="btn primary" @click="startEdit">编辑</button>
        <button v-else class="btn primary" @click="save" :disabled="loading">{{ loading ? '保存中...' : '保存' }}</button>
        <button v-if="editing" class="btn" @click="cancelEdit" :disabled="loading">取消</button>
      </div>
    </div>

    <!-- 快捷信息卡 -->
    <div class="stats">
      <div class="stat">
        <div class="k">所属院区</div>
        <div class="v">{{ form.hospital_name || '-' }}</div>
      </div>
      <div class="stat">
        <div class="k">所属科室</div>
        <div class="v">{{ form.department_name || '-' }}</div>
      </div>
      <div class="stat">
        <div class="k">职称</div>
        <div class="v">{{ form.title || '-' }}</div>
      </div>

      <div class="stat">
        <div class="k">展示状态</div>
        <div class="v">{{ editing ? '编辑中' : '只读' }}</div>
      </div>
    </div>

    <!-- 信息卡 -->
    <div class="card form-card">
      <div class="form-grid">
        <div class="field">
          <label>医生编号</label>
          <input type="text" v-model="form.doctor_id" disabled />
        </div>

        <div class="field">
          <label>姓名</label>
          <input type="text" v-model.trim="form.name" disabled />
        </div>

        <div class="field">
          <label>性别</label>
          <input type="text" v-model="form.gender" disabled />
        </div>

        <div class="field">
          <label>电话</label>
          <input type="text" v-model.trim="form.phone" :disabled="!editing" placeholder="例如：13800001234" />
          <div v-if="editing && phoneError" class="warn">{{ phoneError }}</div>
        </div>

        <div class="field">
          <label>邮箱</label>
          <input type="text" v-model.trim="form.email" :disabled="!editing" placeholder="例如：xx@hospital.com" />
          <div v-if="editing && emailError" class="warn">{{ emailError }}</div>
        </div>

        <div class="field full">
          <label>个人简介</label>
          <textarea v-model.trim="form.bio" :disabled="!editing" rows="4" placeholder="用于患者端展示的医生简介"></textarea>
        </div>
      </div>

      <div class="foot">
        <div class="meta">
          <div>最后更新时间：<span class="mono">{{ form.updated_at || '-' }}</span></div>
        </div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <div v-if="showPasswordModal" class="modal-mask" @click.self="showPasswordModal=false">
      <div class="modal">
        <div class="modal-head">
          <h3>修改密码</h3>
          <button class="close-btn" @click="showPasswordModal=false">×</button>
        </div>
        <div class="modal-body">
          <div class="field">
            <label>当前密码</label>
            <input
              type="password"
              v-model="passwordForm.oldPassword"
              placeholder="请输入当前密码"
            />
          </div>
          <div class="field">
            <label>新密码</label>
            <input
              type="password"
              v-model="passwordForm.newPassword"
              placeholder="请输入新密码（至少6位）"
            />
          </div>
          <div class="field">
            <label>确认新密码</label>
            <input
              type="password"
              v-model="passwordForm.confirmPassword"
              placeholder="请再次输入新密码"
            />
          </div>
          <div v-if="passwordError" class="error">{{ passwordError }}</div>
        </div>
        <div class="modal-foot">
          <button class="btn" @click="showPasswordModal=false">取消</button>
          <button class="btn primary" @click="changePassword" :disabled="changingPassword">
            {{ changingPassword ? '修改中...' : '确认修改' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

/* ======================
 * 状态
 * ====================== */
const loading = ref(false)
const editing = ref(false)
const error = ref('')
const hospitals = ref([])
const departments = ref([])

// 修改密码相关状态
const showPasswordModal = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordError = ref('')
const changingPassword = ref(false)

/* ======================
 * 页面表单（UI 使用）
 * ====================== */
const form = reactive({
  doctor_id: '',
  name: '',
  gender: '',
  phone: '',
  email: '',
  title: '',
  bio: '',
  hospital_id: '',
  hospital_name: '',
  department_id: '',
  department_name: '',
  updated_at: ''
})

let snapshot = null

/* ======================
 * 工具函数
 * ====================== */
function nowText() {
  const d = new Date()
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

function apiUrl() {
  const base = import.meta.env.VITE_API_BASE || 'http://localhost:8080'
  console.log('API Base URL:', base)
  return base + '/api/doctor/profile'
}

function hospitalsApiUrl() {
  const base = import.meta.env.VITE_API_BASE || 'http://localhost:8080'
  console.log('Hospitals API URL:', base + '/api/hospitals')
  return base + '/api/hospitals'
}

function departmentsApiUrl(hospitalId = '') {
  const base = (import.meta.env.VITE_API_BASE || 'http://localhost:8080') + '/api/hospital/departments'
  const url = hospitalId ? `${base}?hospitalId=${hospitalId}` : base
  console.log('Departments API URL:', url)
  return url
}

/* ======================
 * 后端 → 前端 映射
 * ====================== */
function applyFromApi(data) {
  form.doctor_id = data.doctorId || ''
  form.name = data.doctorName || ''
  form.gender = data.doctorGender || ''
  form.phone = data.doctorPhone || ''
  form.email = data.doctorEmail || ''
  form.title = data.title || ''
  form.bio = data.doctorIntro || ''
  form.hospital_id = data.hospitalId || ''
  form.hospital_name = data.hospitalName || ''
  form.department_id = data.departmentId || ''
  form.department_name = data.departmentName || ''
  form.updated_at = nowText()

  // 更新localStorage中的医生信息，用于Dashboard显示
  if (data.doctorName) {
    localStorage.setItem('doctorName', data.doctorName)
  }
  if (data.title) {
    localStorage.setItem('doctorTitle', data.title)
  }
  if (data.departmentName) {
    localStorage.setItem('doctorDept', data.departmentName)
  }
}

/* ======================
 * 加载医院列表
 * ====================== */
async function loadHospitals() {
  try {
    const resp = await fetch(hospitalsApiUrl())
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
    const json = await resp.json()
    if (json.code !== 200) throw new Error(json.message)
    hospitals.value = json.data || []
  } catch (e) {
    console.error('加载医院列表失败:', e)
  }
}

/* ======================
 * 加载科室列表
 * ====================== */
async function loadDepartments(hospitalId = '') {
  try {
    const resp = await fetch(departmentsApiUrl(hospitalId))
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
    const json = await resp.json()
    if (json.code !== 200) throw new Error(json.message)
    departments.value = json.data || []
  } catch (e) {
    console.error('加载科室列表失败:', e)
  }
}

/* ======================
 * 加载医生信息
 * ====================== */
async function load() {
  loading.value = true
  error.value = ''

  try {
    // 并行加载医院列表和医生信息
    const token = localStorage.getItem('hospital_token')
    const [doctorResp, hospitalsResp] = await Promise.all([
      fetch(apiUrl(), {
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {})
        }
      }),
      fetch(hospitalsApiUrl())
    ])

    if (!doctorResp.ok) throw new Error(`医生信息 HTTP ${doctorResp.status}`)

    const doctorJson = await doctorResp.json()
    if (doctorJson.code !== 200) throw new Error(doctorJson.message)

    // 加载医院列表
    if (hospitalsResp.ok) {
      const hospitalsJson = await hospitalsResp.json()
      if (hospitalsJson.code === 200) {
        hospitals.value = hospitalsJson.data || []
      }
    }

    // 应用医生数据
    applyFromApi(doctorJson.data)

    // 如果医生有医院ID，加载对应科室
    if (doctorJson.data.hospitalId) {
      await loadDepartments(doctorJson.data.hospitalId)
    } else {
      await loadDepartments()
    }
  } catch (e) {
    console.error(e)
    error.value = '医生信息加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

/* ======================
 * 修改密码
 * ====================== */
function showChangePassword() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordError.value = ''
  showPasswordModal.value = true
}

async function changePassword() {
  // 验证输入
  if (!passwordForm.oldPassword.trim()) {
    passwordError.value = '请输入当前密码'
    return
  }
  if (!passwordForm.newPassword.trim()) {
    passwordError.value = '请输入新密码'
    return
  }
  if (passwordForm.newPassword.length < 6) {
    passwordError.value = '新密码至少6位'
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }

  changingPassword.value = true
  passwordError.value = ''

  try {
    const token = localStorage.getItem('hospital_token')
    const response = await fetch('http://localhost:8080/api/doctor/change-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
    })

    const result = await response.json()

    if (result.code === 200) {
      alert('密码修改成功！')
      showPasswordModal.value = false
    } else {
      passwordError.value = result.message || '密码修改失败'
    }
  } catch (error) {
    passwordError.value = '网络错误，请重试'
    console.error('修改密码失败:', error)
  } finally {
    changingPassword.value = false
  }
}

/* ======================
 * 编辑 / 取消
 * ====================== */
function startEdit() {
  snapshot = JSON.parse(JSON.stringify(form))
  editing.value = true
}

function cancelEdit() {
  if (snapshot) Object.assign(form, snapshot)
  editing.value = false
}


/* ======================
 * 医院选择变化处理
 * ====================== */
async function onHospitalChange() {
  // 清空科室选择
  form.department_id = ''
  form.department_name = ''

  // 重新加载科室列表
  if (form.hospital_id) {
    await loadDepartments(form.hospital_id)

    // 更新医院名称显示
    const selectedHospital = hospitals.value.find(h => h.hospitalId === form.hospital_id)
    form.hospital_name = selectedHospital ? selectedHospital.hospitalName : ''
  } else {
    departments.value = []
    form.hospital_name = ''
  }
}

/* ======================
 * 校验
 * ====================== */
const phoneError = computed(() => {
  if (!editing.value) return ''
  if (!form.phone) return '电话不能为空'
  return /^\d{11}$/.test(form.phone) ? '' : '电话格式不正确'
})

const emailError = computed(() => {
  if (!editing.value) return ''
  if (!form.email) return ''
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email) ? '' : '邮箱格式不正确'
})

function validate() {
  if (!form.name) return '姓名不能为空'
  if (phoneError.value) return phoneError.value
  if (emailError.value) return emailError.value
  return ''
}

/* ======================
 * 保存（只提交允许修改的字段）
 * ====================== */
async function save() {
  const msg = validate()
  if (msg) {
    alert(msg)
    return
  }

  loading.value = true
  error.value = ''

  try {
    const token = localStorage.getItem('hospital_token')

    const payload = {}
    if (form.phone && form.phone.trim()) {
      payload.doctorPhone = form.phone.trim()
    }
    if (form.email && form.email.trim()) {
      payload.doctorEmail = form.email.trim()
    }
    if (form.bio && form.bio.trim()) {
      payload.doctorIntro = form.bio.trim()
    }

    const resp = await fetch(apiUrl(), {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify(payload)
    })

    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)

    const json = await resp.json()
    if (json.code !== 200) throw new Error(json.message)

    applyFromApi(json.data)
    editing.value = false
    alert('保存成功')
  } catch (e) {
    console.error(e)
    error.value = '保存失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

/* ======================
 * 生命周期
 * ====================== */
onMounted(load)
</script>


<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  padding: 16px 18px;
}

.head-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
}
.head-card h3 { margin: 0; font-size: 1.5rem; color: #333;  }
.head-card small { color: #999; font-weight: normal; margin-left: 8px; }

.right { display: flex; align-items: center; gap: 10px; }
.error { color: #ff4d4f; font-size: 0.85rem; max-width: 360px; }

.btn {
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
}
.btn.primary {
  border: none;
  background: #004ea2;
  color: #fff;
  font-weight: 800;
}
.btn.primary:hover { background: #003d80; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

.field label {
  display: block;
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 6px;
}

.field input,
.field select,
.field textarea {
  width: 96%;
  padding: 10px 10px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  outline: none;
  background: #fff;
}

.field textarea { resize: vertical; }

.field input:focus,
.field select:focus,
.field textarea:focus {
  border-color: #004ea2;
  box-shadow: 0 0 0 3px rgba(0, 78, 162, 0.10);
}

.field input:disabled,
.field select:disabled,
.field textarea:disabled {
  background: #fafafa;
  color: #666;
}

.field.full { grid-column: 1 / -1; }

.warn {
  margin-top: 6px;
  font-size: 0.82rem;
  color: #fa8c16;
}

.foot {
  margin-top: 14px;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
  color: #999;
  font-size: 0.85rem;
}
.meta { display: flex; justify-content: space-between; gap: 10px; flex-wrap: wrap; }
.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  color: #004ea2;
  font-weight: 800;
}

.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  padding: 14px 16px;
}
.stat .k { font-size: 0.85rem; color: #999; }
.stat .v { margin-top: 6px; font-size: 1.2rem; font-weight: 900; color: #004ea2; }

/* 修改密码弹窗 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 12px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-head h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.close-btn:hover {
  background: #f5f5f5;
}

.modal-body {
  padding: 20px;
}

.modal-foot {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}
</style>

