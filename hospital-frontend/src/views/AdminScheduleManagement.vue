<template>
  <div class="schedule-page">
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
          <label>科室</label>
          <select
              v-model="filters.departmentId"
              class="select"
              :disabled="depLoading"
              @change="onFilterDepartmentChange"
          >
            <option value="">全部科室</option>
            <option v-for="d in filterDepartments" :key="d.departmentId" :value="d.departmentId">
              {{ d.departmentName }}（{{ d.departmentId }}）
            </option>
          </select>
        </div>

        <div class="field">
          <label>医生</label>
          <select
              v-model="filters.doctorId"
              class="select"
              :disabled="!filters.hospitalId || doctorFilterLoading"
              @change="loadSchedules(1)"
          >
            <option value="">全部医生</option>
            <option v-for="doc in filterDoctors" :key="doc.doctorId" :value="doc.doctorId">
              {{ doc.doctorName }}（{{ doc.doctorId }}）
            </option>
          </select>
        </div>

        <div class="field">
          <label>日期（起）</label>
          <input v-model="filters.dateFrom" class="input" type="date" @change="loadSchedules(1)" />
        </div>

        <div class="field">
          <label>日期（止）</label>
          <input v-model="filters.dateTo" class="input" type="date" @change="loadSchedules(1)" />
        </div>

        <!-- <div class="field">
          <label>状态</label>
          <select v-model="filters.status" class="select" @change="loadSchedules(1)">
            <option value="">全部</option>
            <option value="OPEN">开放</option>
            <option value="CLOSED">关闭</option>
            <option value="FULL">已满</option>
          </select>
        </div> -->

        <div class="actions">
          <button class="btn btn-primary btn-mini" @click="loadSchedules(1)">
            <Icon icon="mdi:magnify" /> 查询
          </button>
          <button class="btn btn-ghost btn-mini" @click="resetFilters">
            <Icon icon="mdi:refresh" /> 重置
          </button>
        </div>
      </div>

      <div class="toolbar-right">
        <button class="btn btn-primary" @click="openCreate">
          <Icon icon="mdi:calendar-plus" />
          新增排班
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

        <button class="btn btn-danger btn-mini" :disabled="batchLoading" @click="batchClose">
          <Icon v-if="batchLoading" icon="mdi:loading" class="spin" />
          <Icon v-else icon="mdi:calendar-remove" />
          批量关闭
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
          <div class="section-title">排班列表</div>
          <div class="section-sub">
            支持按院区/科室/医生与日期范围筛选；支持批量关闭与删除。
          </div>
        </div>

        <div class="table-right">
          <div class="range mono">
            <span v-if="loading">加载中…</span>
            <!-- <span v-else>共 <span class="v mono">{{ pager.total }}</span> 条</span> -->
          </div>

          <div class="field small">
            <label>每页</label>
            <select class="select small" v-model.number="pager.size" @change="loadSchedules(1)">
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

            <th style="width: 160px;">排班ID</th>
            <th style="width: 90px;">院区</th>
            <th style="width: 120px;">科室</th>
            <th style="width: 170px;">医生</th>
            <th style="width: 120px;">日期</th>
            <th style="width: 110px;">班次</th>
            <th style="width: 110px;">号源</th>
            <th style="width: 120px;">剩余</th>
            <th style="width: 120px;">状态</th>
            <th style="width: 190px;">创建时间</th>
            <th style="width: 260px;">操作</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="loading">
            <td colspan="12" class="empty">加载中...</td>
          </tr>

          <tr v-else-if="items.length === 0">
            <td colspan="12" class="empty">暂无数据</td>
          </tr>

          <tr v-else v-for="row in items" :key="row.scheduleId">
            <td>
              <input
                  type="checkbox"
                  :checked="isSelected(row.scheduleId)"
                  @change="toggleRow(row.scheduleId)"
              />
            </td>

            <td class="mono">{{ row.scheduleId }}</td>
            <td class="mono">{{ row.hospitalId }}</td>
            <td class="mono">
              {{ row.departmentName ? row.departmentName + '（' + row.departmentId + '）' : row.departmentId }}
            </td>
            <td class="mono">
              {{ row.doctorName ? row.doctorName + '（' + row.doctorId + '）' : row.doctorId }}
            </td>
            <td class="mono">{{ row.workDate }}</td>
            <td>{{ slotLabel(row.timeSlot) }}</td>
            <td class="mono">{{ safeNum(row.capacity) }}</td>
            <td class="mono">{{ safeNum(row.remaining) }}</td>
            <td>
                <span class="badge" :class="statusBadgeClass(row.status)">
                  {{ statusLabel(row.status) }}
                </span>
            </td>
            <td class="mono">{{ formatTime(row.createTime) }}</td>

            <td>
              <div class="actions">
                <button class="btn btn-mini" @click="openEdit(row)">
                  <Icon icon="mdi:pencil-outline" /> 编辑
                </button>

                <button
                    class="btn btn-danger btn-mini"
                    :disabled="row.status === 'CLOSED'"
                    @click="confirmClose(row)"
                >
                  <Icon icon="mdi:calendar-remove" /> 关闭
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
          <button class="btn btn-ghost btn-mini" :disabled="pager.page <= 1 || loading" @click="loadSchedules(pager.page - 1)">
            上一页
          </button>
          <button class="btn btn-ghost btn-mini" :disabled="pager.page >= totalPages || loading" @click="loadSchedules(pager.page + 1)">
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 弹窗：新增/编辑（对齐：预约订单管理弹窗风格） -->
    <div v-if="modal.visible" class="modal-mask" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">{{ modal.mode === 'create' ? '新增排班' : '编辑排班' }}</div>
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
              <label>院区ID（hospitalId）</label>
              <input
                  v-model="form.hospitalId"
                  class="input"
                  :disabled="modal.mode === 'edit'"
                  placeholder="如：1 / 2"
              />
            </div>

            <div class="field">
              <label>科室（departmentId）</label>
              <select
                  v-model="form.departmentId"
                  class="select"
                  :disabled="modal.mode === 'edit' || !form.hospitalId || modalDepLoading"
                  @change="onModalDepartmentChange"
              >
                <option value="">请选择</option>
                <option v-for="d in modalDepartments" :key="d.departmentId" :value="d.departmentId">
                  {{ d.departmentName }}（{{ d.departmentId }}）
                </option>
              </select>
            </div>

            <div class="field">
              <label>医生（doctorId）</label>
              <select
                  v-model="form.doctorId"
                  class="select"
                  :disabled="modal.mode === 'edit' || !form.hospitalId || modalDoctorLoading"
              >
                <option value="">请选择</option>
                <option v-for="doc in modalDoctors" :key="doc.doctorId" :value="doc.doctorId">
                  {{ doc.doctorName }}（{{ doc.doctorId }}）
                </option>
              </select>
            </div>

            <div class="field">
              <label>日期（workDate）</label>
              <input v-model="form.workDate" class="input" type="date" />
            </div>

            <div class="field">
              <label>班次（timeSlot）</label>
              <select v-model="form.timeSlot" class="select">
                <option value="">请选择</option>
                <option value="08:00-10:00">上午 (08:00-10:00)</option>
                <option value="13:30-17:00">下午 (13:30-17:00)</option>
                <option value="19:00-21:00">晚间 (19:00-21:00)</option>
              </select>
            </div>

            <div class="field">
              <label>号源总量（capacity）</label>
              <input v-model="form.capacity" class="input" type="number" min="0" placeholder="如：30" />
            </div>

            <div class="field">
              <label>剩余号源（remaining）</label>
              <input
                  v-model="form.remaining"
                  class="input"
                  type="number"
                  min="0"
                  placeholder="默认等于 capacity"
              />
            </div>

            <div class="field">
              <label>状态（status）</label>
              <select v-model="form.status" class="select">
                <option value="OPEN">开放</option>
                <option value="CLOSED">关闭</option>
                <option value="FULL">已满</option>
              </select>
            </div>

            <div v-if="modal.mode === 'edit'" class="field full">
              <label>排班ID（scheduleId）</label>
              <input :value="form.scheduleId" class="input" disabled />
            </div>

            <div class="field full">
              <label>备注（remark，可选）</label>
              <textarea v-model="form.remark" class="textarea" rows="3" placeholder="可选"></textarea>
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

    <!-- 二次确认：关闭排班 -->
    <div v-if="closeDlg.visible" class="modal-mask" @click.self="closeCloseDlg">
      <div class="modal modal-sm">
        <div class="modal-header">
          <div class="modal-title">确认关闭</div>
          <button class="btn btn-ghost btn-mini" @click="closeCloseDlg">
            <Icon icon="mdi:close" /> 关闭
          </button>
        </div>

        <div class="modal-body">
          <div class="kv">
            <div class="k">排班ID</div>
            <div class="v mono">{{ closeDlg.row?.scheduleId }}</div>
            <div class="k">日期</div>
            <div class="v mono">{{ closeDlg.row?.workDate }}</div>
            <div class="k">班次</div>
            <div class="v">{{ slotLabel(closeDlg.row?.timeSlot) }}</div>
          </div>

          <div class="modal-actions">
            <button class="btn btn-ghost" @click="closeCloseDlg">取消</button>
            <button class="btn btn-danger" :disabled="closeDlg.saving" @click="doClose">
              <Icon v-if="closeDlg.saving" icon="mdi:loading" class="spin" />
              关闭排班
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 二次确认：删除排班 -->
    <div v-if="delDlg.visible" class="modal-mask" @click.self="closeDeleteDlg">
      <div class="modal modal-sm">
        <div class="modal-header">
          <div class="modal-title">确认删除</div>
          <button class="btn btn-ghost btn-mini" @click="closeDeleteDlg">
            <Icon icon="mdi:close" /> 关闭
          </button>
        </div>

        <div class="modal-body">
          <div class="kv">
            <div class="k">排班ID</div>
            <div class="v mono">{{ delDlg.row?.scheduleId }}</div>
            <div class="k">日期</div>
            <div class="v mono">{{ delDlg.row?.workDate }}</div>
            <div class="k">班次</div>
            <div class="v">{{ slotLabel(delDlg.row?.timeSlot) }}</div>
          </div>

          <div class="modal-actions">
            <button class="btn btn-ghost" @click="closeDeleteDlg">取消</button>
            <button class="btn btn-danger" :disabled="delDlg.saving" @click="doDelete">
              <Icon v-if="delDlg.saving" icon="mdi:loading" class="spin" />
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

