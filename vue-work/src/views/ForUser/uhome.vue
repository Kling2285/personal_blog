<template>
  <div class="post-container">
    <!-- 分类筛选 + 搜索框 组合区域 -->
    <div style="display: flex; align-items: center; justify-content: space-between; margin-top: 20px; padding: 0 10px;">
      <!-- 左侧分类筛选按钮区域 -->
      <div class="category-filter" style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
        <!-- 全部按钮 -->
        <div
            class="category-btn"
            :class="{ active: activeCategoryId === '' }"
            @click="handleCategoryClick('')"
        >
          全部
        </div>
        <!-- 分类按钮 -->
        <div
            v-for="category in categoryList"
            :key="category.id"
            class="category-btn"
            :class="{ active: activeCategoryId === category.id }"
            @click="handleCategoryClick(category.id)"
        >
          {{ category.name }}
        </div>
      </div>
      <!-- 右侧标题搜索框 -->
      <div style="display: flex; align-items: center; gap: 5px;">
        <el-input
            v-model="searchTitle"
            placeholder="请输入帖子标题搜索"
            clearable
            style="width: 250px;height: 45px"
            @keyup.enter="handleSearch"
        />
        <el-button type="danger" plain @click="handleSearch"   style="height: 45px">
          搜索
        </el-button>
      </div>
    </div>

    <!-- 多列瀑布流容器（核心：column多列布局，自动填充空白） -->
    <div class="post-waterfall" ref="waterfallRef">
      <!-- 帖子卡片 -->
      <div
          class="post-card"
          v-for="post in postList"
          :key="post.id"
          @click="handlePostCardClick(post)"
          style="cursor: pointer;"
      >
        <div class="post-img">
          <el-image
              :src="post.coverImg"
              fit="cover"
              :alt="post.title"
              placeholder="加载中..."
              error="图片加载失败"
              @load="handleImageLoad(post.id)"
              lazy
          />
        </div>
        <div class="post-title">{{ post.title }}</div>
        <div class="post-footer">
          <div class="user-info">
            <el-image
                :src="userMap[post.userId]?.avatar "
                class="avatar"
                fit="cover"
            />
            <span class="nickname">{{ userMap[post.userId]?.nickname }}</span>
          </div>
          <div class="like-btn" @click="handleLike(post)">
            <el-icon :color="likedMap[post.id] ? '#f43838' : '#666'">
              <Star />
            </el-icon>
            <span>{{ post.likeCount || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- 空数据提示 -->
      <div class="empty-tip" v-if="postList.length === 0 && !loading">
        <el-empty description="暂无相关帖子" />
      </div>
      <!-- 加载中状态 -->
      <div class="loading" v-if="loading && postList.length === 0">
        <el-skeleton :rows="3" animated />
      </div>
      <!-- 底部加载中提示 -->
      <div class="bottom-loading" v-if="loading && postList.length > 0">
        <el-skeleton :rows="1" animated />
      </div>
    </div>

    <!-- 帖子详情弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :modal="true"
        :mask-closable="false"
        :close-on-click-modal="true"
        width="800px"
        center
        destroy-on-close
        style="width: 60%; height: 89%; margin-top: 1%; border-radius: 20px;"
    >
      <div style="display: flex; width: 100%; height: 500px; border-radius: 8px; overflow: hidden;">
        <!-- 左侧图片（固定宽度45%，不滚动） -->
        <div style="width: 45%; height: 100%; background: white; display: flex; align-items: center; justify-content: center; padding: 0; margin: 0;">
          <el-image
              :src="currentPost.coverImg"
              fit="contain"
              style="max-width: 100%; max-height: 100%;"
              error="图片加载失败"
          />
        </div>

        <!-- 右侧整体容器（固定宽度55%，内部拆分三部分） -->
        <div style="flex: 1; height: 100%; background: #fff; display: flex; flex-direction: column; overflow: hidden;">
          <!-- 1. 头像栏（固定高度，不滚动） -->
          <div style="padding: 20px 20px 0 20px;">
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 20px;">
              <el-image
                  :src="userMap[currentPost.userId]?.avatar"
                  class="avatar"
                  fit="cover"
                  style="width: 40px; height: 40px; border-radius: 50%;"
              />
              <span style="font-size: 20px; font-weight: 500;">{{ userMap[currentPost.userId]?.nickname }}</span>
            </div>
          </div>

          <!-- 2. 中间内容区（自适应高度，仅这里能滚动） -->
          <div style="flex: 1; padding: 0 20px; overflow-y: auto;">
            <div style="font-size: 24px; font-weight: bold; margin-bottom: 15px; color: #333;">
              {{ currentPost.title }}
            </div>
            <div style="font-size: 18px; line-height: 1.6; color: #666; margin-bottom: 15px;">
              {{ currentPost.content }}
            </div>
            <div style="font-size: 16px; color: #999; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0;">
              分类：{{ getCategoryName(currentPost.categoryId) || '未分类' }}
            </div>
            <div style="font-size: 16px; color: #999; padding-bottom: 10px;margin-bottom: 15px;margin-top: 15px">
              共{{currentPost.commentCount}}条评论
            </div>

            <div v-if="commentListLoading" style="padding: 10px 0;">
              <el-skeleton :rows="2" animated />
            </div>

            <!-- 无评论提示 -->
            <div v-else-if="commentList.length === 0" style="text-align: center; padding: 20px 0; color: #999;">
              暂无评论，快来抢沙发～
            </div>

            <!-- 评论列表 -->
            <div v-else style="margin-top: 10px;">
              <div
                  v-for="comment in commentList"
                  :key="comment.id"
                  style="padding: 15px 0; border-bottom: 1px solid #f5f5f5;"
              >
                <!-- 评论用户信息 -->
                <div style="display: flex; align-items: center; margin-bottom: 10px;">
                  <el-image
                      :src="userMap[comment.userId]?.avatar"
                      style="width: 32px; height: 32px; border-radius: 50%; margin-right: 10px;"
                      fit="cover"
                  />
                  <div>
                    <div style="font-size: 14px; font-weight: 500; color: #333;">
                      {{ userMap[comment.userId]?.nickname || '未知用户' }}
                    </div>
                    <div style="font-size: 12px; color: #999; margin-top: 2px;">
                      {{ formatCommentTime(comment.createTime) }}
                    </div>
                  </div>
                </div>
                <!-- 评论内容 -->
                <div style="font-size: 16px; line-height: 1.5; color: #666; padding-left: 42px;">
                  {{ comment.content }}
                </div>
              </div>
            </div>

            <!-- 可添加更多内容，这里会滚动 -->
          </div>

          <!-- 3. 评论输入框（固定高度，不滚动） -->
          <div style="padding: 15px 20px; background: #fff; border-top: 1px solid #f0f0f0; box-sizing: border-box;">
            <el-input
                v-model="commentContent"
                placeholder="请输入评论内容..."
                type="textarea"
                style="margin-bottom: 10px; --el-input-focus-border-color: red "
                @keyup.enter="(e) => handleEnterSubmit(e)"
                :autosize="{ minRows: 1, maxRows: 3 }"
            />
            <el-button
                type="primary"
                style="width: 30%;background-color:red;border-color: red "
                @click="handleSubmitComment"
                :loading="commentLoading"
            >
              发送
            </el-button>

            <el-button
                type="primary"
                style="width: 30%;background-color:darkgray;border-color: darkgray "
                @click="handleCancelComment"
                :disabled="commentLoading"
            >
              取消
            </el-button>

          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { listcategory } from "@/api/blogcategory.js";

import { listpostRandom, updatepost } from '@/api/blogpost.js';
import { selectone } from '@/api/bloguser.js';
import {addcomment, listcomment} from "@/api/comment.js";
import { Star } from '@element-plus/icons-vue'
import { ElMessage, ElEmpty, ElImage, ElSkeleton, ElInput, ElButton } from "element-plus";

// 分类相关
const categoryList = ref([]);
const activeCategoryId = ref('');
const searchTitle = ref('');

// 帖子相关
const postList = ref([]);
const loading = ref(false);
const hasMore = ref(true);
const waterfallRef = ref(null);
const queryParams = ref({
  pageNum: 1,
  pageSize: 12,
  categoryId: '',
  title: '',
  isShow: 1
});

// 其他状态
const imgLoadedMap = ref({});
const userMap = ref({});
const likedMap = ref({});
const dialogVisible = ref(false);
const currentPost = ref({});
const dialogMaskClosable = ref(false);

// 评论相关
const commentContent = ref('');
const commentLoading = ref(false);

const commentList = ref([]); // 存储当前帖子的所有评论
const commentListLoading = ref(false); // 评论列表加载状态

// 监听滚动
let scrollObserver = null;

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const res = await listcategory({});
    categoryList.value = res.data || [];
    if (res.code !== 200) {
      ElMessage.error('获取分类列表失败：' + res.msg);
    }
  } catch (err) {
    ElMessage.error('接口调用异常：' + (err.response?.data?.msg || '网络错误'));
    categoryList.value = [];
  }
};


