<template>
  <div class="profile-container">
    <div class="profile-card">
      <div class="profile-avatar-wrapper">
        <img
            :src="userInfo.avatar || defaultAvatar"
            alt="用户头像"
            class="profile-avatar"
        />
      </div>

      <!-- 右侧信息区域 -->
      <div class="profile-info-wrapper">
        <div class="profile-item">
          <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
            <h1><span class="info-value">{{ userInfo.nickname }}</span></h1>
            <el-popover
                v-model:visible="popoverVisible"
                placement="top"
                :width="180"
                trigger="click"
            >
              <div style="padding: 8px 0;">
                <div style="padding: 8px 16px; cursor: pointer;" class="menu-item" @click="handleEdit">修改个人资料</div>
                <div style="padding: 8px 16px; cursor: pointer;" class="menu-item" @click="handleLogout">退出登录</div>
              </div>
              <template #reference>
                <el-button type="text" style="border: none; background: transparent;">
                  <span style="font-size: 35px;color: #1a1a1a">...</span>
                </el-button>
              </template>
            </el-popover>
          </div>
        </div>
        <div class="profile-item" style="font-weight:300">
          <span class="info-label">用户ID：</span>
          <span class="info-value">{{ userInfo.userId }}</span>
        </div>
        <div class="profile-item intro-item" style="font-size: 18px">
          <span class="info-value">{{ userInfo.intro || '暂时无简介' }}</span>
        </div>
        <div class="profile-item"></div>
      </div>
    </div>

    <div class="user-posts-container" style="margin-top: 30px;">
      <h2 style="font-size: 20px; color: #333; margin-bottom: 20px;">我的发布</h2>

      <div v-if="postList.length === 0 && !loadingPosts" style="text-align: center; padding: 40px 0; color: #999;">
        暂无发布的帖子
      </div>

      <div v-if="loadingPosts" style="text-align: center; padding: 40px 0;">
        <el-skeleton :rows="3" animated style="width: 80%; margin: 0 auto;" />
      </div>

      <div class="waterfall-container">
        <div
            class="post-card-item"
            v-for="post in postList"
            :key="post.id"
            @click="handlePostCardClick(post)"
            style="border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; background: #fff; break-inside: avoid; margin-bottom: 20px; cursor: pointer;"
        >
          <!-- 帖子封面图：高度自适应，取消固定180px -->
          <div class="post-cover" style="width: 100%; overflow: hidden;">
            <img
                :src="post.coverImg"
                alt="帖子封面"
                style="width: 100%; height: auto; object-fit: cover; display: block;"
            />
          </div>

          <!-- 帖子内容 -->
          <div class="post-content" style="padding: 15px;">
            <h3 style="font-size: 16px; color: #333; margin: 0 0 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
              {{ post.title }}
            </h3>
            <!-- 替换分类ID为用户信息+点赞按钮 -->
            <div class="post-footer">
              <!-- 左侧：用户头像+昵称 -->
              <div class="user-info">
                <el-image
                    :src="userMap[post.userId]?.avatar || defaultAvatar"
                    class="avatar"
                    fit="cover"
                    fallback="defaultAvatar"
                />
                <span class="nickname">{{ userMap[post.userId]?.nickname || '未知用户' }}</span>
              </div>
              <!-- 右侧：点赞按钮+数量 -->
              <div class="like-btn" @click.stop="handleLike(post)">
                <el-icon :color="likedMap[post.id] ? '#f43838' : '#666'">
                  <Star />
                </el-icon>
                <span>{{ post.likeCount || 0 }}</span>
              </div>
            </div>
            <!-- 显示/隐藏状态 -->
            <div style="margin-top: 10px; color: #666; font-size: 12px; text-align: right;">
              <span v-if="post.isShow === 1" style="color: #67c23a;">显示中</span>
              <span v-else style="color: #f56c6c;">已隐藏</span>
            </div>
          </div>

          <!-- 帖子操作按钮 -->
          <div style="position: relative; padding: 0 15px 15px;">
            <!-- 更多按钮 -->
            <el-popover
                v-model:visible="post.popoverVisible"
                placement="top"
                :width="120"
                trigger="click"
            >
              <div style="padding: 5px 0;">
                <div style="cursor: pointer;padding: 8px 16px;" class="menu-item" @click="handleToggleShow(post)">
                  {{ post.isShow === 1 ? '隐藏帖子' : '显示帖子' }}
                </div>
                <div style="padding: 8px 16px; cursor: pointer; color: #f56c6c;" class="menu-item" @click="handleDeletePost(post.id)">
                  删除帖子
                </div>
              </div>
              <template #reference>
                <button style="position: absolute; right: 20px; bottom: 103px; border: none; background: #f5f5f5; border-radius: 50%; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 17px;">
                  ...
                </button>
              </template>
            </el-popover>
          </div>
        </div>
      </div>
    </div>

    <!-- 个人资料编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="title"
        width="700"
        style="margin-left: 25%;margin-top: 10%;height: 350px"

    >
      <el-form :model="form" :label-width="85" ref="form1" style="gap: 10px;row-gap: 5px" >
        <el-form-item label="用户名" >
          <el-input v-model="form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="form.nickname" autocomplete="off" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" autocomplete="off" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" autocomplete="off" />
        </el-form-item>
        <el-form-item label="头像">
          <el-row style="width: 100%">
            <el-col :span="18">
              <el-input v-model="form.avatar" readonly disabled autocomplete="off" />
            </el-col>
            <el-col :span="6">
              <el-upload
                  action="http://localhost:8080/file/upload"
                  :show-file-list="false"
                  :on-success="avatarUploadSuccess"
              >
                <el-button type="primary">上传头像</el-button>
              </el-upload>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增：帖子详情弹窗 -->
    <el-dialog
        v-model="postDialogVisible"
        :modal="true"
        :mask-closable="true"
        :close-on-click-modal="true"
        width="800px"
        center
        destroy-on-close
        style="width: 60%;
        height: 89%;
        margin-top: 1%;
        border-radius: 20px;
        margin-left: 23%;"

    >
      <!-- 弹窗内容：左右分栏布局 -->
      <div style="display: flex; width: 100%; height: 500px; border-radius: 8px; overflow: hidden;">
        <!-- 左侧：图片区域（无padding/margin） -->
        <div style="width: 45%; height: 100%; background: white; display: flex; align-items: center; justify-content: center; padding: 0; margin: 0;">
          <el-image
              :src="currentPost.coverImg"
              fit="contain"
              style="max-width: 100%; max-height: 100%;"
              error="图片加载失败"
          />
        </div>

        <!-- 右侧：帖子信息区域 -->
        <div style="flex: 1; height: 100%; background: #fff; padding: 20px; overflow-y: auto;">
          <!-- 1. 用户头像+昵称 -->
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 20px;">
            <el-image
                :src="userMap[currentPost.userId]?.avatar || defaultAvatar"
                class="avatar"
                fit="cover"
                style="width: 40px; height: 40px; border-radius: 50%;"
            />
            <span style="font-size: 20px; font-weight: 500;">{{ userMap[currentPost.userId]?.nickname || '未知用户' }}</span>
          </div>

          <!-- 2. 帖子标题 -->
          <div style="font-size: 24px; font-weight: bold; margin-bottom: 15px; color: #333;">
            {{ currentPost.title }}
          </div>

          <!-- 3. 帖子内容 -->
          <div style="font-size: 18px; line-height: 1.6; color: #666; margin-bottom: 15px;">
            {{ currentPost.content }}
          </div>

          <!-- 4. 帖子分类 -->
          <div style="font-size: 16px; color: #999; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0;">
            分类：{{ getCategoryName(currentPost.categoryId) || '未分类' }}
          </div>

          <!-- 剩余区域空白 -->
          <div style="flex: 1;"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted, reactive, nextTick} from 'vue'
