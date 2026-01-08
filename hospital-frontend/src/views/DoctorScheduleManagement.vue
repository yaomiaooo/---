<template>
  <div class="page">
    <!-- 筛选区 -->
    <div class="card filter-card">
      <div class="filter-row">
        <div class="field">
          <label>选择周</label>
          <input type="date" v-model="filters.weekStartDate" @change="handleWeekChange" />
        </div>
        <div class="field">
          <label>时间段</label>
          <select v-model="filters.timeSlot">
            <option value="">全部</option>
            <option v-for="s in TIME_SLOTS" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>
        <div class="field actions">
          <button class="btn primary" @click="loadSchedules" :disabled="loading">
            {{ loading ? '加载中...' : '查询' }}
          </button>
          <button class="btn" @click="resetFilters" :disabled="loading">重置</button>
        </div>
      </div>
    </div>

    <!-- 概览区 -->
    <div class="stats">
      <div class="stat">
        <div class="k">排班条目</div>
        <div class="v">{{ schedules.length }}</div>
      </div>
      <div class="stat">
        <div class="k">总号源</div>
        <div class="v">{{ sumTotal }}</div>
      </div>
      <div class="stat">
        <div class="k">已预约</div>
        <div class="v">{{ sumBooked }}</div>
      </div>
      <div class="stat">
        <div class="k">预约率</div>
        <div class="v">{{ avgUsage }}%</div>
      </div>
    </div>

    <!-- 表格区（日历形式） -->
    <div class="card table-card">
      <div class="table-head">
        <h3>我的排班 <small>My Schedule</small></h3>
        <div class="right">
          <span v-if="error" class="error">{{ error }}</span>
        </div>
      </div>
      <div class="table-wrapper">
        <!-- 日历表格：行=时间段，列=一周7天 -->
        <table class="schedule-calendar">
          <thead>
            <tr>
              <th>时间段</th>
              <th v-for="(day, idx) in weekDays" :key="idx">
                {{ day.week }}<br>{{ day.date }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="slot in filteredTimeSlots" :key="slot">
              <td class="time-slot">{{ slot }}</td>
              <td v-for="(day, idx) in weekDays" :key="idx">
                <!-- 匹配当前日期+时间段的排班 -->
                <div v-if="getSchedule(day.date, slot)" class="schedule-cell">
                  <button class="btn visit-btn" :disabled="getSchedule(day.date, slot).booked_count >= getSchedule(day.date, slot).total_quota">
                    坐诊
                  </button>
                  <!-- 悬浮显示号源信息 -->
                  <div class="tooltip">
                    总号源: {{ getSchedule(day.date, slot).total_quota }}<br>
                    已预约: {{ getSchedule(day.date, slot).booked_count }}<br>
                    剩余: {{ remain(getSchedule(day.date, slot)) }}
                  </div>
                </div>
                <div v-else class="empty-cell"></div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="foot">
        <span class="tip">说明：预约率 = booked_count / total_quota。按钮置灰表示号源已满。</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
// 导入API函数（使用之前定义的排班查询接口）
import { getDoctorSchedules } from '../api/schedule.js'

/** 时间段（对应原上午/下午） */
const TIME_SLOTS = ['08:00-10:00', '13:30-17:00', '19:00-21:00']
const loading = ref(false)
const error = ref('')
const schedules = ref([])
const weekDays = ref([]) // 本周7天的日期+星期信息

// 筛选条件：周起始日期、时间段
const filters = ref({
  weekStartDate: '',
  timeSlot: ''
})

/** 格式化日期（yyyy-MM-dd） */
function pad(n) {
  return String(n).padStart(2, '0')
}
function formatDate(d) {
  const yyyy = d.getFullYear()
  const mm = pad(d.getMonth() + 1)
  const dd = pad(d.getDate())
  return `${yyyy}-${mm}-${dd}`
}

/** 获取日期对应的星期（中文） */
function getWeekName(dateStr) {
  const weekList = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const d = new Date(dateStr)
  return weekList[d.getDay()]
}

/** 初始化本周起始日期（默认当前周周一） */
function initDefaultWeek() {
  const now = new Date()
  // 获取本周一的日期
  const weekStart = new Date(now)
  const day = weekStart.getDay() || 7
  weekStart.setDate(weekStart.getDate() - (day - 1))
  filters.value.weekStartDate = formatDate(weekStart)
  // 生成本周7天的日期+星期
  generateWeekDays(weekStart)
}

/** 生成本周7天的日期+星期信息 */
function generateWeekDays(weekStart) {
  const days = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(weekStart)
    d.setDate(d.getDate() + i)
    days.push({
      date: formatDate(d),
      week: getWeekName(formatDate(d))
    })
  }
  weekDays.value = days
}

/** 选择周日期后，更新本周7天 */
function handleWeekChange() {
  const weekStart = new Date(filters.value.weekStartDate)
  generateWeekDays(weekStart)
}

/** 加载排班数据（调用API接口） */
async function loadSchedules() {
  loading.value = true
  error.value = ''
  try {
    // 计算周结束日期（周日）
    const weekEnd = new Date(filters.value.weekStartDate)
    weekEnd.setDate(weekEnd.getDate() + 6)
    
    // 调用API接口（使用导入的getDoctorSchedules）
    const params = {
      startDate: filters.value.weekStartDate,
      endDate: formatDate(weekEnd),
      timeSlot: filters.value.timeSlot || undefined
    }
    const resp = await getDoctorSchedules(params)
    
    // 处理接口响应数据
    const data = Array.isArray(resp) ? resp : resp?.data || resp?.result || []
    schedules.value = normalize(data)
  } catch (e) {
    error.value = '接口不可用，已展示示例数据'
    schedules.value = normalize(mockData())
  } finally {
    loading.value = false
  }
}