// 获取用户信息
const getUserInfo = async (userId) => {
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

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '';
  const category = categoryList.value.find(item => item.id === categoryId);
  return category ? category.name : '未分类';
};

const formatCommentTime = (timeStr) => {
  if (!timeStr) return '';
  // 简单格式化（根据你的时间格式调整，比如LocalDateTime转字符串）
  const date = new Date(timeStr);
  return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};

// 获取帖子列表
// 获取帖子列表（核心修改：调用随机接口）
const fetchPostList = async (isLoadMore = false) => {
  loading.value = true;

  try {
    // 核心修改2：调用随机接口，不传分页参数（接口返回10条随机帖子）
    const res = await listpostRandom();
    const newPosts = res.data?.list || [];

    // 原有获取用户信息的逻辑完全保留
    const userIdList = [...new Set(newPosts.map(item => item.userId))];
    for (const userId of userIdList) {
      if (userId) await getUserInfo(userId);
    }

    // 核心修改3：直接赋值，不用分页拼接（isLoadMore失效，因为不需要加载更多）
    postList.value = newPosts;
    // 可选：如果保留了hasMore，设为false避免滚动加载
    if (hasMore) hasMore.value = false;

    if (res.code !== 200) {
      ElMessage.error('获取帖子列表失败：' + res.msg);
    }
  } catch (err) {
    ElMessage.error('获取帖子失败：' + (err.response?.data?.msg || '网络错误'));
  } finally {
    loading.value = false;
  }
};