import {useRouter} from "vue-router";
import {selectone, updateuser} from "@/api/bloguser.js";
import {listcategory} from "@/api/blogcategory.js";
import {listpost, deletepost, updatepost} from '@/api/blogpost.js';
import {ElMessage, ElMessageBox, ElImage, ElIcon} from "element-plus";
import { Star } from '@element-plus/icons-vue';

const user = ref({})
const $router = useRouter()
const form1 = ref();

// 帖子相关状态
const postList = ref([]);
const loadingPosts = ref(false);
// 新增：用户信息映射表 + 点赞状态映射表
const userMap = ref({});
const likedMap = ref({});

// 其他状态保持不变
const title = ref('修改个人资料')
const popoverVisible = ref(false)
const dialogVisible = ref(false)

// 新增：帖子弹窗相关状态
const postDialogVisible = ref(false);
const currentPost = ref({});

// 新增：缓存分类列表（核心修复）
const categoryList = ref([]);

const form = reactive({
  userId: '',
  username: '',
  password: '',
  nickname:'',
  avatar:'',
  phone: '',
  email: '',
  status: '0',
  intro:''
});

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 获取本地用户信息
const getUserInfo = () => {
  try {
    const userStr = localStorage.getItem("login_user");
    return userStr ? JSON.parse(userStr) : {};
  } catch (e) {
    console.error("解析用户信息失败：", e);
    return {};
  }
};