/**
 * 说明：
 * 1) 接口文件名与方法名按常见写法组织；如你项目里名称不同，保持“请求参数/返回结构”不变，替换 import 即可。
 * 2) 返回分页结构兼容：res.data 或 res；records/list；total/totalCount。
 */
import {
  pageSchedules,
  createSchedule,
  updateSchedule,
  deleteSchedule,
  closeSchedule,
  listDepartmentsByHospital,
  listDoctorsByHospitalDepartment,
  listHospitals
} from '../api/adminSchedule'
import { pageDepartments } from '../api/adminDepartment'

/* =================== 筛选 =================== */
const filters = reactive({
  hospitalId: '',
  departmentId: '',
  doctorId: '',
  dateFrom: '',
  dateTo: '',
  status: ''
})

async function resetFilters() {
  filters.hospitalId = ''
  filters.departmentId = ''
  filters.doctorId = ''
  filters.dateFrom = ''
  filters.dateTo = ''
  filters.status = ''
  // 重置时加载所有科室
  await loadFilterDepartments('')
  filterDoctors.value = []
  clearSelection()
  loadSchedules(1)
}

/* =================== 列表/分页 =================== */
const items = ref([])
const loading = ref(false)
const error = ref('')

const pager = reactive({
  page: 1,
  size: 10,
  total: 0
})

