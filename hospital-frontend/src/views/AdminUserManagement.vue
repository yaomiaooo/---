<template>
  <div class="users-page">
    <!-- 顶部筛选工具条（风格对齐：预约订单管理） -->
    <div class="toolbar-card">
      <div class="toolbar-left">
        <div class="field">
          <label>关键字</label>
          <input
              v-model="filters.keyword"
              class="input"
              placeholder="按手机号 / 用户ID 搜索"
              @keyup.enter="loadUsers(1)"
          />
        </div>

        <div class="field">
          <label>角色</label>
          <select v-model="filters.role" class="select" @change="loadUsers(1)">
            <option value="">全部</option>
            <option value="user">普通用户</option>
            <option value="doctor">医生</option>
            <option value="admin">管理员</option>
          </select>
        </div>

        <div class="actions">
          <button class="btn btn-primary btn-mini" @click="loadUsers(1)">
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
          新增用户
        </button>
      </div>
    </div>

    <!-- 批量操作条（对齐：预约订单管理） -->
    <div v-if="selectedIds.length > 0" class="batch-bar">
      <div class="batch-left">
        <div class="batch-chip">
          已选 <span class="mono">{{ selectedIds.length }}</span> 条
        </div>
        <div class="batch-chip mono">
          IDs: {{ selectedIds.slice(0, 6).join(", ") }}
          <span v-if="selectedIds.length > 6">...</span>
        </div>
      </div>

      <div class="batch-right">
        <button class="btn btn-mini" @click="clearSelection">
          <Icon icon="mdi:close-circle-outline" /> 清空选择
        </button>
        <button class="btn btn-danger btn-mini" :disabled="batchLoading" @click="batchResetPwd">
          <Icon v-if="batchLoading" icon="mdi:loading" class="spin" />
          <Icon v-else icon="mdi:lock-reset" />
          批量重置密码
        </button>
        <button class="btn btn-danger btn-mini" :disabled="batchLoading" @click="batchDelete">
          <Icon v-if="batchLoading" icon="mdi:loading" class="spin" />
          <Icon v-else icon="mdi:delete-outline" />
          批量删除
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
          <div class="section-title">用户列表</div>
          <div class="section-sub">
            支持按手机号/用户ID与角色筛选；支持勾选批量重置密码与批量删除。
          </div>
        </div>

        <div class="table-right">
          <div class="range mono">
            <span v-if="loading">加载中…</span>
            <!-- <span v-else>共 <span class="v mono">{{ pager.total }}</span> 条</span> -->
          </div>

          <div class="field small">
            <label>每页</label>
            <select class="select small" v-model.number="pager.pageSize" @change="loadUsers(1)">
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
            <th style="width: 48px;">
              <input
                  type="checkbox"
                  :checked="allSelectedOnPage"
                  :indeterminate="someSelectedOnPage"
                  @change="toggleAllOnPage"
              />
            </th>
            <th style="width: 230px;">用户ID</th>
            <th style="width: 180px;">手机号</th>
            <th style="width: 140px;">角色</th>
            <th style="width: 190px;">创建时间</th>
            <th style="width: 260px;">操作</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="loading">
            <td colspan="6" class="empty">加载中...</td>
          </tr>

          <tr v-else-if="users.length === 0">
            <td colspan="6" class="empty">暂无数据</td>
          </tr>

          <tr v-else v-for="u in users" :key="u.user_id">
            <td>
              <input
                  type="checkbox"
                  :checked="isSelected(u.user_id)"
                  @change="toggleRow(u.user_id)"
              />
            </td>

            <td class="mono">{{ u.user_id }}</td>
            <td class="mono">{{ u.user_phone }}</td>
            <td>
                <span class="badge" :class="roleBadgeClass(u.role)">
                  {{ roleLabel(u.role) }}
                </span>
            </td>
            <td class="mono">{{ formatTime(u.created_at) }}</td>

            <td>
              <div class="actions">
                <button class="btn btn-mini" @click="openEdit(u)">
                  <Icon icon="mdi:pencil-outline" /> 编辑
                </button>

                <button class="btn btn-danger btn-mini" @click="confirmResetPwd(u)">
                  <Icon icon="mdi:lock-reset" /> 重置密码
                </button>

                <button class="btn btn-danger btn-mini" @click="confirmDelete(u)">
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
          <button class="btn btn-ghost btn-mini" :disabled="pager.page <= 1 || loading" @click="loadUsers(pager.page - 1)">
            上一页
          </button>
          <button class="btn btn-ghost btn-mini" :disabled="pager.page >= totalPages || loading" @click="loadUsers(pager.page + 1)">
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 弹窗：新增/编辑（对齐：预约订单管理弹窗风格） -->
    <div v-if="modal.visible" class="modal-mask" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">
            {{ modal.mode === 'create' ? '新增用户' : '编辑用户' }}
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
              <label>手机号（userPhone）</label>
              <input v-model.trim="form.user_phone" class="input" placeholder="例如：138xxxxxx" />
            </div>

            <div class="field">
              <label>角色（role）</label>
              <!-- 编辑时禁止修改角色 -->
              <select v-model="form.role" class="select" :disabled="modal.mode === 'edit'">
                <option value="user">普通用户</option>
                <option value="doctor">医生</option>
                <option value="admin">管理员</option>
              </select>
            </div>

            <div class="field full">
              <label>
                密码（userPassword）
                <span v-if="modal.mode === 'edit'" class="muted">
                  （编辑不支持直接改密码，请用“重置密码”）
                </span>
              </label>
              <input
                  v-model="form.user_password"
                  class="input"
                  type="password"
                  :placeholder="modal.mode === 'create' ? '可留空（后端默认 123456），或不少于 6 位' : '编辑模式下此项不生效'"
                  :disabled="modal.mode === 'edit'"
              />
            </div>

            <div v-if="modal.mode === 'edit'" class="field full">
              <label>用户ID（userId）</label>
              <input class="input" :value="form.user_id" disabled />
            </div>
          </div>

          <div class="modal-actions">
            <button class="btn btn-ghost" @click="closeModal">取消</button>
            <button class="btn btn-primary" :disabled="modalSaving" @click="submitForm">
              <Icon v-if="modalSaving" icon="mdi:loading" class="spin" />
              保存
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Icon } from '@iconify/vue'
import {
  pageUsers,
  createUser,
  updateUser,
  resetPassword,
  deleteUser
} from '../api/adminUser'