const userInfo = ref(getUserInfo());

// 退出登录
const handleLogout=()=>{
  localStorage.removeItem('login_user')
  user.value = {}
  window.location.href = '/umanager/uhome';
  $router.push('/umanager/uhome')
}

// 头像上传成功回调
const avatarUploadSuccess=(res)=>{
  form.avatar = res.data;
}

// 提交修改
const submitForm = () => {
  updateuser(form).then(res=>{
    if(res.code === 200){
      ElMessage.success("修改成功");
      dialogVisible.value = false;
      popoverVisible.value = false;
      const newUser = {...userInfo.value, ...form};
      localStorage.setItem("login_user", JSON.stringify(newUser));
      userInfo.value = newUser;
    }else{
      ElMessage.error("修改失败：" + res.msg);
    }
  }).catch(err => {
    ElMessage.error("修改失败：" + (err.response?.data?.msg || '网络错误'));
  });
};

const handleEdit = () => {
  popoverVisible.value = false;
  const userId = userInfo.value.userId;
  if(!userId){
    ElMessage.error("未获取到用户信息，请重新登录");
    return;
  }
  selectone(userId).then(res => {
    if (res.code === 200 && res.data) {
      Object.assign(form, res.data);
      form.status = String(form.status) || '0';
      dialogVisible.value = true;
    } else {
      ElMessage.error('获取用户信息失败');
    }
  }).catch(err => {
    ElMessage.error('获取用户信息失败：' + (err.response?.data?.msg || '网络异常'));
  });
}

// 新增：加载分类列表（核心修复）
const fetchCategoryList = async () => {
  try {
    const res = await listcategory({});
    if (res.code === 200) {
      categoryList.value = res.data || [];
    }
  } catch (err) {
    console.error('加载分类列表失败：', err);
    categoryList.value = [];
  }
};

// 新增：根据userId获取用户信息（缓存）
const getUserInfoByUserId = async (userId) => {
  if (userMap.value[userId]) return userMap.value[userId];
  try {
    const res = await selectone(userId);
    if (res.code === 200 && res.data) {
      userMap.value[userId] = res.data;
      return res.data;
    }
  } catch (err) {
    console.error('获取用户信息失败：', err);
  }
  userMap.value[userId] = { nickname: '未知用户', avatar: '' };
  return userMap.value[userId];
};

// 修复：同步获取分类名称（核心修复）
const getCategoryName = (categoryId) => {
  if (!categoryId || !categoryList.value.length) return '未分类';
  const category = categoryList.value.find(item => item.id === categoryId);
  return category ? category.name : '未分类';
};

// 新增：帖子卡片点击事件
const handlePostCardClick = (post) => {
  currentPost.value = post;
  postDialogVisible.value = true;
  // 补充：如果分类列表未加载，立即加载
  if (!categoryList.value.length) {
    fetchCategoryList();
  }
  // 确保获取用户信息
  getUserInfoByUserId(post.userId);
};

// 新增：点赞/取消点赞函数
const handleLike = async (post) => {
  // 切换点赞状态
  const isLiked = !likedMap.value[post.id];
  likedMap.value[post.id] = isLiked;

  // 计算新的点赞数：点赞+1，取消-1
  const newLikeCount = isLiked ? (post.likeCount || 0) + 1 : (post.likeCount || 0) - 1;

  // 1. 前端先更新数值（优化体验）
  post.likeCount = newLikeCount;

  // 2. 后端更新
  try {
    await updatepost({
      id: post.id,
      likeCount: newLikeCount // 传给后端新的点赞数
    });
  } catch (err) {
    // 失败回滚：恢复原状态和数值
    likedMap.value[post.id] = !isLiked;
    post.likeCount = isLiked ? (post.likeCount || 0) - 1 : (post.likeCount || 0) + 1;
    ElMessage.error('操作失败，请重试');
  }
};