const totalPages = computed(() => {
  const t = Math.ceil((pager.total || 0) / (pager.size || 10))
  return Math.max(t, 1)
})

function unwrapPage(res) {
  const body = res ?? {}
  const pageObj = body.data ?? body
  
  // 如果 pageObj 是数组，说明返回的是数组格式（兼容处理）
  if (Array.isArray(pageObj)) {
    return { records: pageObj, total: pageObj.length }
  }
  
  // 否则是分页格式
  const records = pageObj.records ?? pageObj.list ?? []
  const total = Number(pageObj.total ?? pageObj.totalCount ?? 0)
  return { records, total }
}

function mapRow(vo) {
  // 后端返回的字段：totalQuota, bookedCount, remainingQuota
  const totalQuota = vo.totalQuota ?? vo.total_quota ?? vo.capacity ?? vo.total ?? vo.quota ?? 0
  const bookedCount = vo.bookedCount ?? vo.booked_count ?? 0
  const remainingQuota = vo.remainingQuota ?? vo.remaining_quota ?? vo.remaining ?? vo.left ?? 0
  
  // 计算状态：根据剩余号源判断
  let status = 'OPEN'
  if (totalQuota <= bookedCount) {
    status = 'FULL'  // 已满
  } else if (remainingQuota === 0) {
    status = 'FULL'  // 已满
  }
  
  return {
    scheduleId: vo.scheduleId ?? vo.schedule_id ?? vo.id ?? '',
    hospitalId: vo.hospitalId ?? vo.hospital_id ?? '',
    departmentId: vo.departmentId ?? vo.department_id ?? '',
    departmentName: vo.departmentName ?? vo.department_name ?? '',
    doctorId: vo.doctorId ?? vo.doctor_id ?? '',
    doctorName: vo.doctorName ?? vo.doctor_name ?? '',
    workDate: vo.workDate ?? vo.work_date ?? vo.date ?? '',
    timeSlot: vo.timeSlot ?? vo.time_slot ?? vo.slot ?? '',
    capacity: totalQuota,
    bookedCount: bookedCount,  // 添加 bookedCount 字段
    remaining: remainingQuota,
    status: status,
    createTime: vo.createTime ?? vo.createdAt ?? vo.created_at ?? '',
    remark: vo.remark ?? ''
  }
}

