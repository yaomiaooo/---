<template>
  <div class="page">
    <!-- 筛选区 -->
    <div class="card filter-card">
      <div class="filter-row">
        <div class="field">
          <label>月份</label>
          <input type="month" v-model="filters.month" />
        </div>

        <div class="field actions">
          <button class="btn primary" @click="loadRecords" :disabled="loading">
            {{ loading ? '加载中...' : '查询' }}
          </button>
          <button class="btn" @click="resetFilters" :disabled="loading">重置</button>
        </div>
      </div>

      <div class="hint">
        按月查询医生历史就诊记录，可用于病历回溯与随访。
      </div>
    </div>


    <!-- 表格区 -->
    <div class="card table-card">
      <div class="table-head">
        <h3>病历档案 <small>Visit Records</small></h3>
        <div class="right">
          <span v-if="error" class="error">{{ error }}</span>
        </div>
      </div>

      <div class="table-wrapper">
        <table>
          <thead>
          <tr>
            <th style="width: 120px;">日期</th>
            <th style="width: 110px;">时间段</th>
            <th style="width: 140px;">患者</th>
            <th style="width: 140px;">预约号</th>
            <th style="width: 120px;">科室</th>
            <th style="width: 120px;">诊断</th>
            <th style="width: 140px;">操作</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="!loading && records.length === 0">
            <td colspan="7" class="empty">暂无数据</td>
          </tr>

          <tr v-for="r in records" :key="r.record_id">
            <td class="mono">{{ r.visit_date }}</td>
            <td><span class="badge">{{ r.time_slot || '-' }}</span></td>
            <td>
              <div class="patient">
                <div class="pname">{{ r.patient_name }}</div>
                <div class="psub">{{ r.patient_phone || '' }}</div>
              </div>
            </td>
            <td class="mono">{{ r.order_no || r.record_no || '-' }}</td>
            <td>{{ r.department_name || '-' }}</td>
            <td class="ellipsis" :title="r.summary || ''">{{ r.summary || '-' }}</td>
            <td>
              <button class="btn-action primary" @click="openDetail(r)">查看详情</button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="pager">
        <div class="tip">第 {{ page }} / {{ totalPages }} 页</div>
        <div class="controls">
          <button class="btn" @click="prevPage" :disabled="loading || page <= 1">上一页</button>
          <button class="btn" @click="nextPage" :disabled="loading || page >= totalPages">下一页</button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="detailVisible" class="modal-mask" @click.self="detailVisible=false">
      <div class="modal">
        <div class="modal-head">
          <h3>就诊详情</h3>
          <button class="x" @click="detailVisible=false">×</button>
        </div>
        <div class="modal-body">
          <div class="kv">
            <div><span class="k">日期</span><span class="v">{{ detail.visit_date }}</span></div>
            <div><span class="k">时间段</span><span class="v">{{ detail.time_slot || '-' }}</span></div>
            <div><span class="k">患者</span><span class="v">{{ detail.patient_name }}</span></div>
            <div><span class="k">预约号</span><span class="v">{{ detail.order_no || detail.record_no || '-' }}</span></div>
            <div><span class="k">科室</span><span class="v">{{ detail.department_name || '-' }}</span></div>
          </div>

          <div class="block">
            <div class="block-title">诊断</div>
            <div class="block-text">{{ detail.summary || '—' }}</div>
          </div>


        </div>
        <div class="modal-foot">
          <button class="btn primary" @click="detailVisible=false">关闭</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

const loading = ref(false)
const error = ref('')
const records = ref([])

const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = ref({
  month: ''   // YYYY-MM
})

const detailVisible = ref(false)
const detail = ref({})

function pad(n) { return String(n).padStart(2, '0') }

function initDefaultMonth() {
  const now = new Date()
  filters.value.month = `${now.getFullYear()}-${pad(now.getMonth() + 1)}`
}

function buildUrl() {
  const base = 'http://localhost:8080/api/appointments/visit-records'
  const qs = new URLSearchParams()
  if (filters.value.month) qs.set('month', filters.value.month)
  qs.set('page', String(page.value))
  qs.set('pageSize', String(pageSize.value))
  return `${base}?${qs.toString()}`
}

function normalize(list) {
  return (list || []).map(x => ({
    record_id: x.recordId ?? x.record_id ?? x.id ?? `${x.visitDate || ''}-${x.orderNo || ''}-${Math.random()}`,
    visit_date: x.visitDate ? x.visitDate.toString().slice(0, 10) : (x.visit_date || '').slice(0, 10),
    time_slot: x.timeSlot || x.time_slot || '',
    patient_name: x.patientName || x.patient_name || '未知',
    patient_phone: x.patientPhone || x.patient_phone || '',
    order_no: x.orderNo || x.order_no || '',
    record_no: x.recordNo || x.record_no || '',
    department_name: x.departmentName || x.department_name || '',
    summary: x.summary || x.diagnosis || '',
    advice: x.advice || ''
  }))
}

