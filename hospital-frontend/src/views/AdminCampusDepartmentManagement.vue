<template>
  <div class="campus-page">
    <div class="grid single">
      <!-- 仅保留：科室管理 -->
      <div class="panel">
        <div class="panel-header">
          <div class="header-left">
            <h3>科室管理</h3>
            <div class="header-sub">
              可按院区（医院）筛选科室，并进行新增、编辑、删除操作
            </div>
          </div>

          <div class="header-right">
            <div class="search">
              <Icon icon="mdi:magnify" />
              <input v-model.trim="filters.keyword" class="search-input" placeholder="搜索科室名称/简介" />
            </div>

            <select v-model="filters.hospitalId" class="select select-pill">
              <option value="">全部院区</option>
              <option v-for="h in hospitals" :key="h.hospitalId" :value="h.hospitalId">
                {{ h.hospitalName }}
              </option>
            </select>

            <button class="btn btn-primary" @click="openCreate">
              <Icon icon="mdi:plus" /> 新增科室
            </button>
          </div>
        </div>

        <div class="panel-body">
          <div class="table-wrap">
            <table class="admin-table">
              <thead>
              <tr>
                <th style="width: 220px;">所属院区</th>
                <th style="width: 220px;">科室名称</th>
                <th>简介</th>
                <th style="width: 220px;">操作</th>
              </tr>
              </thead>

              <tbody>
              <tr v-for="d in records" :key="d.departmentId">
                <td>
                  <!-- <span class="badge badge-gray">{{ d.hospitalName || '—' }}</span> -->
                  <div class="muted mono" style="margin-top:6px;">{{ d.hospitalId }}</div>
                </td>
                <td>
                  <div style="font-weight: 900; color:#0f172a;">{{ d.departmentName }}</div>
                  <div class="muted mono" style="margin-top:6px;">{{ d.departmentId }}</div>
                </td>
                <td class="muted">{{ d.departmentIntro || '—' }}</td>
                <td class="actions">
                  <button class="btn-mini" @click="openEdit(d)">
                    <Icon icon="mdi:pencil-outline" /> 编辑
                  </button>
                  <button class="btn-mini danger" @click="deleteUI(d)">
                    <Icon icon="mdi:delete-outline" /> 删除
                  </button>
                </td>
              </tr>

              <tr v-if="records.length === 0">
                <td colspan="4" class="empty-td">
                  <div class="empty-state">
                    <Icon icon="mdi:folder-search-outline" class="empty-icon" />
                    <div class="empty-title">暂无科室</div>
                    <div class="empty-sub">可点击“新增科室”创建。</div>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>

            <!-- 简易分页（保持风格一致：按钮用 btn-mini） -->
            <div class="pager">
              <div class="muted">共 {{ total }} 条</div>
              <div class="pager-actions">
                <button class="btn-mini" :disabled="page<=1" @click="page--, fetchPage()">上一页</button>
                <div class="mono muted">第 {{ page }} 页</div>
                <button class="btn-mini" :disabled="page>=maxPage" @click="page++, fetchPage()">下一页</button>

                <select v-model.number="size" class="select select-small" @change="page=1; fetchPage()">
                  <option :value="10">10/页</option>
                  <option :value="20">20/页</option>
                  <option :value="50">50/页</option>
                </select>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>

    <!-- 科室弹窗（沿用原 modal 风格） -->
    <div v-if="modal.visible" class="modal-mask" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">
            <Icon :icon="modal.mode==='create' ? 'mdi:folder-plus' : 'mdi:folder-edit'" />
            {{ modal.mode==='create' ? '新增科室' : '编辑科室' }}
          </div>
          <button class="icon-btn" @click="closeModal"><Icon icon="mdi:close" /></button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <div class="field">
              <label>所属院区</label>

              <!-- ✅ 新增：可选择院区 -->
              <select v-if="modal.mode==='create'" v-model="form.hospitalId" class="select">
                <option value="" disabled>请选择院区</option>
                <option v-for="h in hospitals" :key="h.hospitalId" :value="h.hospitalId">
                  {{ h.hospitalName }}
                </option>
              </select>

              <!-- ✅ 编辑：院区不可改，只读展示（保留风格，用 input） -->
              <input
                  v-else
                  class="input"
                  :value="getHospitalLabel(form.hospitalId)"
                  disabled
              />
            </div>

            <div class="field">
              <label>科室名称</label>
              <input v-model.trim="form.departmentName" class="input" placeholder="例如：骨科 / 内科 / 急诊科" />
            </div>

            <div class="field full">
              <label>简介</label>
              <textarea v-model.trim="form.departmentIntro" class="textarea" rows="3" placeholder="科室简介（可选）"></textarea>
            </div>

            <div v-if="modal.mode==='edit'" class="field full">
              <label>科室ID</label>
              <input class="input" :value="form.departmentId" disabled />
            </div>
          </div>

          <div v-if="modal.error" class="error-bar">
            <Icon icon="mdi:alert-circle-outline" />
            {{ modal.error }}
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-ghost" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="submit">
            保存
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { Icon } from "@iconify/vue";
import { computed, onMounted, reactive, ref, watch } from "vue";
import {
  pageDepartments,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  listHospitals
} from "../api/adminDepartment";

