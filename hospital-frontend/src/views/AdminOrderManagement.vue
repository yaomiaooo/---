<template>
  <div class="orders-page">
    <!-- 顶部筛选工具条 -->
    <div class="toolbar-card">
      <div class="toolbar-left">
        <div class="field">
          <label>关键字</label>
          <input
              v-model="filters.keyword"
              class="input"
              placeholder="按订单号 / 用户手机号 / 就诊人姓名检索"
              @keyup.enter="loadOrders(1)"
          />
        </div>

        <div class="field">
          <label>状态</label>
          <select v-model="filters.status" class="select" @change="loadOrders(1)">
            <option value="">全部</option>
            <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>

        <div class="field">
          <label>院区ID</label>
          <input
              v-model="filters.hospitalId"
              class="input"
              placeholder="hospitalId（可选）"
              @keyup.enter="loadOrders(1)"
          />
        </div>

        <div class="range">
          <div class="field">
            <label>开始日期</label>
            <input v-model="filters.dateFrom" class="input" type="date" />
          </div>
          <div class="range-sep">-</div>
          <div class="field">
            <label>结束日期</label>
            <input v-model="filters.dateTo" class="input" type="date" />
          </div>
        </div>

        <button class="btn btn-primary" @click="loadOrders(1)">
          <Icon icon="mdi:magnify" /> 查询
        </button>

        <button class="btn btn-ghost" @click="resetFilters">
          <Icon icon="mdi:reload" /> 重置
        </button>
      </div>

      <!-- 右侧批量操作提示 -->
      <div class="toolbar-right">
        <div class="note">批量操作需要先在表格中勾选订单</div>
      </div>
    </div>

    <!-- 批量操作条 -->
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
        <select v-model="batchStatus" class="select">
          <option value="">选择状态</option>
          <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
        </select>
        <button class="btn btn-mini" :disabled="!batchStatus || batchUpdating" @click="doBatchUpdate">
          <Icon icon="mdi:playlist-edit" /> 批量更新
        </button>
        <button class="btn btn-mini btn-ghost" @click="clearSelection">
          <Icon icon="mdi:close" /> 清空选择
        </button>
      </div>
    </div>

    <div v-if="error" class="error">
      <Icon icon="mdi:alert-circle-outline" />
      {{ error }}
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <div class="section-header">
        <div>
          <div class="section-title">预约订单管理</div>
          <div class="section-sub">
            
          </div>
        </div>
        <div class="pager-info">
          <span v-if="loading">加载中...</span>
          <!-- <span v-else>共 {{ pager.total }} 条</span> -->
        </div>
      </div>

      <table class="admin-table">
        <thead>
        <tr>
          <th style="width: 44px">
            <input type="checkbox" :checked="allChecked" @change="toggleCheckAll($event)" />
          </th>
          <th style="width: 160px">订单号</th>
          <th style="width: 90px">状态</th>
          <th style="width: 90px">院区</th>
          <th style="width: 140px">用户手机号</th>
          <th style="width: 120px">就诊人</th>
          <th style="width: 160px">医生</th>
          <th style="width: 160px">科室</th>
          <th style="width: 180px">排班时间</th>
          <th style="width: 180px">创建时间</th>
          <th style="width: 220px">操作</th>
        </tr>
        </thead>

        <tbody>
        <tr v-if="loading">
          <td colspan="11" class="empty">加载中...</td>
        </tr>

        <tr v-else-if="orders.length === 0">
          <td colspan="11" class="empty">暂无数据</td>
        </tr>

        <tr v-else v-for="o in orders" :key="o.appointmentId">
          <td>
            <input type="checkbox" :checked="selectedSet.has(o.appointmentId)" @change="toggleRow(o)" />
          </td>

          <td class="mono">{{ o.appointmentId }}</td>

          <td>
            <span class="badge" :class="statusBadgeClass(o.status)">{{ o.status }}</span>
          </td>

          <td class="mono">
            {{ o.hospitalId }}
            <span v-if="o.hospitalName" class="muted">({{ o.hospitalName }})</span>
          </td>

          <td class="mono">{{ o.userPhone || "-" }}</td>

          <td>
            <div>{{ o.patientName || "-" }}</div>
            <div class="muted mono" v-if="o.patientPhone">{{ o.patientPhone }}</div>
          </td>

          <td>
            <div>{{ o.doctorName || "-" }}</div>
            <div class="muted mono" v-if="o.doctorId">{{ o.doctorId }}</div>
          </td>

          <td>
            <div>{{ o.departmentName || "-" }}</div>
            <div class="muted mono" v-if="o.departmentId">{{ o.departmentId }}</div>
          </td>

          <td class="mono">
            <div>{{ o.workDate || "-" }}</div>
            <div class="muted">{{ o.timeSlot || "" }}</div>
          </td>

          <td class="mono">{{ formatTime(o.createdAt) }}</td>

          <td class="actions">
            <button class="btn btn-mini" @click="openDetail(o)">
              <Icon icon="mdi:eye-outline" /> 详情
            </button>

            <select v-model="o._nextStatus" class="select">
              <option value="">改状态</option>
              <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
            </select>

            <button
                class="btn btn-mini"
                :disabled="!o._nextStatus || singleUpdatingId === o.appointmentId"
                @click="doUpdateStatus(o)"
            >
              <Icon icon="mdi:check" /> 保存
            </button>
          </td>
        </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pager">
        <button class="btn btn-ghost" :disabled="pager.page <= 1" @click="loadOrders(pager.page - 1)">上一页</button>
        <div class="pager-info">
          第 <span class="mono">{{ pager.page }}</span> / <span class="mono">{{ totalPages }}</span> 页（每页
          {{ pager.pageSize }} 条）
        </div>
        <button class="btn btn-ghost" :disabled="pager.page >= totalPages" @click="loadOrders(pager.page + 1)">
          下一页
        </button>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="detail.visible" class="modal-mask" @click.self="closeDetail">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">订单详情</div>
          <button class="btn btn-ghost btn-mini" @click="closeDetail">
            <Icon icon="mdi:close" /> 关闭
          </button>
        </div>

        <div class="modal-body">
          <div v-if="detail.loading" class="empty">加载中...</div>

          <template v-else>
            <div class="kv">
              <div class="k">订单号</div>
              <div class="v mono">{{ detail.data?.appointmentId || "-" }}</div>

              <div class="k">状态</div>
              <div class="v">
                <span class="badge" :class="statusBadgeClass(detail.data?.status)">{{ detail.data?.status }}</span>
              </div>

              <div class="k">院区</div>
              <div class="v mono">
                {{ detail.data?.hospitalId }}
                {{ detail.data?.hospitalName ? "(" + detail.data?.hospitalName + ")" : "" }}
              </div>

              <div class="k">用户手机号</div>
              <div class="v mono">{{ detail.data?.userPhone || "-" }}</div>

              <div class="k">就诊人</div>
              <div class="v">{{ detail.data?.patientName || "-" }}</div>

              <div class="k">医生</div>
              <div class="v">{{ detail.data?.doctorName || "-" }}</div>

              <div class="k">科室</div>
              <div class="v">{{ detail.data?.departmentName || "-" }}</div>

              <div class="k">排班</div>
              <div class="v mono">{{ detail.data?.workDate || "-" }} {{ detail.data?.timeSlot || "" }}</div>

              <div class="k">创建时间</div>
              <div class="v mono">{{ formatTime(detail.data?.createdAt) }}</div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { Icon } from "@iconify/vue";