// ========== 帖子相关功能 ==========
const getMyPosts = async () => {
  const userId = userInfo.value.userId;
  if (!userId) return;

  loadingPosts.value = true;
  try {
    const res = await listpost({
      pageNum: 1,
      pageSize: 100,
      userId: userId,
      isShow: ''
    });

    if (res.code === 200) {
      // 为每个帖子添加popoverVisible状态
      const posts = (res.data?.list || []).map(post => ({
        ...post,
        popoverVisible: false
      }));
      postList.value = posts;

      // 批量获取用户信息
      const userIdList = [...new Set(posts.map(item => item.userId))];
      for (const uid of userIdList) {
        if (uid) await getUserInfoByUserId(uid);
      }

      // 确保DOM渲染完成
      nextTick(() => {
        console.log('帖子列表渲染完成，数量：', postList.value.length);
      });
    } else {
      ElMessage.error('获取帖子列表失败：' + res.msg);
    }
  } catch (err) {
    ElMessage.error('获取帖子失败：' + (err.response?.data?.msg || '网络错误'));
  } finally {
    loadingPosts.value = false;
  }
};

const handleToggleShow = async (post) => {
  post.popoverVisible = false;

  try {
    const res = await updatepost({
      ...post,
      isShow: post.isShow === 1 ? 0 : 1
    });

    if (res.code === 200) {
      post.isShow = post.isShow === 1 ? 0 : 1;
      ElMessage.success(post.isShow === 1 ? '帖子已显示' : '帖子已隐藏');
    } else {
      ElMessage.error('操作失败：' + res.msg);
    }
  } catch (err) {
    ElMessage.error('操作失败：' + (err.response?.data?.msg || '网络错误'));
  }
};

const handleDeletePost = async (postId) => {
  ElMessageBox.confirm(
      '确定要删除该帖子吗？删除后无法恢复！',
      '删除确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      const res = await deletepost(postId);
      if (res.code === 200) {
        postList.value = postList.value.filter(post => post.id !== postId);
        ElMessage.success('帖子删除成功');
      } else {
        ElMessage.error('删除失败：' + res.msg);
      }
    } catch (err) {
      ElMessage.error('删除失败：' + (err.response?.data?.msg || '网络错误'));
    }
  }).catch(() => {});
};

// 页面加载
onMounted(() => {
  userInfo.value = getUserInfo();
  getMyPosts();
  fetchCategoryList(); // 新增：提前加载分类列表
});
</script>



<style scoped>
.profile-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background-color: #fff;
  border-radius: 8px;
}

.profile-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;
  gap: 40px;
}

.profile-avatar-wrapper {
  flex-shrink: 0;
}
.profile-avatar {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #f5f5f5;
}

.profile-info-wrapper {
  padding: 10px 10px 30px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.profile-item {
  display: flex;
  align-items: flex-start;
}

.intro-item {
  align-items: flex-start;
}

.info-label {
  color: #666;
  font-weight: bold;
  width: 70px;
  flex-shrink: 0;
}
.info-value {
  color: #333;
  flex: 1;
  line-height: 1.5;
}

.menu-item:hover {
  background-color: #f5f5f5;
}

.profile-container * {
  box-sizing: border-box;
  margin: 0;
}

.waterfall-container {
  column-count: 3;          /* 3列瀑布流 */
  column-gap: 20px;         /* 列间距 */
  column-fill: balance;     /* 自动平衡列高度 */
  width: 100%;
  padding: 0 10px;
}

/* 帖子卡片样式 */
.post-card-item {
  transition: transform 0.2s ease;
  width: 100%;
}

.post-card-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}


.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-top: 1px solid #f5f5f5;
  margin-top: 5px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}
.nickname {
  font-size: 12px;
  color: #666;
}
.like-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  font-size: 12px;
  color: #666;
  transition: color 0.2s;
}
.like-btn:hover {
  color: #f43838;
}

</style>