const hospitals = ref([]);

const filters = reactive({
  keyword: "",
  hospitalId: "",
});

const page = ref(1);
const size = ref(10);
const total = ref(0);
const records = ref([]);

const maxPage = computed(() => {
  const t = total.value || 0;
  const s = size.value || 10;
  return Math.max(1, Math.ceil(t / s));
});

const modal = reactive({
  visible: false,
  mode: "create", // create | edit
  error: "",
});

const form = reactive({
  departmentId: "",
  hospitalId: "",
  departmentName: "",
  departmentIntro: "",
});

function getHospitalLabel(hospitalId) {
  const id = hospitalId || "";
  const h = hospitals.value?.find(x => x.hospitalId === id);
  if (!id) return "—";
  return h?.hospitalName ? `${h.hospitalName}（${id}）` : id;
}

function resetForm() {
  form.departmentId = "";
  form.hospitalId = filters.hospitalId || "";
  form.departmentName = "";
  form.departmentIntro = "";
}

function openCreate() {
  modal.visible = true;
  modal.mode = "create";
  modal.error = "";
  resetForm();
}

function openEdit(row) {
  modal.visible = true;
  modal.mode = "edit";
  modal.error = "";
  form.departmentId = row.departmentId;
  form.hospitalId = row.hospitalId || "";
  form.departmentName = row.departmentName || "";
  form.departmentIntro = row.departmentIntro || "";
}

function closeModal() {
  modal.visible = false;
  modal.error = "";
}

function validate() {
  // ✅ 仅新增时要求选择院区；编辑时院区只读，不做可编辑校验
  if (modal.mode === "create" && !form.hospitalId) return "请选择所属院区";
  if (!form.departmentName) return "科室名称不能为空";
  return "";
}

async function submit() {
  modal.error = "";
  const msg = validate();
  if (msg) {
    modal.error = msg;
    return;
  }

  try {
    if (modal.mode === "create") {
      const payload = {
        hospitalId: form.hospitalId,
        departmentName: form.departmentName,
        departmentIntro: form.departmentIntro,
      };
      const res = await createDepartment(payload);
      if (res?.code !== 200) throw new Error(res?.message || "创建失败");
    } else {
      // ✅ 编辑时不提交 hospitalId（前端层面禁止“改院区”的逻辑）
      const payload = {
        departmentName: form.departmentName,
        departmentIntro: form.departmentIntro,
      };
      const res = await updateDepartment(form.departmentId, payload);
      if (res?.code !== 200) throw new Error(res?.message || "更新失败");
    }

    closeModal();
    await fetchPage();
  } catch (e) {
    modal.error = e?.message || "操作失败";
  }
}

async function deleteUI(row) {
  const ok = confirm(`确认删除科室「${row.departmentName}」？`);
  if (!ok) return;

  try {
    const res = await deleteDepartment(row.departmentId);
    if (res?.code !== 200) throw new Error(res?.message || "删除失败");
    // 删除后如果当前页空了，回退一页
    if (records.value.length === 1 && page.value > 1) page.value -= 1;
    await fetchPage();
  } catch (e) {
    alert(e?.message || "删除失败");
  }
}

async function fetchHospitals() {
  const res = await listHospitals();
  // 兼容 Result<T> 返回
  if (res?.code === 200) {
    hospitals.value = res.data || [];
  } else {
    hospitals.value = [];
  }
}