import {
  pageAppointments,
  getAppointmentDetail,
  updateAppointmentStatus,
  batchUpdateAppointmentStatus,
} from "../api/adminAppointment";

/**
 * 不修改 request.js：
 * request.js 的响应拦截器会返回后端 Result：{ code, message, data }。
 * 分页数据在 data 里，因此本页面必须从 res.data.records/res.data.total 读取。
 */
const loading = ref(false);
const error = ref("");
const orders = ref([]);

const filters = reactive({
  keyword: "",
  status: "",
  hospitalId: "",
  departmentId: "",
  doctorId: "",
  scheduleId: "",
  dateFrom: "",
  dateTo: "",
});

const pager = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
});

const totalPages = computed(() => {
  const t = Math.ceil((pager.total || 0) / pager.pageSize);
  return Math.max(t, 1);
});

const statusOptions = ["BOOKED", "CANCELLED", "COMPLETED", "NO_SHOW"];

function formatTime(val) {
  if (!val) return "-";
  const s = String(val).replace("T", " ").replace("Z", "");
  return s.length > 19 ? s.slice(0, 19) : s;
}

function toDateTimeRange(dateFrom, dateTo) {
  const startTime = dateFrom ? `${dateFrom} 00:00:00` : "";
  const endTime = dateTo ? `${dateTo} 23:59:59` : "";
  return { startTime, endTime };
}

/**
 * 兼容解析分页：
 * - request.js -> Result：res.data = page
 * - 未来若某接口直接返回 page，也兼容
 */
function unwrapPage(res) {
  const body = res ?? {};
  const pageObj = body.data ?? body;
  const records = pageObj.records ?? pageObj.list ?? [];
  const total = Number(pageObj.total ?? 0);
  return { records, total };
}

