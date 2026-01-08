<template>
  <div class="logs-page">
    <h2 style="margin: 0 0 6px 0;">日志与审计</h2>

    <!-- 顶部筛选 -->
    <div class="toolbar-card">
      <div class="toolbar-left">
        <div class="field">
          <label>角色</label>
          <select v-model="query.roleType" class="select">
            <option value="">全部</option>
            <option value="admin">admin</option>
            <option value="doctor">doctor</option>
            <option value="user">user</option>
          </select>
        </div>

        <div class="field" style="min-width: 260px;">
          <label>关键字</label>
          <input v-model="query.keyword" class="input" placeholder="操作内容" />
        </div>
      </div>

      <div class="toolbar-right">
        <button class="btn btn-primary" @click="loadLogs">查询</button>
        <button class="btn btn-ghost" @click="reset">重置</button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <div class="section-header">
        <div>
          <h3 style="margin: 0;">日志列表</h3>
          <!-- <div class="hint">共 {{ pageData.total }} 条</div> -->
        </div>
      </div>

      <table class="admin-table">
        <thead>
        <tr>
          <th style="width: 220px;">时间</th>
          <th style="width: 120px;">角色</th>
          <th style="width: 140px;">操作者</th>
          <th>操作内容</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="row in pageData.records" :key="row.logId">
          <td>{{ formatTime(row.actionTime) }}</td>
          <td>{{ row.roleType }}</td>
          <td>{{ row.operatorUserId }}</td>
          <td>{{ row.action }}</td>
        </tr>

        <tr v-if="pageData.records.length === 0">
          <td colspan="4">
            <div class="empty-state">
              <div class="empty-title">暂无日志</div>
              <div class="empty-sub">请调整筛选条件后查询</div>
            </div>
          </td>
        </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pager">
        <button class="btn btn-ghost" @click="prevPage" :disabled="query.page <= 1">上一页</button>
        <div class="pager-info">第 {{ query.page }} / {{ totalPages }} 页</div>
        <button class="btn btn-ghost" @click="nextPage" :disabled="query.page >= totalPages">下一页</button>

        <div style="flex: 1;"></div>

        <div class="pager-info">每页</div>
        <select class="select" style="width: 90px;" v-model.number="query.size" @change="onSizeChange">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        <div class="pager-info">条</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchAuditLogs } from '../api/adminAuditLog'

const query = reactive({
  page: 1,
  size: 10,
  roleType: '',
  keyword: ''
})

const pageData = ref({
  records: [],
  total: 0
})

const totalPages = computed(() => Math.max(1, Math.ceil(pageData.value.total / query.size)))

function formatTime(t) {
  if (!t) return ''
  // 兼容 LocalDateTime 的序列化：2025-12-24T18:20:00
  return String(t).replace('T', ' ')
}

async function loadLogs() {
  const res = await fetchAuditLogs(query)
  pageData.value = res.data
}

function reset() {
  query.roleType = ''
  query.keyword = ''
  query.page = 1
  query.size = 10
  loadLogs()
}

function prevPage() {
  if (query.page > 1) {
    query.page--
    loadLogs()
  }
}

function nextPage() {
  if (query.page < totalPages.value) {
    query.page++
    loadLogs()
  }
}

function onSizeChange() {
  query.page = 1
  loadLogs()
}

onMounted(loadLogs)
</script>

<style scoped>
.logs-page { display: flex; flex-direction: column; gap: 20px; }

.toolbar-card,
.table-card {
  background: white;
  border-radius: 12px;
  padding: 18px 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.toolbar-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
}

.toolbar-left { display: flex; flex-wrap: wrap; gap: 12px 16px; }
.toolbar-right { display: flex; gap: 10px; }

.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 12px; color: #666; font-weight: 700; }

.input {
  height: 36px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 12px;
  outline: none;
}
.input:focus { border-color: #93c5fd; box-shadow: 0 0 0 3px rgba(59,130,246,.15); }

.select {
  height: 36px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 10px;
  outline: none;
  background: #fff;
}
.select:focus { border-color: #93c5fd; box-shadow: 0 0 0 3px rgba(59,130,246,.15); }

.btn {
  height: 36px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 700;
  font-size: 0.9rem;
}
.btn-primary { background: #3b82f6; color: white; }
.btn-primary:hover { background: #2563eb; }
.btn-ghost { background: #f3f4f6; color: #111827; }
.btn-ghost:hover { background: #e5e7eb; }

.section-header { display:flex; justify-content:space-between; align-items:flex-end; margin-bottom: 10px; }
.hint { color:#6b7280; font-size: 12px; margin-top: 6px; }

.admin-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
}
.admin-table th,
.admin-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
  vertical-align: top;
}
.admin-table thead th { background: #f9fafb; font-weight: 800; }
.admin-table tbody tr:hover { background: #f9fafb; }

.empty-state { text-align:center; padding: 22px 0; color:#6b7280; }
.empty-title { font-weight: 800; color:#111827; margin-bottom: 6px; }
.empty-sub { font-size: 12px; }

.pager { display:flex; align-items:center; gap: 10px; margin-top: 12px; }
.pager-info { color:#6b7280; font-size: 12px; }
</style>