const loading = ref(false)
const error = ref('')
const users = ref([])

const filters = reactive({
  keyword: '',
  role: ''
})

const pager = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const totalPages = computed(() => {
  const t = Math.ceil((pager.total || 0) / (pager.pageSize || 10))
  return Math.max(t, 1)
})

function roleLabel(role) {
  if (role === 'admin') return '管理员'
  if (role === 'doctor') return '医生'
  return '普通用户'
}

function roleBadgeClass(role) {
  if (role === 'admin') return 'badge-blue'
  if (role === 'doctor') return 'badge-green'
  return 'badge-gray'
}

function formatTime(val) {
  if (!val) return '-'
  const d = new Date(val)
  if (!Number.isNaN(d.getTime())) {
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  }
  const s = String(val).replace('T', ' ').replace('Z', '')
  return s.length > 19 ? s.slice(0, 19) : s
}

function resetFilters() {
  filters.keyword = ''
  filters.role = ''
  clearSelection()
  loadUsers(1)
}

/** 兼容分页返回：res.data / res */
function unwrapPage(res) {
  const body = res ?? {}
  const pageObj = body.data ?? body
  const records = pageObj.records ?? pageObj.list ?? []
  const total = pageObj.total ?? pageObj.totalCount ?? 0
  return { records, total }
}

/** 将后端 VO 映射为页面字段 */
function mapUserVOToRow(vo) {
  return {
    user_id: vo.userId ?? vo.user_id ?? '',
    user_phone: vo.userPhone ?? vo.user_phone ?? '',
    role: vo.role ?? 'user',
    created_at: vo.createdAt ?? vo.created_at ?? vo.createTime ?? vo.create_time ?? ''
  }
}

async function loadUsers(page = pager.page) {
  loading.value = true
  error.value = ''
  try {
    pager.page = page
    const res = await pageUsers({
      page: pager.page,
      size: pager.pageSize,
      keyword: filters.keyword || '',
      role: filters.role || ''
    })

    const { records, total } = unwrapPage(res)
    users.value = (records || []).map(mapUserVOToRow)
    pager.total = total
  } catch (e) {
    error.value = e?.message || '加载失败'
    users.value = []
    pager.total = 0
  } finally {
    loading.value = false
  }
}

/* ================= 批量选择（对齐：预约订单管理） ================= */
const selectedSet = ref(new Set())
const batchLoading = ref(false)

const selectedIds = computed(() => Array.from(selectedSet.value))

function isSelected(id) {
  return selectedSet.value.has(id)
}

function toggleRow(id) {
  if (selectedSet.value.has(id)) selectedSet.value.delete(id)
  else selectedSet.value.add(id)
}

function clearSelection() {
  selectedSet.value.clear()
}

const idsOnPage = computed(() => (users.value || []).map(u => u.user_id).filter(Boolean))