async function loadSchedules(page = 1) {
  pager.page = page
  loading.value = true
  error.value = ''
  try {
    const hospitalId = (filters.hospitalId || '').trim()
    
    // 后端要求必须有 hospitalId，如果没有选择院区，需要查询所有院区
    if (!hospitalId) {
      // 获取所有院区列表
      const hospitalsRes = await listHospitals()
      const hospitals = hospitalsRes?.data ?? hospitalsRes ?? []
      
      if (!Array.isArray(hospitals) || hospitals.length === 0) {
        items.value = []
        pager.total = 0
        error.value = '请先选择院区，或确保系统中有院区数据'
        return
      }
      
      // 分别查询每个院区的数据
      // 注意：由于后端要求必须有 hospitalId，我们需要分别查询每个院区
      // 这里使用较大的 size 来获取更多数据，如果数据量很大可能需要优化
      const allRecords = []
      let allTotal = 0
      
      for (const hospital of hospitals) {
        const hid = hospital.hospitalId ?? hospital.hospital_id ?? hospital.id
        if (!hid) continue
        
        const params = {
          page: 1,  // 查询所有院区时，每个院区都查第一页
          size: 10000,  // 使用较大的 size 来获取更多数据（如果数据量超过此值，可能需要分页查询）
          hospitalId: String(hid),
          departmentId: (filters.departmentId || '').trim(),
          doctorId: (filters.doctorId || '').trim(),
          workDateFrom: filters.dateFrom || '',
          workDateTo: filters.dateTo || '',
        }
        Object.keys(params).forEach((k) => {
          if (params[k] === '' || params[k] == null) delete params[k]
        })
        
        try {
          const res = await pageSchedules(params)
          const { records, total } = unwrapPage(res)
          if (Array.isArray(records)) {
            allRecords.push(...records)
            allTotal += total
          }
        } catch (e) {
          console.error(`查询院区 ${hid} 的排班失败:`, e)
          // 继续查询其他院区，不中断
        }
      }
      
      // 按日期和班次排序
      allRecords.sort((a, b) => {
        const dateA = a.workDate ?? a.work_date ?? ''
        const dateB = b.workDate ?? b.work_date ?? ''
        if (dateA !== dateB) return dateB.localeCompare(dateA) // 日期降序
        const slotA = a.timeSlot ?? a.time_slot ?? ''
        const slotB = b.timeSlot ?? b.time_slot ?? ''
        return slotA.localeCompare(slotB) // 班次升序
      })
      
      // 手动分页
      const start = (pager.page - 1) * pager.size
      const end = start + pager.size
      const pagedRecords = allRecords.slice(start, end)
      
      items.value = pagedRecords.map(mapRow)
      pager.total = allTotal
      
      if (allTotal === 0) {
        error.value = ''
      }
      return
    }
    
    // 有选择院区时，正常查询
    const params = {
      page: pager.page,
      size: pager.size,
      hospitalId: hospitalId,
      departmentId: (filters.departmentId || '').trim(),
      doctorId: (filters.doctorId || '').trim(),
      workDateFrom: filters.dateFrom || '',
      workDateTo: filters.dateTo || '',
    }
    Object.keys(params).forEach((k) => {
      if (params[k] === '' || params[k] == null) delete params[k]
    })

    const res = await pageSchedules(params)
    const { records, total } = unwrapPage(res)
    items.value = (records || []).map(mapRow)
    pager.total = total
    
    // 如果没有数据，清除错误提示
    if (total === 0) {
      error.value = ''
    }

    // 翻页后，如果整页未选中，保持 selectedSet 不动（对齐订单管理体验）
  } catch (e) {
    items.value = []
    pager.total = 0
    error.value = e?.message || '加载失败，请检查网络连接或稍后重试'
    console.error('加载排班数据失败:', e)
  } finally {
    loading.value = false
  }
}