/** 统一映射字段，避免 camelCase/snake_case 导致前端列空白 */
function mapVO(x) {
  return {
    appointmentId: x.appointmentId ?? x.appointment_id,
    userId: x.userId ?? x.user_id,
    patientId: x.patientId ?? x.patient_id,
    scheduleId: x.scheduleId ?? x.schedule_id,
    hospitalId: x.hospitalId ?? x.hospital_id,
    status: x.status,
    createdAt: x.createdAt ?? x.created_at,

    userPhone: x.userPhone ?? x.user_phone,
    patientName: x.patientName ?? x.patient_name,
    patientPhone: x.patientPhone ?? x.patient_phone,
    doctorId: x.doctorId ?? x.doctor_id,
    doctorName: x.doctorName ?? x.doctor_name,
    departmentId: x.departmentId ?? x.department_id,
    departmentName: x.departmentName ?? x.department_name,
    hospitalName: x.hospitalName ?? x.hospital_name,
    workDate: x.workDate ?? x.work_date,
    timeSlot: x.timeSlot ?? x.time_slot,

    _nextStatus: "",
  };
}

async function loadOrders(page = pager.page) {
  loading.value = true;
  error.value = "";
  try {
    pager.page = page;

    const { startTime, endTime } = toDateTimeRange(filters.dateFrom, filters.dateTo);

    const res = await pageAppointments({
      page: pager.page,
      size: pager.pageSize,
      keyword: filters.keyword || "",
      status: filters.status || "",
      hospitalId: filters.hospitalId || "",
      departmentId: filters.departmentId || "",
      doctorId: filters.doctorId || "",
      scheduleId: filters.scheduleId || "",
      startTime,
      endTime,
    });

    const { records, total } = unwrapPage(res);
    orders.value = (records || []).map(mapVO);
    pager.total = total;

    // 刷新/切页后，移除不在当前页的勾选，避免批量误操作
    const currentIds = new Set(orders.value.map((x) => x.appointmentId));
    selectedSet.value = new Set([...selectedSet.value].filter((id) => currentIds.has(id)));
  } catch (e) {
    error.value = e?.message || "加载失败";
    orders.value = [];
    pager.total = 0;
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  filters.keyword = "";
  filters.status = "";
  filters.hospitalId = "";
  filters.departmentId = "";
  filters.doctorId = "";
  filters.scheduleId = "";
  filters.dateFrom = "";
  filters.dateTo = "";
  loadOrders(1);
}

function statusBadgeClass(status) {
  switch (status) {
    case "BOOKED":
      return "badge-blue";
    case "COMPLETED":
      return "badge-green";
    case "CANCELLED":
      return "badge-gray";
    case "NO_SHOW":
      return "badge-red";
    default:
      return "badge-orange";
  }
}

/** 勾选/批量 */
const selectedSet = ref(new Set());
const batchStatus = ref("");
const batchUpdating = ref(false);

const selectedIds = computed(() => Array.from(selectedSet.value));

const allChecked = computed(() => {
  if (!orders.value.length) return false;
  return orders.value.every((o) => selectedSet.value.has(o.appointmentId));
});

function toggleRow(row) {
  const id = row.appointmentId;
  const next = new Set(selectedSet.value);
  if (next.has(id)) next.delete(id);
  else next.add(id);
  selectedSet.value = next;
}

function toggleCheckAll(evt) {
  const checked = evt?.target?.checked;
  const next = new Set(selectedSet.value);
  if (checked) {
    orders.value.forEach((o) => next.add(o.appointmentId));
  } else {
    orders.value.forEach((o) => next.delete(o.appointmentId));
  }
  selectedSet.value = next;
}

function clearSelection() {
  selectedSet.value = new Set();
  batchStatus.value = "";
}

async function doBatchUpdate() {
  if (!batchStatus.value) return;
  if (selectedIds.value.length === 0) return;

  batchUpdating.value = true;
  try {
    await batchUpdateAppointmentStatus(selectedIds.value, batchStatus.value);
    clearSelection();
    await loadOrders(pager.page);
  } catch (e) {
    error.value = e?.message || "批量更新失败";
  } finally {
    batchUpdating.value = false;
  }
}

/** 单条更新 */
const singleUpdatingId = ref("");

async function doUpdateStatus(row) {
  if (!row._nextStatus) return;
  singleUpdatingId.value = row.appointmentId;
  try {
    await updateAppointmentStatus(row.appointmentId, row._nextStatus);
    row._nextStatus = "";
    await loadOrders(pager.page);
  } catch (e) {
    error.value = e?.message || "更新失败";
  } finally {
    singleUpdatingId.value = "";
  }
}

/** 详情 */
const detail = reactive({
  visible: false,
  loading: false,
  data: null,
});

async function openDetail(row) {
  detail.visible = true;
  detail.loading = true;
  detail.data = null;
  try {
    const res = await getAppointmentDetail(row.appointmentId);
    const body = res ?? {};
    const data = body.data ?? body;
    detail.data = mapVO(data);
  } catch (e) {
    error.value = e?.message || "加载详情失败";
  } finally {
    detail.loading = false;
  }
}

function closeDetail() {
  detail.visible = false;
  detail.loading = false;
  detail.data = null;
}

onMounted(() => {
  loadOrders(1);
});
</script>

<style scoped>
.orders-page { display: flex; flex-direction: column; gap: 14px; }

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
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
}