const allSelectedOnPage = computed(() => {
  const pageIds = idsOnPage.value
  if (pageIds.length === 0) return false
  return pageIds.every(id => selectedSet.value.has(id))
})

const someSelectedOnPage = computed(() => {
  const pageIds = idsOnPage.value
  if (pageIds.length === 0) return false
  const picked = pageIds.filter(id => selectedSet.value.has(id)).length
  return picked > 0 && picked < pageIds.length
})

function toggleAllOnPage(e) {
  const checked = e?.target?.checked
  const pageIds = idsOnPage.value
  if (checked) pageIds.forEach(id => selectedSet.value.add(id))
  else pageIds.forEach(id => selectedSet.value.delete(id))
}

async function batchResetPwd() {
  const ids = selectedIds.value
  if (ids.length === 0) return
  if (!confirm(`确定将所选 ${ids.length} 个用户的密码重置为 123456 吗？`)) return

  batchLoading.value = true
  try {
    const fails = []
    for (const id of ids) {
      try {
        await resetPassword(id)
      } catch (e) {
        fails.push({ id, msg: e?.message || '失败' })
      }
    }
    await loadUsers(pager.page)
    if (fails.length > 0) {
      error.value = `部分重置失败：${fails.slice(0, 3).map(x => `${x.id}`).join(', ')}${fails.length > 3 ? '...' : ''}`
    }
  } finally {
    batchLoading.value = false
  }
}

async function batchDelete() {
  const ids = selectedIds.value
  if (ids.length === 0) return
  if (!confirm(`确定删除所选 ${ids.length} 个用户吗？此操作不可恢复。`)) return

  batchLoading.value = true
  try {
    const fails = []
    for (const id of ids) {
      try {
        await deleteUser(id)
        selectedSet.value.delete(id)
      } catch (e) {
        fails.push({ id, msg: e?.message || '失败' })
      }
    }
    await loadUsers(1)
    if (fails.length > 0) {
      error.value = `部分删除失败：${fails.slice(0, 3).map(x => `${x.id}`).join(', ')}${fails.length > 3 ? '...' : ''}`
    }
  } finally {
    batchLoading.value = false
  }
}

/* ================= 新增/编辑 ================= */
const modal = reactive({
  visible: false,
  mode: 'create' // create | edit
})

const modalSaving = ref(false)
const modalError = ref('')

const form = reactive({
  user_id: '',
  user_phone: '',
  role: 'user',
  user_password: ''
})

function openCreate() {
  modal.visible = true
  modal.mode = 'create'
  modalError.value = ''
  form.user_id = ''
  form.user_phone = ''
  form.role = 'user'
  form.user_password = ''
}

function openEdit(u) {
  modal.visible = true
  modal.mode = 'edit'
  modalError.value = ''
  form.user_id = u.user_id
  form.user_phone = u.user_phone
  form.role = u.role
  form.user_password = ''
}

function closeModal() {
  modal.visible = false
  modalError.value = ''
  modalSaving.value = false
}

function validateForm() {
  if (!form.user_phone) return '手机号不能为空'
  if (!['user', 'doctor', 'admin'].includes(form.role)) return '角色取值非法'
  if (modal.mode === 'create') {
    if (form.user_password && form.user_password.length < 6) return '密码长度不足（不少于 6 位）'
  }
  return ''
}

async function submitForm() {
  modalError.value = ''
  const msg = validateForm()
  if (msg) {
    modalError.value = msg
    return
  }

  modalSaving.value = true
  try {
    if (modal.mode === 'create') {
      const payload = {
        userPhone: form.user_phone,
        role: form.role
      }
      if (form.user_password) payload.userPassword = form.user_password
      await createUser(payload)
    } else {
      // 编辑：只改手机号；角色不允许在前端变更（select 已禁用），此处保留原值提交以兼容后端校验
      await updateUser(form.user_id, {
        userPhone: form.user_phone,
        role: form.role
      })
    }

    closeModal()
    await loadUsers(1)
  } catch (e) {
    modalError.value = e?.message || '提交失败'
  } finally {
    modalSaving.value = false
  }
}

/* ================= 删除/重置密码 ================= */
async function confirmDelete(u) {
  if (!confirm(`确定删除用户 ${u.user_phone} 吗？此操作不可恢复。`)) return
  try {
    await deleteUser(u.user_id)
    selectedSet.value.delete(u.user_id)
    await loadUsers(pager.page)
  } catch (e) {
    alert(e?.message || '删除失败')
  }
}

async function confirmResetPwd(u) {
  if (!confirm(`确定将用户 ${u.user_phone} 的密码重置为 123456 吗？`)) return
  try {
    await resetPassword(u.user_id)
    await loadUsers(pager.page)
  } catch (e) {
    alert(e?.message || '重置失败')
  }
}