/* =================== 科室/医生下拉（筛选区） =================== */
const depLoading = ref(false)
const doctorFilterLoading = ref(false)

const filterDepartments = ref([]) // {departmentId, departmentName}
const filterDoctors = ref([]) // {doctorId, doctorName}

function mapDepartmentItem(x) {
  return {
    departmentId: x.departmentId ?? x.department_id ?? x.id ?? '',
    departmentName: x.departmentName ?? x.department_name ?? x.name ?? ''
  }
}
function mapDoctorItem(x) {
  return {
    doctorId: x.doctorId ?? x.doctor_id ?? x.id ?? '',
    doctorName: x.doctorName ?? x.doctor_name ?? x.name ?? ''
  }
}

/**
 * 加载科室列表（用于筛选下拉）
 * 当hospitalId为空时，加载所有科室（传入空字符串，获取所有院区的所有科室）
 * 当hospitalId有值时，加载指定院区的科室
 */
async function loadFilterDepartments(hospitalId) {
  const hid = (hospitalId || '').trim()
  
  depLoading.value = true
  filterDepartments.value = []
  
  try {
    let res
    if (!hid) {
      // 加载所有科室（不传hospitalId参数，使用pageDepartments获取所有科室）
      res = await pageDepartments({ page: 1, size: 1000, hospitalId: '' })
    } else {
      // 加载指定院区的科室
      res = await listDepartmentsByHospital(hid)
    }
    
    // 处理返回数据
    // 统一处理：支持分页格式和数组格式
    let dataArray = []
    if (res?.code === 200 || res?.code === undefined) {
      const data = res?.data ?? res
      if (Array.isArray(data)) {
        // 直接是数组格式
        dataArray = data
      } else if (data && typeof data === 'object') {
        // 分页格式：从 records 或 list 中提取
        dataArray = data.records || data.list || []
      }
    } else {
      // 其他情况，尝试直接使用 res.data 或 res
      const data = res?.data ?? res ?? []
      dataArray = Array.isArray(data) ? data : []
    }
    filterDepartments.value = dataArray.map(mapDepartmentItem)
  } catch (e) {
    console.error('加载科室列表失败:', e)
    filterDepartments.value = []
  } finally {
    depLoading.value = false
  }
}

async function loadFilterDoctors(hospitalId, departmentId) {
  const hid = (hospitalId || '').trim()
  const did = (departmentId || '').trim()
  if (!hid) {
    filterDoctors.value = []
    return
  }
  doctorFilterLoading.value = true
  try {
    const res = await listDoctorsByHospitalDepartment({ hospitalId: hid, departmentId: did || '' })
    const data = res?.data ?? res ?? []
    filterDoctors.value = Array.isArray(data) ? data.map(mapDoctorItem) : []
  } catch (e) {
    filterDoctors.value = []
  } finally {
    doctorFilterLoading.value = false
  }
}