async function fetchPage() {
  // 修正 page 边界
  if (page.value < 1) page.value = 1;
  if (page.value > maxPage.value) page.value = maxPage.value;

  const params = {
    page: page.value,
    size: size.value,
    keyword: filters.keyword || "",
    hospitalId: filters.hospitalId || "",
  };

  const res = await pageDepartments(params);
  if (res?.code !== 200) {
    records.value = [];
    total.value = 0;
    return;
  }
  records.value = res.data?.records || [];
  total.value = res.data?.total || 0;

  // 再次修正 maxPage 变化导致的越界
  if (page.value > maxPage.value) {
    page.value = maxPage.value;
    await fetchPage();
  }
}

// 搜索/筛选：轻量触发（保持你现有交互风格）
watch(
    () => [filters.keyword, filters.hospitalId],
    () => {
      page.value = 1;
      fetchPage();
    }
);

onMounted(async () => {
  await fetchHospitals();
  await fetchPage();
});
</script>


<style scoped>
/* 保留原风格，只在 grid 上加一个 single 让它变成单列 */
.campus-page { display: flex; flex-direction: column; gap: 20px; }
.grid { display: grid; grid-template-columns: 360px 1fr; gap: 18px; }
.grid.single { grid-template-columns: 1fr; }

.panel {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.panel-header {
  padding: 16px 18px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.header-left { display: flex; flex-direction: column; gap: 4px; }
.header-sub { font-size: 0.85rem; color: #64748b; }
.header-right { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

.panel-header h3 { margin: 0; font-size: 1.05rem; color: #1e293b; }

.panel-body { padding: 14px 16px; }

.table-wrap { display: flex; flex-direction: column; gap: 10px; }

.search {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  padding: 8px 12px;
}
.search-input {
  border: none;
  outline: none;
  background: transparent;
  width: 220px;
  color: #0f172a;
}

.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th { text-align: left; padding: 14px; background: #f8fafc; color: #64748b; font-size: 0.85rem; font-weight: 700; }
.admin-table td { padding: 14px; border-bottom: 1px solid #f1f5f9; color: #334155; font-size: 0.9rem; vertical-align: middle; }

.actions { display: flex; gap: 8px; flex-wrap: wrap; }
.mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace; font-size: 0.85rem; color: #0f172a; }
.muted { color: #64748b; }

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
.btn-ghost { background: #f1f5f9; color: #334155; }

.btn-mini {
  height: 30px;
  padding: 0 10px;
  border-radius: 6px;
  background: #f1f5f9;
  color: #334155;
  font-weight: 700;
  font-size: 0.82rem;
  border: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.btn-mini.danger { background: #fee2e2; color: #b91c1c; }

.badge { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 999px; font-size: 0.78rem; font-weight: 800; }
.badge-gray { background: #f1f5f9; color: #334155; }

.empty-td { padding: 30px !important; }
.empty-state { text-align: center; color: #94a3b8; padding: 24px 10px; }
.empty-icon { font-size: 3rem; color: #cbd5e1; }
.empty-title { font-weight: 900; margin-top: 8px; color: #64748b; }
.empty-sub { margin-top: 4px; font-size: 0.9rem; }

.select {
  height: 36px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 0 12px;
  outline: none;
  color: #0f172a;
  background: #fff;
}
.select-pill { border-radius: 999px; background: #f1f5f9; }
.select-small { height: 30px; border-radius: 8px; }

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 2px;
}
.pager-actions { display: inline-flex; align-items: center; gap: 10px; }

/* 弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 99;
}
.modal {
  width: 720px;
  max-width: 100%;
  background: white;
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.20);
  overflow: hidden;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #e2e8f0;
}
.modal-title { display: inline-flex; align-items: center; gap: 8px; font-weight: 900; color: #0f172a; }
.icon-btn { border: none; background: transparent; cursor: pointer; font-size: 1.1rem; color: #64748b; }
.modal-body { padding: 16px; }
.modal-footer {
  padding: 14px 16px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field.full { grid-column: 1 / -1; }
.field label { font-size: 0.8rem; color: #64748b; }

.input {
  height: 36px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 0 12px;
  outline: none;
  color: #0f172a;
  background: #fff;
}
.textarea {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 12px;
  outline: none;
  color: #0f172a;
  background: #fff;
  resize: vertical;
}
.input:focus,
.select:focus,
.textarea:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.15); }

.error-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff1f2;
  color: #9f1239;
  border: 1px solid #fecdd3;
  padding: 10px 12px;
  border-radius: 10px;
  margin-top: 12px;
}

/* 响应式 */
@media (max-width: 1050px) {
  .grid { grid-template-columns: 1fr; }
  .search-input { width: 160px; }
}
</style>