onMounted(() => {
  loadUsers(1)
})
</script>

<style scoped>
.users-page { display: flex; flex-direction: column; gap: 14px; }

/* 卡片（对齐：预约订单管理） */
.toolbar-card,
.table-card {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.toolbar-card { display: flex; justify-content: space-between; gap: 14px; flex-wrap: wrap; }

.toolbar-left {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.toolbar-right { display: flex; align-items: flex-end; gap: 10px; }

.field { display: flex; flex-direction: column; gap: 6px; min-width: 220px; }
.field.small { min-width: unset; width: 140px; }
.field label { font-size: 0.84rem; color: #64748b; font-weight: 800; }

.input,
.select {
  height: 38px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 0 12px;
  outline: none;
  background: #fff;
  color: #0f172a;
  font-weight: 650;
}

.input:focus,
.select:focus { border-color: #94a3b8; box-shadow: 0 0 0 3px rgba(148, 163, 184, 0.18); }

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
  transition: all 0.15s ease;
  user-select: none;
  background: transparent;
  color: #0f172a;
}

.btn:disabled { opacity: 0.55; cursor: not-allowed; }

.btn-primary { background: #2563eb; color: #fff; }
.btn-primary:hover { filter: brightness(0.98); transform: translateY(-1px); }

.btn-ghost { background: transparent; border-color: #e2e8f0; color: #0f172a; }
.btn-ghost:hover { background: #f8fafc; }

.btn-mini { height: 32px; padding: 0 10px; border-radius: 10px; font-size: 0.85rem; }

.btn-danger { background: #ef4444; color: #fff; }
.btn-danger:hover { filter: brightness(0.98); transform: translateY(-1px); }

/* 批量条（对齐：预约订单管理） */
.batch-bar {
  background: rgba(37, 99, 235, 0.05);
  border: 1px dashed rgba(37, 99, 235, 0.25);
  border-radius: 14px;
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.batch-left { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }
.batch-right { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }

.batch-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid rgba(37, 99, 235, 0.18);
  color: #0f172a;
  font-weight: 850;
}

/* 标题区 */
.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  padding-bottom: 10px;
  border-bottom: 1px solid #eef2f7;
  margin-bottom: 10px;
}

.section-title { font-size: 1.05rem; font-weight: 950; color: #0f172a; }
.section-sub { font-size: 0.86rem; color: #64748b; margin-top: 2px; }
.table-right { display: flex; gap: 12px; align-items: flex-end; flex-wrap: wrap; }

.range { color: #64748b; font-weight: 800; }
.mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace; }
.v { color: #0f172a; }

/* 表格（对齐：预约订单管理） */
.table-wrap { width: 100%; overflow: auto; border-radius: 12px; border: 1px solid #eef2f7; }
.admin-table { width: 100%; border-collapse: collapse; min-width: 980px; background: #fff; }
.admin-table th,
.admin-table td { padding: 12px 12px; border-bottom: 1px solid #eef2f7; text-align: left; white-space: nowrap; }

.admin-table th { font-size: 0.85rem; color: #64748b; font-weight: 900; background: #f8fafc; }
.admin-table tbody tr:hover td { background: #fbfdff; }

.empty { text-align: center; padding: 18px 12px; color: #64748b; font-weight: 700; }

/* Badge（对齐：预约订单管理） */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 900;
  border: 1px solid transparent;
}

.badge-blue { background: rgba(37, 99, 235, 0.10); color: #1d4ed8; border-color: rgba(37, 99, 235, 0.25); }
.badge-green { background: rgba(16, 185, 129, 0.12); color: #047857; border-color: rgba(16, 185, 129, 0.25); }
.badge-gray { background: rgba(100, 116, 139, 0.12); color: #334155; border-color: rgba(100, 116, 139, 0.25); }

.muted { color: #64748b; font-weight: 650; margin-left: 8px; }

/* 分页（对齐：预约订单管理） */
.pager { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; padding-top: 12px; }
.pager-info { color: #64748b; font-weight: 850; }

/* 错误条（对齐：预约订单管理） */
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

/* Modal（对齐：预约订单管理） */
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
  width: min(860px, 96vw);
  background: #fff;
  border-radius: 16px;
  border: 1px solid #eef2f7;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.20);
  overflow: hidden;
}

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
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; padding-top: 6px; }

@media (max-width: 860px) {
  .field { min-width: unset; width: 100%; }
  .form-grid { grid-template-columns: 1fr; }
  .admin-table { min-width: 860px; }
}

.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg);} to { transform: rotate(360deg);} }
</style>