.toolbar-right { display: flex; align-items: center; justify-content: flex-end; }

.field { display: flex; flex-direction: column; gap: 6px; min-width: 190px; }
.field label { font-size: 0.82rem; color: #64748b; font-weight: 700; }

.input,
.select {
  height: 38px;
  padding: 0 12px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  outline: none;
  background: #fff;
  color: #0f172a;
  transition: all 0.15s ease;
}

.input:focus,
.select:focus { border-color: #93c5fd; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15); }

.range { display: flex; align-items: flex-end; gap: 10px; }
.range-sep { height: 38px; display: flex; align-items: center; color: #94a3b8; }

.btn {
  height: 38px;
  border-radius: 12px;
  border: 1px solid transparent;
  padding: 0 14px;
  cursor: pointer;
  font-weight: 800;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  user-select: none;
  transition: all 0.15s ease;
}

.btn-primary { background: #2563eb; color: #fff; }
.btn-primary:hover { filter: brightness(0.98); transform: translateY(-1px); }

.btn-ghost { background: transparent; border-color: #e2e8f0; color: #0f172a; }
.btn-ghost:hover { background: #f8fafc; }

.btn-mini { height: 32px; padding: 0 10px; border-radius: 10px; font-size: 0.85rem; }
.btn-danger { background: #ef4444; color: #fff; }
.btn-danger:hover { filter: brightness(0.98); transform: translateY(-1px); }

.actions { display: flex; gap: 10px; align-items: center; }

.error {
  background: #fff5f5;
  border: 1px solid #fecaca;
  color: #991b1b;
  border-radius: 14px;
  padding: 12px 14px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.note { color: #64748b; font-size: 0.9rem; font-weight: 700; }

.table-card { padding-top: 12px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.section-title { font-size: 1.05rem; font-weight: 900; color: #0f172a; }
.section-sub { margin-top: 4px; font-size: 0.85rem; color: #64748b; font-weight: 600; }

.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th { text-align: left; padding: 14px; border-bottom: 1px solid #eef2f7; background: #f8fafc; color: #64748b; font-size: 0.85rem; font-weight: 700; }
.admin-table td { padding: 14px; border-bottom: 1px solid #eef2f7; color: #334155; font-size: 0.9rem; vertical-align: middle; }
.empty { text-align: center; color: #94a3b8; font-weight: 700; padding: 18px !important; }

.badge { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 999px; font-size: 0.78rem; font-weight: 900; border: 1px solid transparent; }
.badge-blue { background: rgba(37, 99, 235, 0.10); color: #1d4ed8; border-color: rgba(37, 99, 235, 0.25); }
.badge-green { background: rgba(16, 185, 129, 0.12); color: #047857; border-color: rgba(16, 185, 129, 0.25); }
.badge-gray { background: rgba(100, 116, 139, 0.12); color: #334155; border-color: rgba(100, 116, 139, 0.22); }
.badge-red { background: rgba(239, 68, 68, 0.12); color: #b91c1c; border-color: rgba(239, 68, 68, 0.25); }
.badge-orange { background: rgba(249, 115, 22, 0.12); color: #c2410c; border-color: rgba(249, 115, 22, 0.25); }

.mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace; }
.muted { color: #94a3b8; font-size: 0.82rem; font-weight: 700; }

.pager { display: flex; justify-content: center; align-items: center; gap: 14px; padding-top: 12px; }
.pager-info { color: #64748b; font-weight: 800; }

.batch-bar {
  background: #0f172a;
  color: white;
  border-radius: 12px;
  padding: 12px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.20);
}

.batch-left { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.batch-right { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

.batch-chip {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  padding: 6px 10px;
  font-weight: 900;
  font-size: 0.82rem;
}

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
  width: min(920px, 96vw);
  background: #fff;
  border-radius: 16px;
  border: 1px solid #eef2f7;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.20);
  overflow: hidden;
}

.modal-header {
  padding: 14px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  border-bottom: 1px solid #eef2f7;
}

.modal-title { font-weight: 900; color: #0f172a; }
.modal-body { padding: 14px; }

.kv {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 10px 14px;
  align-items: center;
}

.k { color: #64748b; font-weight: 900; font-size: 0.9rem; }
.v { color: #0f172a; font-weight: 700; }
</style>