/**
 * 院区选择变化时的处理函数
 */
async function onHospitalChange() {
  // 重置科室和医生选择
  filters.departmentId = ''
  filters.doctorId = ''
  filterDoctors.value = []
  // 加载对应的科室列表
  await loadFilterDepartments(filters.hospitalId)
  // 加载对应的医生列表
  await loadFilterDoctors(filters.hospitalId, '')
  // 重新加载排班列表
  loadSchedules(1)
}

function onFilterDepartmentChange() {
  filters.doctorId = ''
  loadFilterDoctors(filters.hospitalId, filters.departmentId)
  loadSchedules(1)
}

/* =================== 批量选择（对齐订单管理） =================== */
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

const idsOnPage = computed(() => (items.value || []).map(r => r.scheduleId).filter(Boolean))

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

async function batchClose() {
  const ids = selectedIds.value
  if (ids.length === 0) return
  if (!confirm(`确定关闭所选 ${ids.length} 条排班吗？`)) return

  batchLoading.value = true
  try {
    const fails = []
    for (const id of ids) {
      try {
        await closeSchedule(id)
      } catch (e) {
        fails.push(id)
      }
    }
    await loadSchedules(pager.page)
    if (fails.length > 0) {
      error.value = `部分关闭失败：${fails.slice(0, 3).join(', ')}${fails.length > 3 ? '...' : ''}`
    }
  } finally {
    batchLoading.value = false
  }
}

async function batchDelete() {
  const ids = selectedIds.value
  if (ids.length === 0) return
  if (!confirm(`确定删除所选 ${ids.length} 条排班吗？此操作不可恢复。`)) return

  batchLoading.value = true
  try {
    const fails = []
    for (const id of ids) {
      try {
        await deleteSchedule(id)
        selectedSet.value.delete(id)
      } catch (e) {
        fails.push(id)
      }
    }
    await loadSchedules(1)
    if (fails.length > 0) {
      error.value = `部分删除失败：${fails.slice(0, 3).join(', ')}${fails.length > 3 ? '...' : ''}`
    }
  } finally {
    batchLoading.value = false
  }
}

/* =================== 新增/编辑弹窗（对齐订单管理） =================== */
const modal = reactive({
  visible: false,
  mode: 'create' // create | edit
})
const modalSaving = ref(false)
const modalError = ref('')

const form = reactive({
  scheduleId: '',
  hospitalId: '',
  departmentId: '',
  doctorId: '',
  workDate: '',
  timeSlot: '',
  capacity: '',
  remaining: '',
  status: 'OPEN',
  remark: ''
})

function resetForm() {
  form.scheduleId = ''
  form.hospitalId = ''
  form.departmentId = ''
  form.doctorId = ''
  form.workDate = ''
  form.timeSlot = ''
  form.capacity = ''
  form.remaining = ''
  form.status = 'OPEN'
  form.remark = ''
}

const modalDepLoading = ref(false)
const modalDoctorLoading = ref(false)
const modalDepartments = ref([])
const modalDoctors = ref([])

async function loadModalDepartments(hospitalId) {
  const hid = (hospitalId || '').trim()
  if (!hid) {
    modalDepartments.value = []
    return
  }
  modalDepLoading.value = true
  try {
    const res = await listDepartmentsByHospital(hid)
    // 统一处理：支持分页格式和数组格式
    let dataArray = []
    if (res?.code === 200 || res?.code === undefined) {
      const data = res?.data ?? res
      if (Array.isArray(data)) {
        // 直接是数组格式
        dataArray = data
      } else if (data && typeof data === 'object') {
        // 分页格式：从 records 或 list 中提取
        dataArray = data.records || data.list || []
      }
    } else {
      // 其他情况，尝试直接使用 res.data 或 res
      const data = res?.data ?? res ?? []
      dataArray = Array.isArray(data) ? data : []
    }
    modalDepartments.value = dataArray.map(mapDepartmentItem)
  } catch (e) {
    console.error('加载弹窗科室列表失败:', e)
    modalDepartments.value = []
  } finally {
    modalDepLoading.value = false
  }
}

