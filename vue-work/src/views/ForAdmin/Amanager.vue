<template>
  <div class="manager-layout">
    <!-- 顶部表头 -->
    <header class="app-header">
      <div class="header-left">
        <img src="@/assets/img/2.jpg" alt="Logo" class="logo" />
        <span class="site-name">博客管理后台</span>
      </div>
      <div class="header-right">
        <el-popover
            placement="bottom"
            :width="150"
            trigger="click"
        >
          <template #reference>
            <span class="username">{{ user.nickname || '未登录' }}</span>
          </template>
          <div class="popover-menu">
            <el-button link size="small" @click="handleLogout">退出登录</el-button>
          </div>
        </el-popover>
      </div>
    </header>

    <!-- 主体布局 -->
    <div class="main-content">
      <!-- 左侧导航栏 -->
      <aside class="sidebar">
        <el-menu
            router
            :default-active="$route.path"
            class="menu"
            background-color="#ffffff"
            text-color="#333"
            active-text-color="#ff0000"
            :shadow="true"
        >
          <el-menu-item index="/manager/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/manager/category">
            <el-icon><Files /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/manager/post">
            <el-icon><Document /></el-icon>
            <span>帖子管理</span>
          </el-menu-item>
          <el-menu-item index="/manager/user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/manager/notice">
            <el-icon><Setting /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
          <el-menu-item index="/manager/word">
            <el-icon><WarnTriangleFilled /></el-icon>
            <span>敏感词管理</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <!-- 主内容区域 -->
      <main class="main-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import {
  HomeFilled,
  Document,
  Setting,
  User,
  Files,
  WarnTriangleFilled
} from '@element-plus/icons-vue'

import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import router from "@/router/index.js";

const $route = useRoute()
const $router = useRouter()

const user = ref({})

const handleLogout = () => {
  localStorage.removeItem('login_user')
  user.value = {}
  $router.push('/login')
}

onMounted(() => {
  const loginUser = localStorage.getItem('login_user')
  if (loginUser) {
    user.value = JSON.parse(loginUser)
  }
})
</script>

<style scoped>
.manager-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: Arial, sans-serif;
}

.app-header {
  height: 70px;
  background-color: rgb(250, 0, 0);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 8px;
}

.site-name {
  font-size: 18px;
  font-weight: bold;
}

.username {
  cursor: pointer;
  font-size: 18px;
  margin-right: 30px;
}
.username:hover {
  text-decoration: underline;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  background-color: #ffffff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  overflow-y: auto;
  border-right: 1px solid #ebeef5;
}

.menu {
  border-right: none;
  height: 100%;
}

.main-area {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

.content-placeholder {
  font-size: 18px;
  color: #666;
  text-align: center;
  margin-top: 100px;
}

.popover-menu {
  display: flex;
  flex-direction: column;
  padding: 10px 0;
}
.popover-menu .el-button {
  padding: 8px 15px;
  text-align: left;
}
.popover-menu .el-button:hover {
  background-color: #f5f7fa;
}
</style>