const fetchCommentList = async (postId) => {
  if (!postId) return;
  commentListLoading.value = true;
  try {
    const res = await listcomment({ postId: postId, pageNum: 1, pageSize: 100 });
    if (res.code === 200) {
      commentList.value = res.data || [];
      const userIdList = [...new Set(commentList.value.map(item => item.userId))];
      for (const userId of userIdList) {
        if (userId) await getUserInfo(userId);
      }
    }
  } catch (err) {
    console.error('获取评论列表失败：', err);
    ElMessage.error('获取评论失败，请重试');
  } finally {
    commentListLoading.value = false;
  }
};

// 分类点击
const handleCategoryClick = (categoryId) => {
  activeCategoryId.value = categoryId;
  queryParams.value.categoryId = categoryId;
  queryParams.value.pageNum = 1;
  hasMore.value = true;
  fetchPostList();
};

// 搜索
const handleSearch = () => {
  queryParams.value.title = searchTitle.value;
  queryParams.value.pageNum = 1;
  hasMore.value = true;
  fetchPostList();
};

// 图片加载
const handleImageLoad = (postId) => {
  imgLoadedMap.value[postId] = true;
};

// 点赞
const handleLike = async (post) => {
  const isLiked = !likedMap.value[post.id];
  likedMap.value[post.id] = isLiked;
  const newLikeCount = isLiked ? (post.likeCount || 0) + 1 : (post.likeCount || 0) - 1;

  post.likeCount = newLikeCount;
  try {
    await updatepost({ id: post.id, likeCount: newLikeCount });
  } catch (err) {
    likedMap.value[post.id] = !isLiked;
    post.likeCount = isLiked ? (post.likeCount || 0) - 1 : (post.likeCount || 0) + 1;
    ElMessage.error('操作失败，请重试');
  }
};

// 打开帖子弹窗
const handlePostCardClick = (post) => {
  currentPost.value = post;
  dialogVisible.value = true;
  getUserInfo(post.userId);
  fetchCommentList(post.id);
};

// 回车提交评论（阻止默认换行行为）
const handleEnterSubmit = (e) => {
  // 阻止textarea默认换行
  e.preventDefault();
  // 触发提交
  handleSubmitComment();
};

