<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-left">
        <h1 class="logo">博客</h1>
      </div>
    </header>

    <div class="content-wrapper">
      <aside class="sidebar" :class="{ 'collapsed': isSidebarCollapsed }">
        <nav class="nav-menu">
          <ul>
            <!-- 循环仅渲染带图标的菜单项：发现/发布/通知 -->
            <li v-for="item in menuItems" :key="item.path">
              <router-link :to="item.path" class="nav-link">
                <el-icon class="menu-icon">
                  <component :is="item.icon" />
                </el-icon>
                <span class="menu-text">{{ item.name }}</span>
              </router-link>
            </li>
            <li v-if="isLogin">
              <router-link to="/umanager/center" class="nav-link">
                <div class="avatar-container">
                  <img :src="userInfo.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" alt="用户头像" class="user-avatar" />
                </div>
                <span class="menu-text">我</span>
              </router-link>
            </li>
            <li v-else>
              <router-link to="/login"
                           class="nav-link"
                           style="background-color: #fb0000;
                           color: white;
                           border-radius: 30px;">
                <span class="menu-text" style="margin-left: 90px">登录</span>
              </router-link>
            </li>
          </ul>
        </nav>
      </aside>

      <!-- 主内容区域 -->
      <main class="main-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElIcon } from 'element-plus'
import {
  Compass,
  Upload,
  Bell
} from '@element-plus/icons-vue'

const isSidebarCollapsed = ref(false)
const route = useRoute()


const loadUserInfo = () => {
  try {
    const userStr = localStorage.getItem("login_user");
    if (userStr) {
      const user = JSON.parse(userStr);
      return user;
    }
    return null;
  } catch (e) {
    console.error("解析用户信息失败：", e);
    return null;
  }
};

// 响应式用户信息
const userInfo = ref(loadUserInfo());

const isLogin = computed(() => {
  return !!userInfo.value && !!userInfo.value.userId;
});

watch(
    () => route.path,
    () => {
      userInfo.value = loadUserInfo();
    },
    { immediate: true }
);

onMounted(() => {
  userInfo.value = loadUserInfo();
  window.addEventListener('storage', (e) => {
    if (e.key === 'login_user') {
      userInfo.value = loadUserInfo();
    }
  });
});

const menuItems = [
  { name: '发现', path: '/umanager/uhome', icon: Compass },
  { name: '发布', path: '/umanager/upost', icon: Upload },
  { name: '通知', path: '/umanager/unotice', icon: Bell }
]
</script>

<style scoped>
/* 全局重置 & 布局 */
* {
  margin: 0;
  box-sizing: border-box;
}

.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 0;
  position: relative; /* 为侧边栏绝对定位提供参考 */
}

/* 顶部 Header */
.header {
  height: 80px;
  background-color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  position: fixed; /* 顶部导航也固定 */
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  width: 100px;
  height: 50px;
  background-color: #ff0000;
  border-radius: 30px;
}

.logo {
  height: 40px;
  width: 85px;
  font-size: 28px;
  color: white;
  padding: 4px 8px;
  border-radius: 30px;
  align-items: center;
  margin-left: 10px;
  margin-bottom: 10px;
}

.content-wrapper {
  display: flex;
  flex: 1;
  padding-top: 100px; /* 给固定的顶部导航留出空间 */
  height: calc(100vh - 100px); /* 内容区高度 = 视口高度 - 顶部导航高度 */
  overflow: hidden; /* 隐藏容器滚动 */
}

/* 侧边栏 - 核心修改：固定定位 */
.sidebar {
  width: 300px;
  background-color: white;
  color: #333;
  position: fixed; /* 固定定位，不随滚动移动 */
  top: 80px; /* 顶部对齐到导航栏下方 */
  left: 0;
  bottom: 0; /* 底部对齐到视口底部 */
  z-index: 99; /* 低于顶部导航，高于主内容 */
  transition: transform 0.3s ease, opacity 0.3s ease;
  overflow-y: auto; /* 侧边栏内容超出时自身滚动（可选） */
}

.sidebar.collapsed {
  transform: translateX(-100%);
}

.nav-menu ul {
  list-style: none;
  padding: 20px 0;
}

.nav-menu li {
  margin: 8px 0;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: #333;
  text-decoration: none;
  transition: background-color 0.2s;
  border-radius: 4px;
  margin: 0 8px;
}

/* 图标样式（仅发现/发布/通知有） */
.menu-icon {
  font-size: 30px;
  color: #666;
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* 头像容器样式（「我」的专属） */
.avatar-container {
  width: 40px;
  height: 40px;
  border-radius: 50%; /* 圆形头像 */
  overflow: hidden; /* 裁剪超出部分 */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0; /* 背景兜底 */
}

/* 头像图片样式 */
.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保持比例 */
}

/* 菜单文字样式 */
.menu-text {
  font-size: 24px;
  font-weight: bold;
}

.nav-link:hover,
.nav-link.router-link-exact-active {
  background-color: #f0f0f0;
  color: #333;
}

.nav-link:hover .menu-icon,
.nav-link.router-link-exact-active .menu-icon {
  color: #333;
}

/* 主内容区 - 核心修改：留出侧边栏空间 */
.main-area {
  flex: 1;
  margin-left: 300px; /* 侧边栏宽度，避免内容被遮挡 */
  padding: 24px;
  height: 100%; /* 填满父容器高度 */
  overflow-y: auto;
  background-color: white;
  margin-top: -20px;
}
</style>