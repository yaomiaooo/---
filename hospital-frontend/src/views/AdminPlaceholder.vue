<template>
  <div class="placeholder-page">
    <div class="empty-state">
      <Icon icon="mdi:hammer-wrench" class="empty-icon" />
      <h2>{{ title }} 模块建设中</h2>
      <p>这里将展示 {{ title }} 的详细配置表格与操作表单。</p>
      <button class="btn-primary">新建条目</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Icon } from '@iconify/vue'

const route = useRoute()
const title = computed(() => route.meta?.title || '后台管理')
</script>

<style scoped>
.placeholder-page {
  height: calc(100vh - 64px - 60px);
  display: flex;
  justify-content: center;
  align-items: center;
}
.empty-state { text-align: center; color: #94a3b8; }
.empty-icon { font-size: 4rem; margin-bottom: 20px; color: #cbd5e1; }
.btn-primary { margin-top: 20px; background: #3b82f6; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
</style>
