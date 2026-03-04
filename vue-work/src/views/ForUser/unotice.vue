<template>
  <div class="notice-container">
    <div class="notice-header">
      <h1>最新通知</h1>
      <el-input
          v-model="searchTitle"
          placeholder="请输入公告标题搜索"
          class="search-input"
          @keyup.enter="fetchNoticeList"
      >
        <template #append>
          <el-button @click="fetchNoticeList"
                     style="background-color: #ff0000;
                     color: white;height: 50px;width: 60px">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div class="notice-card-list">
      <div v-if="noticeList.length === 0 && !loading" class="empty-notice">
        <el-empty description="暂无公告信息"></el-empty>
      </div>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated></el-skeleton>
      </div>

      <div
          v-for="notice in noticeList"
          :key="notice.id"
          class="notice-card"
      >
        <div class="notice-title">{{ notice.title }}</div>
        <div class="notice-time">
          <el-icon><Clock /></el-icon>
          {{ formatTime(notice.publishTime || notice.createTime) }}
        </div>
        <div class="notice-content">{{ notice.content || '暂无内容' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElEmpty, ElSkeleton, ElIcon, ElInput, ElButton, ElMessage } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import { listnotice } from '@/api/notice' // 引入请求函数

const loading = ref(false) // 加载状态
const searchTitle = ref('') // 搜索标题
const noticeList = ref([]) // 公告列表

const formatTime = (time) => {
  if (!time) return '未知时间'
  const date = new Date(time)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const fetchNoticeList = async () => {
  try {
    loading.value = true
    const params = {
      title: searchTitle.value.trim()
    }
    const res = await listnotice(params)
    noticeList.value = res.data?.list || []
  } catch (error) {
    console.error('获取公告列表失败：', error)
    ElMessage.error('获取公告失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchNoticeList()
})
</script>

<style scoped>
/* 容器整体样式 */
.notice-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

/* 页面头部（标题+搜索） */
.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 15px;
}

.notice-header h2 {
  margin: 0;
  color: #333;
  font-size: 22px;
}

.search-input {
  width: 500px;
  height: 50px;
}

/* 公告卡片列表容器（纵向单列） */
.notice-card-list {
  display: flex;
  flex-direction: column; /* 纵向排列 */
  gap: 16px; /* 卡片之间的间距 */
  width: 100%;
}

/* 加载中容器 */
.loading-container {
  width: 100%;
}

/* 空状态容器 */
.empty-notice {
  width: 100%;
  padding: 50px 0;
}

.notice-card {
  width: 100%;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  padding: 24px;
  transition: box-shadow 0.3s ease;
  cursor: default;
  box-sizing: border-box;
}

/* 卡片hover效果（增强交互体验） */
.notice-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.12);
}

/* 公告标题 */
.notice-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

/* 公告时间 */
.notice-time {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 公告内容 */
.notice-content {
  font-size: 15px;
  color: #374151;
  line-height: 1.8;
  margin-bottom: 10px;
  min-height: 80px;
}
</style>