async function loadModalDoctors(hospitalId, departmentId) {
  const hid = (hospitalId || '').trim()
  const did = (departmentId || '').trim()
  if (!hid) {
    modalDoctors.value = []
    return
  }
  modalDoctorLoading.value = true
  try {
    const res = await listDoctorsByHospitalDepartment({ hospitalId: hid, departmentId: did || '' })
    const data = res?.data ?? res ?? []
    modalDoctors.value = Array.isArray(data) ? data.map(mapDoctorItem) : []
  } catch (e) {
    modalDoctors.value = []
  } finally {
    modalDoctorLoading.value = false
  }
}

function openCreate() {
  resetForm()
  modal.mode = 'create'
  modal.visible = true
  modalError.value = ''
  modalDepartments.value = []
  modalDoctors.value = []
}

function openEdit(row) {
  resetForm()
  modal.mode = 'edit'
  modal.visible = true
  modalError.value = ''

  form.scheduleId = row.scheduleId
  form.hospitalId = row.hospitalId
  form.departmentId = row.departmentId
  form.doctorId = row.doctorId
  form.workDate = row.workDate
  form.timeSlot = row.timeSlot
  form.capacity = String(row.capacity ?? '')
  form.remaining = String(row.remaining ?? '')
  form.status = row.status || 'OPEN'
  form.remark = row.remark || ''

  // 编辑模式下禁改院区/科室/医生（下拉禁用），无需加载列表
  modalDepartments.value = []
  modalDoctors.value = []
}

function closeModal() {
  modal.visible = false
  modalSaving.value = false
  modalError.value = ''
}

function onModalDepartmentChange() {
  form.doctorId = ''
  loadModalDoctors(form.hospitalId, form.departmentId)
}

watch(
    () => form.hospitalId,
    async (nv, ov) => {
      if (!modal.visible) return
      if (modal.mode === 'edit') return
      const a = (nv || '').trim()
      const b = (ov || '').trim()
      if (a === b) return
      form.departmentId = ''
      form.doctorId = ''
      modalDoctors.value = []
      modalError.value = ''
      await loadModalDepartments(a)
      await loadModalDoctors(a, '')
    }
)

function validateModal() {
  const hid = (form.hospitalId || '').trim()
  if (!hid) return 'hospitalId 不能为空'
  if (!(form.departmentId || '').trim()) return 'departmentId 不能为空'
  if (!(form.doctorId || '').trim()) return 'doctorId 不能为空'
  if (!form.workDate) return 'workDate 不能为空'
  if (!(form.timeSlot || '').trim()) return 'timeSlot 不能为空'

  const cap = Number(form.capacity)
  if (!Number.isFinite(cap) || cap < 0) return 'capacity 必须是非负数字'

  // remaining 可选；为空则默认 = capacity
  if (form.remaining !== '' && form.remaining != null) {
    const rem = Number(form.remaining)
    if (!Number.isFinite(rem) || rem < 0) return 'remaining 必须是非负数字'
    if (rem > cap) return 'remaining 不能大于 capacity'
  }
  return ''
}

async function submitModal() {
  modalError.value = ''
  const msg = validateModal()
  if (msg) {
    modalError.value = msg
    return
  }

  modalSaving.value = true
  try {
    const cap = Number(form.capacity)
    const rem = (form.remaining === '' || form.remaining == null) ? cap : Number(form.remaining)

    const payload = {
      hospitalId: form.hospitalId.trim(),
      departmentId: form.departmentId.trim(),
      doctorId: form.doctorId.trim(),
      workDate: form.workDate,
      timeSlot: form.timeSlot,
      totalQuota: cap,
      remaining: rem,
      status: form.status,
      remark: form.remark || ''
    }

    if (modal.mode === 'create') {
      await createSchedule(payload)
    } else {
      await updateSchedule(form.scheduleId, payload)
    }

    closeModal()
    clearSelection()
    await loadSchedules(1)
  } catch (e) {
    modalError.value = e?.message || '提交失败'
  } finally {
    modalSaving.value = false
  }
}