/** 标准化排班数据格式 */
function normalize(list) {
  return (list || []).map(x => ({
    schedule_id: x.schedule_id ?? x.id ?? `${x.work_date || ''}-${x.time_slot || ''}`,
    work_date: (x.work_date || x.workDate || '').slice(0, 10),
    time_slot: x.time_slot || x.timeSlot || '',
    total_quota: Number(x.total_quota ?? x.totalQuota ?? 0),
    booked_count: Number(x.booked_count ?? x.bookedCount ?? 0),
  })).sort((a, b) => (a.work_date + a.time_slot).localeCompare(b.work_date + b.time_slot))
}

/** 模拟数据（按当前周生成） */
function mockData() {
  const mockList = []
  weekDays.value.forEach(day => {
    // 周三、周五上午，周三、周日下午有排班
    if (day.week === '周三' || day.week === '周五') {
      mockList.push({
        work_date: day.date,
        time_slot: '08:00-10:00',
        total_quota: 50,
        booked_count: 20
      })
    }
    if (day.week === '周三' || day.week === '周日') {
      mockList.push({
        work_date: day.date,
        time_slot: '13:30-17:00',
        total_quota: 50,
        booked_count: 10
      })
    }
  })
  return mockList
}

/** 重置筛选条件 */
function resetFilters() {
  initDefaultWeek()
  filters.value.timeSlot = ''
  loadSchedules()
}

/** 获取指定日期+时间段的排班 */
function getSchedule(date, slot) {
  return schedules.value.find(item => item.work_date === date && item.time_slot === slot)
}

/** 计算剩余号源 */
function remain(row) {
  return Math.max(0, (row.total_quota || 0) - (row.booked_count || 0))
}

/** 过滤后的时间段（根据筛选条件） */
const filteredTimeSlots = computed(() => {
  if (!filters.value.timeSlot) return TIME_SLOTS
  return [filters.value.timeSlot]
})

// 概览统计
const sumTotal = computed(() => schedules.value.reduce((acc, x) => acc + (x.total_quota || 0), 0))
const sumBooked = computed(() => schedules.value.reduce((acc, x) => acc + (x.booked_count || 0), 0))
const avgUsage = computed(() => {
  if (schedules.value.length === 0) return 0
  const total = schedules.value.reduce((acc, x) => {
    const t = x.total_quota || 0
    return acc + (t > 0 ? Math.round((x.booked_count / t) * 100) : 0)
  }, 0)
  return Math.round(total / schedules.value.length)
})

// 初始化
onMounted(() => {
  initDefaultWeek()
  loadSchedules()
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  padding: 16px 18px;
}
.filter-card .filter-row {
  display: grid;
  grid-template-columns: 200px 220px 1fr;
  gap: 14px;
  align-items: end;
}
.field label {
  display: block;
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 6px;
}
.field input, .field select {
  width: 85%;
  padding: 10px 10px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  outline: none;
}
.field input:focus, .field select:focus {
  border-color: #004ea2;
  box-shadow: 0 0 0 3px rgba(0, 78, 162, 0.10);
}
.actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
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
.btn.primary:hover {
  background: #003d80;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.stat {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  padding: 14px 16px;
}
.stat .k {
  font-size: 0.85rem;
  color: #999;
}
.stat .v {
  margin-top: 6px;
  font-size: 1.4rem;
  font-weight: 800;
  color: #004ea2;
}
.table-card .table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.table-head h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #333;
}
.table-head small {
  color: #999;
  font-weight: normal;
  margin-left: 8px;
}
.error {
  color: #ff4d4f;
  font-size: 0.85rem;
}
.table-wrapper {
  overflow: auto;
}
/* 日历表格样式 */
.schedule-calendar {
  width: 100%;
  border-collapse: collapse;
  text-align: center;
}
.schedule-calendar th, .schedule-calendar td {
  border: 1px solid #eee;
  padding: 10px;
  width: calc(100% / 8); /* 1列时间段 + 7列日期 */
}
.schedule-calendar th {
  background: #f7f9fc;
  font-weight: 600;
  color: #333;
}
.time-slot {
  background: #f7f9fc;
  font-weight: 900;
  color: #000000;
}
.schedule-cell {
  position: relative;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.visit-btn {
  background: #5acf83;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 700;
  font-size: 1.2rem;
  cursor: pointer;
  width: 100%;
  height: 100%;
}
.visit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
/* 悬浮提示 */
.tooltip {
  display: none;
  position: absolute;
  top: 50%;
  left: 100%;
  transform: translateY(-50%);
  margin-left: 8px;
  background: #333;
  color: #fff;
  padding: 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  white-space: nowrap;
  z-index: 10;
}
.schedule-cell:hover .tooltip {
  display: block;
}
.empty-cell {
  height: 80px;
  background: rgb(255, 255, 255);
}
.foot {
  margin-top: 10px;
  color: #999;
  font-size: 0.82rem;
}
</style>