// 提交评论（核心修复）
const handleSubmitComment = async () => {
  // 1. 校验内容
  if (!commentContent.value.trim()) {
    ElMessage.warning("请输入评论内容！");
    return;
  }

  // 2. 修复：读取正确的localStorage key（和request.js一致）
  let loginUser = {};
  try {
    const userStr = localStorage.getItem('login_user');
    if (userStr) {
      loginUser = JSON.parse(userStr);
    }
  } catch (e) {
    console.error('解析登录信息失败：', e);
    ElMessage.error('登录信息异常，请重新登录');
    return;
  }

  // 3. 校验用户ID（允许未登录时提示，而非直接阻断请求）
  if (!loginUser.userId && !loginUser.id) {
    ElMessage.warning("请先登录！");
    return;
  }
  const userId = loginUser.userId || loginUser.id; // 兼容userId/id两种字段名

  // 4. 校验帖子ID
  if (!currentPost.value.id) {
    ElMessage.warning("未获取到帖子信息！");
    return;
  }

  commentLoading.value = true;
  try {
    console.log('提交评论参数：', {
      postId: currentPost.value.id,
      userId: userId,
      content: commentContent.value.trim(),
      parentId: 0
    });

    // 5. 提交评论（打印请求信息，方便排查）
    const commentRes = await addcomment({
      postId: currentPost.value.id,
      userId: userId,
      content: commentContent.value.trim(),
      parentId: 0
    });

    if (commentRes.code === 200) {
      ElMessage.success("评论提交成功！");
      commentContent.value = '';

      // 更新评论数
      const newCommentCount = (currentPost.value.commentCount || 0) + 1;
      await updatepost({
        id: currentPost.value.id,
        commentCount: newCommentCount
      });
      currentPost.value.commentCount = newCommentCount;
      fetchCommentList(currentPost.value.id);
    } else {
      ElMessage.error("评论提交失败：" + (commentRes.msg || '未知错误'));
    }
  } catch (err) {
    // 打印完整错误信息，方便定位
    console.error('评论提交失败详情：', err);
    ElMessage.error("评论提交失败：" + (err.response?.data?.msg || err.message || '网络错误'));
  } finally {
    commentLoading.value = false;
  }
};

const handleCancelComment = () => {
  // 清空输入框内容
  commentContent.value = '';
  // dialogVisible.value = false;
};
// 初始化滚动监听
const initScrollObserver = () => {
  if (!waterfallRef.value) return;

  const triggerEl = document.createElement('div');
  triggerEl.id = 'lazy-load-trigger';
  triggerEl.style.height = '1px';
  triggerEl.style.opacity = '0';
  triggerEl.style.width = '100%';
  waterfallRef.value.appendChild(triggerEl);

  scrollObserver = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        if (entry.isIntersecting && !loading.value && hasMore.value) {
          queryParams.value.pageNum += 1;
          fetchPostList(true);
        }
      },
      { threshold: 0.1 }
  );

  scrollObserver.observe(triggerEl);
};

// 组件卸载
onUnmounted(() => {
  if (scrollObserver) {
    const triggerEl = document.getElementById('lazy-load-trigger');
    if (triggerEl) {
      scrollObserver.unobserve(triggerEl);
      triggerEl.remove();
    }
    scrollObserver.disconnect();
  }
});

// 初始化
onMounted(() => {
  fetchCategoryList();
  fetchPostList();
  setTimeout(() => initScrollObserver(), 500);
});
</script>

<style scoped>
.category-filter {
  flex-wrap: wrap;
}
.category-btn {
  font-size: 18px;
  height: 40px;
  width: 50px;
  background-color: #ffffff;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}
.category-btn.active {
  background-color: #f43838;
  color: #fff;
}
.category-btn:hover {
  background-color: #ffffff;
}
.category-btn.active:hover {
  background-color: #e02d2d;
}

.post-waterfall {
  column-count: 3;
  column-gap: 15px;
  column-fill: balance;
  padding: 20px 10px;
  margin-top: 10px;
}

.post-card {
  break-inside: avoid;
  margin-bottom: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.2s ease;
  width: 100%;
}

.post-img {
  width: 100%;
}
.post-img img {
  width: 100%;
  height: auto;
  object-fit: cover;
  display: block;
}

.post-title {
  padding: 10px 15px;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background-color: #fff;
}

.empty-tip {
  padding: 50px 0;
  text-align: center;
}
.loading {
  padding: 20px 10px;
}
.bottom-loading {
  padding: 10px 0;
  text-align: center;
  width: 100%;
  break-inside: avoid;
}

.post-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 15px;
  background-color: #fff;
  border-top: 1px solid #f5f5f5;
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

:deep(.el-dialog__body) {
  ::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;
}
:deep(.el-dialog__body) div[style*="overflow-y: auto"] {
  ::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;
}
</style>