/* =================== 单条关闭/删除（二次确认） =================== */
const closeDlg = reactive({ visible: false, saving: false, row: null })
function confirmClose(row) {
  closeDlg.row = row
  closeDlg.visible = true
}
function closeCloseDlg() {
  closeDlg.visible = false
  closeDlg.saving = false
  closeDlg.row = null
}
async function doClose() {
  if (!closeDlg.row) return
  closeDlg.saving = true
  try {
    await closeSchedule(closeDlg.row.scheduleId)
    closeCloseDlg()
    await loadSchedules(pager.page)
  } catch (e) {
    error.value = e?.message || '关闭失败'
  } finally {
    closeDlg.saving = false
  }
}

const delDlg = reactive({ visible: false, saving: false, row: null })
function confirmDelete(row) {
  delDlg.row = row
  delDlg.visible = true
}
function closeDeleteDlg() {
  delDlg.visible = false
  delDlg.saving = false
  delDlg.row = null
}
async function doDelete() {
  if (!delDlg.row) return
  delDlg.saving = true
  try {
    await deleteSchedule(delDlg.row.scheduleId)
    selectedSet.value.delete(delDlg.row.scheduleId)
    closeDeleteDlg()
    await loadSchedules(1)
  } catch (e) {
    error.value = e?.message || '删除失败'
  } finally {
    delDlg.saving = false
  }
}

/* =================== 展示工具函数 =================== */
function slotLabel(slot) {
  if (slot === '08:00-10:00') return '上午'
  if (slot === '13:30-17:00') return '下午'
  if (slot === '19:00-21:00') return '晚间'
  return slot || '-'
}
function statusLabel(st) {
  if (st === 'OPEN') return '开放'
  if (st === 'CLOSED') return '关闭'
  if (st === 'FULL') return '已满'
  return st || '-'
}
function statusBadgeClass(st) {
  if (st === 'OPEN') return 'badge-green'
  if (st === 'CLOSED') return 'badge-gray'
  if (st === 'FULL') return 'badge-red'
  return 'badge-gray'
}
function safeNum(x) {
  if (x === 0) return 0
  return x ? x : '-'
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

onMounted(async () => {
  // 初始化时加载所有科室（因为默认院区为空，表示所有院区）
  await loadFilterDepartments('')
  await loadSchedules(1)
})
</script>

<style scoped>
.schedule-page { display: flex; flex-direction: column; gap: 14px; }

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

.toolbar-left { display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }

.toolbar-right { display: flex; align-items: flex-end; gap: 10px; }

.field { display: flex; flex-direction: column; gap: 6px; min-width: 210px; }
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
  background: transparent;
  color: #0f172a;
}

.btn:disabled { opacity: 0.55; cursor: not-allowed; }

.btn-primary { background: #2563eb; color: #fff; }
.btn-primary:hover { filter: brightness(0.98); transform: translateY(-1px); }

.btn-ghost { background: transparent; border-color: #e2e8f0; color: #0f172a; }
.btn-ghost:hover { background: #f8fafc; }

.btn-danger { background: #ef4444; color: #fff; }
.btn-danger:hover { filter: brightness(0.98); transform: translateY(-1px); }

.btn-mini { height: 32px; padding: 0 10px; border-radius: 10px; font-size: 0.85rem; }

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
.admin-table { width: 100%; border-collapse: collapse; min-width: 1280px; background: #fff; }
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
.badge-green { background: rgba(16, 185, 129, 0.12); color: #047857; border-color: rgba(16, 185, 129, 0.25); }
.badge-gray { background: rgba(100, 116, 139, 0.12); color: #334155; border-color: rgba(100, 116, 139, 0.25); }
.badge-red { background: rgba(239, 68, 68, 0.10); color: #b91c1c; border-color: rgba(239, 68, 68, 0.25); }

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

.modal-actions { display: flex; justify-content: flex-end; gap: 10px; padding-top: 6px; }

.kv {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 10px 14px;
  align-items: center;
}
.k { color: #64748b; font-weight: 900; font-size: 0.9rem; }
.v { color: #0f172a; font-weight: 750; }

@media (max-width: 980px) {
  .field { min-width: unset; width: 100%; }
  .form-grid { grid-template-columns: 1fr; }
  .admin-table { min-width: 980px; }
}

.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg);} to { transform: rotate(360deg);} }
</style>
