<template>
  <div class="doctors-page">
    <!-- 顶部筛选工具条（风格对齐：预约订单管理） -->
    <div class="toolbar-card">
      <div class="toolbar-left">
        <div class="field">
          <label>院区</label>
          <select
              v-model="filters.hospitalId"
              class="select"
              @change="onHospitalChange"
          >
            <option value="">所有院区</option>
            <option value="1">朝晖院区</option>
            <option value="2">屏峰院区</option>
          </select>
        </div>

        <div class="field">
          <label>关键词</label>
          <input
              v-model="filters.keyword"
              class="input"
              placeholder="按医生ID/姓名/手机号检索"
              @keyup.enter="loadDoctors(1)"
          />
        </div>

        <div class="field">
          <label>科室</label>
          <select
              v-model="filters.departmentId"
              class="select"
              :disabled="filterDeptLoading"
              @change="loadDoctors(1)"
          >
            <option value="">全部科室</option>
            <option v-for="dep in filterDepartments" :key="dep.departmentId" :value="dep.departmentId">
              {{ dep.departmentName }}（{{ dep.departmentId }}）
            </option>
          </select>
        </div>

        <div class="actions">
          <button class="btn btn-primary btn-mini" @click="loadDoctors(1)">
            <Icon icon="mdi:magnify" /> 查询
          </button>
          <button class="btn btn-ghost btn-mini" @click="resetFilters">
            <Icon icon="mdi:refresh" /> 重置
          </button>
        </div>
      </div>

      <div class="toolbar-right">
        <button class="btn btn-primary" @click="openCreate">
          <Icon icon="mdi:account-plus-outline" />
          新增医生
        </button>
      </div>
    </div>

    <div v-if="error" class="error">
      <Icon icon="mdi:alert-circle-outline" />
      <span>{{ error }}</span>
    </div>

    <!-- 表格区 -->
    <div class="table-card">
      <div class="section-header">
        <div>
          <div class="section-title">医生列表</div>
          <div class="section-sub">支持按院区/科室与关键词筛选；分页加载。</div>
        </div>

        <div class="table-right">
          <!-- <div class="range mono">
            共 <span class="v mono">{{ pager.total }}</span> 条
          </div> -->

          <div class="field small">
            <label>每页</label>
            <select class="select small" v-model.number="pager.size" @change="loadDoctors(1)">
              <option :value="10">10/页</option>
              <option :value="20">20/页</option>
              <option :value="50">50/页</option>
            </select>
          </div>
        </div>
      </div>

      <div class="table-wrap">
        <table class="admin-table">
          <thead>
          <tr>
            <th style="width: 110px;">医生ID</th>
            <th style="width: 90px;">院区ID</th>
            <th style="width: 110px;">科室ID</th>
            <th style="width: 120px;">姓名</th>
            <th style="width: 80px;">性别</th>
            <th style="width: 110px;">职称</th>
            <th style="width: 140px;">手机号</th>
            <th>邮箱</th>
            <th style="width: 150px;">操作</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="loading">
            <td colspan="9" class="empty">加载中...</td>
          </tr>

          <tr v-else-if="items.length === 0">
            <td colspan="9" class="empty">暂无数据</td>
          </tr>

          <tr v-else v-for="row in items" :key="row.doctorId">
            <td class="mono">{{ row.doctorId }}</td>
            <td class="mono">{{ row.hospitalId }}</td>
            <td class="mono">{{ row.departmentId }}</td>
            <td>{{ row.doctorName }}</td>
            <td>{{ row.doctorGender }}</td>
            <td>{{ row.title }}</td>
            <td class="mono">{{ row.doctorPhone }}</td>
            <td class="mono">{{ row.doctorEmail }}</td>
            <td>
              <div class="actions">
                <button class="btn btn-ghost btn-mini" @click="openEdit(row)">
                  <Icon icon="mdi:pencil-outline" /> 编辑
                </button>
                <button class="btn btn-danger btn-mini" @click="confirmDelete(row)">
                  <Icon icon="mdi:delete-outline" /> 删除
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="pager">
        <div class="pager-left">
          <span class="pager-info mono">
            第 {{ pager.page }} / {{ totalPages }} 页
          </span>
        </div>

        <div class="pager-right">
          <button class="btn btn-ghost btn-mini" :disabled="pager.page <= 1" @click="loadDoctors(pager.page - 1)">
            上一页
          </button>
          <button class="btn btn-ghost btn-mini" :disabled="pager.page >= totalPages" @click="loadDoctors(pager.page + 1)">
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 弹窗：新增/编辑 -->
    <div v-if="modal.visible" class="modal-mask" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">
            {{ modal.mode === 'create' ? '新增医生' : '修改医生信息' }}
          </div>
          <button class="btn btn-ghost btn-mini" @click="closeModal">
            <Icon icon="mdi:close" /> 关闭
          </button>
        </div>

        <div class="modal-body">
          <div v-if="modalError" class="error">
            <Icon icon="mdi:alert-circle-outline" />
            <span>{{ modalError }}</span>
          </div>

          <div class="form-grid">
            <div class="field">
              <label>医生ID（doctorId）</label>
              <input v-model="form.doctor_id" class="input" disabled placeholder="系统生成/不可修改" />
            </div>

            <div class="field">
              <label>用户ID（userId）</label>
              <input v-model="form.user_id" class="input" disabled placeholder="系统生成/不可修改" />
            </div>

            <div class="field">
              <label>用户手机号（userPhone）</label>
              <input v-model="form.user_phone" class="input" placeholder="如：138xxxxxx" />
            </div>

            <div v-if="modal.mode === 'create'" class="field">
              <label>初始密码（userPassword）</label>
              <input v-model="form.user_password" class="input" placeholder="创建用户时使用" />
            </div>

            <div class="field">
              <label>院区ID（hospitalId）</label>
              <input v-model="form.hospital_id" class="input" :disabled="modal.mode !== 'create'" placeholder="如：1 / 2" />
            </div>

            <div class="field">
              <label>科室（departmentId）</label>
              <!-- 编辑模式：显示科室名称（只读） -->
              <input
                  v-if="modal.mode === 'edit'"
                  :value="currentDepartmentName || form.department_id || '未知科室'"
                  class="input"
                  disabled
                  readonly
              />
              <!-- 新增模式：下拉选择 -->
              <select
                  v-else
                  v-model="form.department_id"
                  class="select"
                  :disabled="deptLoading || !form.hospital_id"
              >
                <option value="">请选择</option>
                <option v-for="dep in departments" :key="dep.departmentId" :value="dep.departmentId">
                  {{ dep.departmentName }}（{{ dep.departmentId }}）
                </option>
              </select>
            </div>

            <div class="field">
              <label>姓名（doctorName）</label>
              <input v-model="form.doctor_name" class="input" placeholder="医生姓名" />
            </div>

            <div class="field">
              <label>性别（doctorGender）</label>
              <select v-model="form.doctor_gender" class="select">
                <option value="">请选择</option>
                <option value="男">男</option>
                <option value="女">女</option>
              </select>
            </div>

            <div class="field">
              <label>身份证（doctorIdcard）</label>
              <input v-model="form.doctor_idcard" class="input" placeholder="可选" />
            </div>

            <div class="field">
              <label>职称（title）</label>
              <input v-model="form.title" class="input" placeholder="医师/主治/副高/正高..." />
            </div>

            <div class="field">
              <label>电话（doctorPhone）</label>
              <input v-model="form.doctor_phone" class="input" placeholder="可选" />
            </div>

            <div class="field">
              <label>邮箱（doctorEmail）</label>
              <input v-model="form.doctor_email" class="input" placeholder="可选" />
            </div>

            <div class="field full">
              <label>简介（doctorIntro）</label>
              <textarea v-model="form.doctor_intro" class="textarea" rows="4" placeholder="可选"></textarea>
            </div>

            <div class="field full">
              <label>头像URL（avatarUrl）</label>
              <input v-model="form.avatar_url" class="input" placeholder="可选" />
            </div>
          </div>

          <div class="modal-actions">
            <button class="btn btn-ghost" @click="closeModal">取消</button>
            <button class="btn btn-primary" :disabled="modalSaving" @click="submitModal">
              <Icon v-if="modalSaving" icon="mdi:loading" class="spin" />
              保存
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 二次确认：删除 -->
    <div v-if="del.visible" class="modal-mask" @click.self="closeDelete">
      <div class="modal modal-sm">
        <div class="modal-header">
          <div class="modal-title">确认删除</div>
          <button class="btn btn-ghost btn-mini" @click="closeDelete">
            <Icon icon="mdi:close" /> 关闭
          </button>
        </div>
        <div class="modal-body">
          <div class="kv">
            <div class="k">医生ID</div>
            <div class="v mono">{{ del.row?.doctorId }}</div>
            <div class="k">姓名</div>
            <div class="v">{{ del.row?.doctorName }}</div>
          </div>

          <div class="modal-actions">
            <button class="btn btn-ghost" @click="closeDelete">取消</button>
            <button class="btn btn-danger" :disabled="del.saving" @click="doDelete">
              <Icon v-if="del.saving" icon="mdi:loading" class="spin" />
              删除
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import {
  fetchDoctors,
  createDoctor,
  updateDoctor,
  deleteDoctor,
  fetchDepartmentsByHospital
} from '../api/adminDoctor'
import { pageDepartments } from '../api/adminDepartment'