async function loadRecords() {
  loading.value = true
  error.value = ''
  try {
    const url = buildUrl()
    const token = localStorage.getItem('hospital_token')
    const resp = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      }
    })
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)

    // 检查响应内容类型
    const contentType = resp.headers.get('content-type')
    if (!contentType || !contentType.includes('application/json')) {
      // 如果不是JSON响应，可能是HTML错误页面
      const text = await resp.text()
      console.error('收到非JSON响应:', text.substring(0, 200))
      throw new Error('服务器返回了意外的响应格式，请检查服务状态')
    }

    const json = await resp.json()
    if (json.code !== 200) throw new Error(json.message || '请求失败')

    // 处理分页数据
    const pageData = json.data
    if (pageData && pageData.records) {
      records.value = normalize(pageData.records)
      total.value = pageData.total
    } else {
      records.value = []
      total.value = 0
    }
  } catch (e) {
    console.error('加载病历记录失败:', e)
    error.value = '加载病历记录失败，请稍后重试'
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function mockData() {
  // 简单 mock：按页返回
  const baseDate = filters.value.month ? `${filters.value.month}-` : '2025-12-'
  const all = [
    { record_id: 'r1', visit_date: baseDate + '03', time_slot: '08:00-10:00', patient_name: '张伟', patient_phone: '138****1234', order_no: 'A20251203001', department_name: '心血管内科', summary: '胸闷、心悸，建议进一步检查', advice: '心电图/血脂/随访' },
    { record_id: 'r2', visit_date: baseDate + '07', time_slot: '13:30-17:00', patient_name: '李秀英', patient_phone: '139****9876', order_no: 'A20251207012', department_name: '心血管内科', summary: '高血压复诊，调整用药', advice: '低盐饮食，2周复诊' },
    { record_id: 'r3', visit_date: baseDate + '09', time_slot: '19:00-21:00', patient_name: '王强', patient_phone: '137****2222', order_no: 'A20251209005', department_name: '心血管内科', summary: '胸痛待查，建议急查', advice: '必要时急诊处理' },
    { record_id: 'r4', visit_date: baseDate + '18', time_slot: '08:00-10:00', patient_name: '陈静', patient_phone: '136****3344', order_no: 'A20251218009', department_name: '心血管内科', summary: '药物不良反应评估', advice: '调整剂量，观察' },
  ]

  const start = (page.value - 1) * pageSize.value
  const end = start + pageSize.value
  return { total: all.length, list: all.slice(start, end) }
}

function resetFilters() {
  initDefaultMonth()
  page.value = 1
  loadRecords()
}

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

function prevPage() {
  if (page.value > 1) {
    page.value--
    loadRecords()
  }
}

function nextPage() {
  if (page.value < totalPages.value) {
    page.value++
    loadRecords()
  }
}

function openDetail(r) {
  detail.value = r
  detailVisible.value = true
}


onMounted(() => {
  initDefaultMonth()
  loadRecords()
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  padding: 16px 18px;
}

.filter-row {
  display: flex;
  justify-content: left;
  gap: 50px;
  align-items: end;
}

.field label {
  display: block;
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 6px;
}

.field input {
  width: 100%;
  padding: 10px 10px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  outline: none;
}
.field input:focus {
  border-color: #004ea2;
  box-shadow: 0 0 0 3px rgba(0, 78, 162, 0.10);
}

.actions { display: flex; justify-content: flex-end; gap: 10px; }
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
  font-weight: 700;
}
.btn.primary:hover { background: #003d80; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }

.hint { margin-top: 10px; font-size: 0.82rem; color: #999; }


.stat {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  padding: 14px 16px;
}
.stat .k { font-size: 0.85rem; color: #999; }
.stat .v { margin-top: 6px; font-size: 1.4rem; font-weight: 800; color: #004ea2; }

.table-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.table-head h3 { margin: 0; font-size: 1.1rem; color: #333; }
.table-head small { color: #999; font-weight: normal; margin-left: 8px; }
.error { color: #ff4d4f; font-size: 0.85rem; }

.table-wrapper { overflow: auto; }
table { width: 100%; border-collapse: collapse; }
th {
  text-align: left;
  padding: 14px 12px;
  color: #999;
  font-size: 0.85rem;
  border-bottom: 1px solid #eee;
  font-weight: 600;
}
td {
  padding: 14px 12px;
  border-bottom: 1px solid #f7f7f7;
  color: #333;
}
tr:hover { background: #f0f7ff; }

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  color: #004ea2;
  font-weight: 700;
}

.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  background: #e6f7ff;
  color: #004ea2;
  font-size: 0.85rem;
  font-weight: 700;
}

.patient .pname { font-weight: 700; }
.patient .psub { font-size: 0.8rem; color: #999; margin-top: 2px; }

.ellipsis {
  max-width: 260px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}


.btn-action { border: none; background: none; cursor: pointer; font-size: 0.9rem; margin-right: 10px; }
.btn-action.primary { color: #004ea2; font-weight: 800; }
.btn-action.text { color: #999; }
.btn-action:hover { text-decoration: underline; }

.empty { text-align: center; color: #999; padding: 18px 0; }

.pager {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #999;
  font-size: 0.85rem;
}
.pager .controls { display: flex; gap: 10px; }

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal {
  width: min(720px, calc(100vw - 40px));
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  overflow: hidden;
}
.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.modal-head h3 { margin: 0; font-size: 1.05rem; }
.modal-head .x {
  border: none; background: none; cursor: pointer;
  font-size: 22px; line-height: 22px; color: #999;
}
.modal-body { padding: 14px 16px; }
.kv { display: grid; grid-template-columns: 1fr 1fr; gap: 10px 16px; }
.k { color: #999; margin-right: 8px; }
.v { color: #333; font-weight: 700; }

.block { margin-top: 12px; }
.block-title { color: #666; font-weight: 800; margin-bottom: 6px; }
.block-text { background: #fafafa; border: 1px solid #f0f0f0; border-radius: 10px; padding: 10px; color: #333; }

.modal-foot {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
}
</style>