const filters = reactive({
  keyword: '',
  hospitalId: '',
  departmentId: ''
})

const items = ref([])
const loading = ref(false)
const error = ref('')

const pager = reactive({
  page: 1,
  size: 10,
  total: 0
})

const totalPages = computed(() => {
  const t = Math.max(1, Math.ceil((pager.total || 0) / (pager.size || 10)))
  return t
})

function formatTime(x) {
  if (!x) return '-'
  const d = new Date(x)
  if (Number.isNaN(d.getTime())) return String(x)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function resetFilters() {
  filters.keyword = ''
  filters.hospitalId = ''
  filters.departmentId = ''
  // 重置时加载所有科室
  await loadFilterDepartmentsByHospital('')
  loadDoctors(1)
}

/**
 * 兼容解析分页：
 * - request.js -> Result：res.data = page
 * - 未来若某接口直接返回 page，也兼容
 */
function unwrapPage(res) {
  const body = res ?? {}
  const pageObj = body.data ?? body
  return pageObj || {}
}

/** ======== 科室下拉（新增弹窗用） ======== */
const departments = ref([]) // { departmentId, departmentName }[]
const deptLoading = ref(false)
const currentDepartmentName = ref('') // 编辑模式下当前医生的科室名称

/** ======== 科室下拉（筛选区用） ======== */
const filterDepartments = ref([]) // { departmentId, departmentName }[]
const filterDeptLoading = ref(false)

/** 兼容科室 VO/Entity：departmentId/departmentName 或 department_id/department_name */
function mapDepartmentItem(x) {
  return {
    departmentId: x.departmentId ?? x.department_id ?? '',
    departmentName: x.departmentName ?? x.department_name ?? x.name ?? ''
  }
}

/**
 * 加载科室列表（用于新增/编辑弹窗）
 */
async function loadDepartmentsByHospital(hospitalId) {
  const hid = (hospitalId || '').trim()
  if (!hid) {
    departments.value = []
    return
  }

  deptLoading.value = true
  try {
    const res = await fetchDepartmentsByHospital(hid)
    // 处理分页数据格式
    if (res?.code === 200) {
      const pageData = res.data || {}
      const records = pageData.records || pageData.list || []
      departments.value = Array.isArray(records) ? records.map(mapDepartmentItem) : []
    } else {
      // 兼容直接返回数组的情况
      const data = (res?.data ?? res) || []
      departments.value = Array.isArray(data) ? data.map(mapDepartmentItem) : []
    }
  } catch (e) {
    departments.value = []
    // 弹窗里展示
    if (modal.visible) {
      modalError.value = e?.message || '科室加载失败'
    }
  } finally {
    deptLoading.value = false
  }
}

/**
 * 加载科室名称（用于编辑模式显示）
 */
async function loadDepartmentNameForEdit(hospitalId, departmentId) {
  const hid = (hospitalId || '').trim()
  const did = (departmentId || '').trim()
  if (!hid || !did) {
    currentDepartmentName.value = ''
    return
  }

  try {
    const res = await fetchDepartmentsByHospital(hid)
    // 处理分页数据格式
    let deptList = []
    if (res?.code === 200) {
      const pageData = res.data || {}
      const records = pageData.records || pageData.list || []
      deptList = Array.isArray(records) ? records.map(mapDepartmentItem) : []
    } else {
      // 兼容直接返回数组的情况
      const data = (res?.data ?? res) || []
      deptList = Array.isArray(data) ? data.map(mapDepartmentItem) : []
    }
    
    const dept = deptList.find(d => d.departmentId === did)
    if (dept) {
      currentDepartmentName.value = `${dept.departmentName}（${dept.departmentId}）`
    } else {
      currentDepartmentName.value = `科室ID: ${did}`
    }
  } catch (e) {
    console.error('加载科室名称失败:', e)
    currentDepartmentName.value = `科室ID: ${did}`
  }
}

/**
 * 加载科室列表（用于筛选下拉）
 * 当hospitalId为空时，加载所有科室（传入空字符串，获取所有院区的所有科室）
 * 当hospitalId有值时，加载指定院区的科室（传入hospitalId参数）
 */
async function loadFilterDepartmentsByHospital(hospitalId) {
  const hid = (hospitalId || '').trim()
  
  filterDeptLoading.value = true
  filterDepartments.value = []
  
  try {
    // 统一使用 pageDepartments API
    // 当 hospitalId 为空时传入空字符串，获取所有院区的所有科室
    // 当 hospitalId 有值时传入该值，获取指定院区的科室
    const params = {
      page: 1,
      size: 1000,
      hospitalId: hid  // 空字符串表示所有院区
    }
    
    const res = await pageDepartments(params)
    
    // 处理返回数据（pageDepartments 返回分页格式）
    if (res?.code === 200) {
      const pageData = res.data || {}
      const records = pageData.records || pageData.list || []
      filterDepartments.value = Array.isArray(records) ? records.map(mapDepartmentItem) : []
    } else {
      filterDepartments.value = []
    }
  } catch (e) {
    console.error('加载科室列表失败:', e)
    filterDepartments.value = []
  } finally {
    filterDeptLoading.value = false
  }
}

/**
 * 院区选择变化时的处理函数
 */
async function onHospitalChange() {
  // 重置科室选择
  filters.departmentId = ''
  // 加载对应的科室列表
  await loadFilterDepartmentsByHospital(filters.hospitalId)
  // 重新加载医生列表
  loadDoctors(1)
}

/** ======== 查询 ======== */
async function loadDoctors(page = 1) {
  pager.page = page
  loading.value = true
  error.value = ''

  try {
    const params = {
      page: pager.page,
      size: pager.size,
      keyword: (filters.keyword || '').trim(),
      hospitalId: (filters.hospitalId || '').trim(),
      departmentId: (filters.departmentId || '').trim()
    }
    Object.keys(params).forEach((key) => {
      if (!params[key]) delete params[key]
    })

    const res = await fetchDoctors(params)
    const pageObj = unwrapPage(res)

    items.value = pageObj.records || pageObj.list || []
    pager.total = pageObj.total ?? pageObj.totalCount ?? 0
  } catch (e) {
    error.value = e?.message || '加载失败'
    items.value = []
    pager.total = 0
  } finally {
    loading.value = false
  }
}

/** ======== 新增/编辑弹窗 ======== */
const modal = reactive({
  visible: false,
  mode: 'create' // create | edit
})

const modalError = ref('')
const modalSaving = ref(false)

const form = reactive({
  doctor_id: '',
  user_id: '',
  hospital_id: '',
  department_id: '',
  doctor_name: '',
  doctor_gender: '',
  doctor_idcard: '',
  title: '',
  doctor_phone: '',
  doctor_email: '',
  doctor_intro: '',
  avatar_url: '',
  user_phone: '',
  user_password: ''
})

function resetForm() {
  form.doctor_id = ''
  form.user_id = ''
  form.hospital_id = ''
  form.department_id = ''
  form.doctor_name = ''
  form.doctor_gender = ''
  form.doctor_idcard = ''
  form.title = ''
  form.doctor_phone = ''
  form.doctor_email = ''
  form.doctor_intro = ''
  form.avatar_url = ''
  form.user_phone = ''
  form.user_password = ''
  currentDepartmentName.value = '' // 重置科室名称
}

function openCreate() {
  resetForm()
  modal.mode = 'create'
  modal.visible = true
  modalError.value = ''
  departments.value = []
}

async function openEdit(row) {
  resetForm()
  modal.mode = 'edit'
  modal.visible = true
  modalError.value = ''
  currentDepartmentName.value = '' // 重置科室名称
  
  form.doctor_id = row.doctorId || ''
  form.user_id = row.userId || ''
  form.hospital_id = row.hospitalId || ''
  form.department_id = row.departmentId || ''
  form.doctor_name = row.doctorName || ''
  form.doctor_gender = row.doctorGender || ''
  form.doctor_idcard = row.doctorIdcard || ''
  form.title = row.title || ''
  form.doctor_phone = row.doctorPhone || ''
  form.doctor_email = row.doctorEmail || ''
  form.doctor_intro = row.doctorIntro || ''
  form.avatar_url = row.avatarUrl || ''
  form.user_phone = row.userPhone || ''
  
  // 编辑模式下加载科室信息，用于显示科室名称
  if (form.hospital_id && form.department_id) {
    await loadDepartmentNameForEdit(form.hospital_id, form.department_id)
  }
}

function closeModal() {
  modal.visible = false
  modalError.value = ''
  modalSaving.value = false
}

/** 校验：新增 */
function validateCreate() {
  const hid = (form.hospital_id || '').trim()
  if (!hid) return 'hospitalId 不能为空'
  if (!(form.department_id || '').trim()) return 'departmentId 不能为空'
  if (!(form.user_phone || '').trim()) return 'userPhone 不能为空'
  if (!(form.user_password || '').trim()) return 'userPassword 不能为空'
  if (!(form.doctor_name || '').trim()) return 'doctorName 不能为空'
  return ''
}

/** 校验：编辑 */
function validateEdit() {
  const hid = (form.hospital_id || '').trim()
  if (!hid) return 'hospitalId 不能为空'
  if (!(form.doctor_id || '').trim()) return 'doctorId 不能为空'
  if (!(form.doctor_name || '').trim()) return 'doctorName 不能为空'
  return ''
}

async function submitModal() {
  modalError.value = ''
  const msg = modal.mode === 'create' ? validateCreate() : validateEdit()
  if (msg) {
    modalError.value = msg
    return
  }

  modalSaving.value = true
  try {
    if (modal.mode === 'create') {
      const hid = form.hospital_id.trim()
      const payload = {
        hospitalId: hid,
        departmentId: form.department_id.trim(),
        userPhone: form.user_phone.trim(),
        userPassword: form.user_password.trim(),
        doctorName: form.doctor_name.trim(),
        doctorGender: form.doctor_gender || '',
        doctorIdcard: form.doctor_idcard.trim(),
        title: form.title || '',
        doctorPhone: form.doctor_phone || '',
        doctorIntro: form.doctor_intro || '',
        avatarUrl: form.avatar_url || ''
      }

      // 只在doctorEmail有值时才包含该字段，避免空字符串导致唯一约束冲突
      if (form.doctor_email && form.doctor_email.trim()) {
        payload.doctorEmail = form.doctor_email.trim()
      }
      await createDoctor(payload)
    } else {
      // 编辑：严格不让 user_id / hospital_id / department_id 参与更新；后端按 hospitalId 路由
      const hid = form.hospital_id.trim()
      const payload = {
        userPhone: form.user_phone.trim(),
        doctorName: form.doctor_name.trim(),
        doctorGender: form.doctor_gender || '',
        doctorIdcard: form.doctor_idcard.trim(),
        title: form.title || '',
        doctorPhone: form.doctor_phone || '',
        doctorIntro: form.doctor_intro || '',
        avatarUrl: form.avatar_url || ''
      }

      // 只在doctorEmail有值时才包含该字段，避免空字符串导致唯一约束冲突
      if (form.doctor_email && form.doctor_email.trim()) {
        payload.doctorEmail = form.doctor_email.trim()
      }

      await updateDoctor(form.doctor_id, hid, payload)
    }

    closeModal()
    loadDoctors(1)
  } catch (e) {
    modalError.value = e?.message || '提交失败'
  } finally {
    modalSaving.value = false
  }
}

/** ======== 删除 ======== */
const del = reactive({
  visible: false,
  saving: false,
  row: null
})

function confirmDelete(row) {
  del.row = row
  del.visible = true
}

function closeDelete() {
  del.visible = false
  del.row = null
  del.saving = false
}

async function doDelete() {
  if (!del.row) return
  const doctorId = del.row.doctorId
  const hospitalId = del.row.hospitalId
  if (!doctorId || !hospitalId) return

  del.saving = true
  try {
    await deleteDoctor(doctorId, hospitalId)
    closeDelete()
    loadDoctors(1)
  } catch (e) {
    error.value = e?.message || '删除失败'
  } finally {
    del.saving = false
  }
}

/**
 * ✅ 监听：院区ID变化时，自动刷新科室下拉
 * - 仅对"新增模式"生效（编辑模式禁止改院区/科室）
 */
watch(
    () => form.hospital_id,
    async (newVal, oldVal) => {
      if (!modal.visible) return
      if (modal.mode === 'edit') return

      const nv = (newVal || '').trim()
      const ov = (oldVal || '').trim()
      if (nv === ov) return

      form.department_id = ''
      modalError.value = ''
      await loadDepartmentsByHospital(nv)
    }
)

onMounted(async () => {
  // 初始化时加载所有科室（因为默认院区为空，表示所有院区）
  await loadFilterDepartmentsByHospital('')
  loadDoctors(1)
})
</script>

<style scoped>
.doctors-page { display: flex; flex-direction: column; gap: 14px; }

.toolbar-card,
.table-card {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.toolbar-card { display: flex; justify-content: space-between; gap: 14px; flex-wrap: wrap; }

.toolbar-left { display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }

.toolbar-right { display: flex; align-items: flex-end; gap: 10px; }

.field { display: flex; flex-direction: column; gap: 6px; min-width: 220px; }
.field.small { min-width: unset; width: 140px; }
.field label { font-size: 0.84rem; color: #64748b; font-weight: 800; }

.input,
.select,
.textarea {
  height: 38px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 0 12px;
  outline: none;
  background: #fff;
  color: #0f172a;
  font-weight: 650;
}

.textarea {
  height: auto;
  padding: 10px 12px;
  line-height: 1.45;
  resize: vertical;
}

.input:focus,
.select:focus,
.textarea:focus { border-color: #94a3b8; box-shadow: 0 0 0 3px rgba(148, 163, 184, 0.18); }

.select { cursor: pointer; }
.select.small { height: 34px; border-radius: 10px; }

.actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

.btn {
  border: 1px solid transparent;
  border-radius: 12px;
  height: 38px;
  padding: 0 14px;
  font-weight: 850;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all .12s ease;
  user-select: none;
}

.btn:disabled { opacity: 0.55; cursor: not-allowed; }

.btn-primary { background: #2563eb; color: #fff; }
.btn-primary:hover { filter: brightness(0.98); transform: translateY(-1px); }

.btn-ghost { background: #fff; color: #0f172a; border-color: #e5e7eb; }
.btn-ghost:hover { background: #f8fafc; }

.btn-danger { background: #ef4444; color: #fff; }
.btn-danger:hover { filter: brightness(0.98); transform: translateY(-1px); }

.btn-mini { height: 32px; padding: 0 10px; border-radius: 10px; font-size: 0.85rem; }

.section-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 14px; flex-wrap: wrap; padding-bottom: 10px; border-bottom: 1px solid #eef2f7; margin-bottom: 10px; }
.section-title { font-size: 1.05rem; font-weight: 950; color: #0f172a; }
.section-sub { font-size: 0.86rem; color: #64748b; margin-top: 2px; }

.table-right { display: flex; gap: 12px; align-items: flex-end; flex-wrap: wrap; }

.table-wrap { width: 100%; overflow: auto; border-radius: 12px; border: 1px solid #eef2f7; }
.admin-table { width: 100%; border-collapse: collapse; min-width: 980px; background: #fff; }
.admin-table th,
.admin-table td { padding: 12px 12px; border-bottom: 1px solid #eef2f7; text-align: left; white-space: nowrap; }
.admin-table th { font-size: 0.85rem; color: #64748b; font-weight: 900; background: #f8fafc; }
.admin-table tbody tr:hover td { background: #fbfdff; }

.empty { text-align: center; padding: 18px 12px; color: #64748b; font-weight: 700; }

.pager { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; padding-top: 12px; }
.pager-info { color: #64748b; font-weight: 850; }
.range { color: #64748b; font-weight: 800; }
.mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace; }

.error {
  background: #fff5f5;
  border: 1px solid #fecaca;
  color: #991b1b;
  border-radius: 14px;
  padding: 12px 14px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ===== Modal（复用订单管理弹窗风格） ===== */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.40);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
  z-index: 50;
}

.modal {
  width: min(980px, 96vw);
  background: #fff;
  border-radius: 16px;
  border: 1px solid #eef2f7;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.20);
  overflow: hidden;
}

.modal-sm { width: min(560px, 96vw); }

.modal-header {
  padding: 12px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #eef2f7;
}

.modal-title { font-weight: 950; color: #0f172a; }
.modal-body { padding: 14px; display: flex; flex-direction: column; gap: 12px; }

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 12px;
}

.field.full { grid-column: 1 / -1; }

@media (max-width: 860px) {
  .field { min-width: unset; width: 100%; }
  .form-grid { grid-template-columns: 1fr; }
  .admin-table { min-width: 860px; }
}

.modal-actions { display: flex; justify-content: flex-end; gap: 10px; padding-top: 6px; }

.kv {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 10px 14px;
  align-items: center;
}

.k { color: #64748b; font-weight: 900; font-size: 0.9rem; }
.v { color: #0f172a; font-weight: 750; }

.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg);} to { transform: rotate(360deg);} }
</style>
