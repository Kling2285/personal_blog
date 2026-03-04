<template>
  <div class="post-add-container">
    <!-- 标题栏 -->
    <div class="post-add-header">
      <h2>发布新帖子</h2>
    </div>
    <div class="post-add-form">
      <div class="form-group">
        <label class="form-label">帖子标题 <span class="required">*</span></label>
        <input
            type="text"
            class="form-input"
            v-model="form.title"
            placeholder="请输入帖子标题（最多30字）"
            maxlength="30"
            @blur="validateTitle"
        />
        <div class="error-tip" v-if="titleError">{{ titleError }}</div>
      </div>

      <div class="form-group">
        <label class="form-label">封面图URL <span class="required">*</span></label>
        <div class="upload-row">
          <input
              type="text"
              class="form-input upload-input"
              v-model="form.coverImg"
              @blur="validateCoverImg"
          />
          <button type="button" class="btn upload-btn" @click="triggerUpload">
            上传封面图
          </button>
          <input
              type="file"
              ref="fileInput"
              class="file-input"
              accept="image/*"
              @change="handleFileUpload"
          />
        </div>
        <!-- 预览图 -->
        <div class="preview-container" v-if="form.coverImg">
          <img :src="form.coverImg" alt="封面预览" class="preview-img" />
        </div>
        <div class="error-tip" v-if="coverImgError">{{ coverImgError }}</div>
      </div>

      <!-- 分类选择 -->
      <div class="form-group">
        <label class="form-label">帖子分类 <span class="required">*</span></label>
        <select
            class="form-select"
            v-model="form.categoryId"
            @change="validateCategoryId"
        >
          <option value="" disabled selected>请选择分类</option>
          <option
              v-for="category in categoryList"
              :key="category.id"
              :value="category.id"
          >
            {{ category.name }}
          </option>
        </select>
        <div class="error-tip" v-if="categoryIdError">{{ categoryIdError }}</div>
      </div>

      <!-- 帖子内容 -->
      <div class="form-group">
        <label class="form-label">帖子内容</label>
        <textarea
            class="form-textarea"
            v-model="form.content"
            rows="5"
        ></textarea>
      </div>

      <!-- 操作按钮 -->
      <div class="form-group form-button-group">
        <button class="btn primary-btn" @click="handleSubmit">发布帖子</button>
        <button class="btn default-btn" @click="handleReset">重置表单</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { addpost } from '@/api/blogpost.js';
import { listcategory, updatecategory } from '@/api/blogcategory.js'; // 新增updatecategory导入
import request from '@/utils/request.js'; // 导入请求工具

// 表单数据
const form = reactive({
  title: '',
  content: '',
  coverImg: '',
  categoryId: '',
  userId: null,
  viewCount: 0,
  likeCount: 0,
  commentCount: 0,
  isShow: 1,
  createTime: new Date().toISOString()
});

// 验证错误提示
const titleError = ref('');
const coverImgError = ref('');
const categoryIdError = ref('');

// 分类列表
const categoryList = ref([]);

// 文件上传相关
const fileInput = ref(null);

// 获取分类列表
const getCategories = async () => {
  try {
    const res = await listcategory({});
    if (res.code === 200) {
      categoryList.value = Array.isArray(res.data) ? res.data : (res.data.list || []);
    } else {
      ElMessage.error('获取分类失败：' + (res.msg || '接口返回异常'));
    }
  } catch (err) {
    console.error('获取分类接口错误：', err);
    ElMessage.error('加载分类失败，请稍后重试');
  }
};

// 初始化用户信息
const initUserInfo = () => {
  try {
    const userStr = localStorage.getItem('login_user');
    if (userStr) {
      const userInfo = JSON.parse(userStr);
      if (userInfo?.userId) {
        form.userId = userInfo.userId;
      } else {
        ElMessage.warning('未获取到用户ID，请重新登录');
      }
    } else {
      ElMessage.warning('请先登录');
    }
  } catch (err) {
    ElMessage.error('解析用户信息失败，请重新登录');
    console.error('用户信息解析错误：', err);
  }
};

// 触发文件选择框
const triggerUpload = () => {
  fileInput.value.click();
};

// 处理文件上传
const handleFileUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  // 验证文件类型
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('请上传图片格式的文件！');
    fileInput.value.value = ''; // 清空选择
    return;
  }

  // 构建FormData
  const formData = new FormData();
  formData.append('file', file);

  try {
    // 调用上传接口（和原代码一致的上传地址）
    const res = await request({
      url: 'http://localhost:8080/file/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    if (res.code === 200 && res.data) {
      // 上传成功，填充URL到表单
      form.coverImg = res.data;
      ElMessage.success('封面图上传成功！');
      coverImgError.value = ''; // 清空验证错误
    } else {
      ElMessage.error('封面图上传失败：' + (res.msg || '接口返回异常'));
    }
  } catch (err) {
    console.error('上传接口错误：', err);
    ElMessage.error('封面图上传失败，请稍后重试');
  } finally {
    // 清空文件选择框，避免重复上传同一文件不触发change事件
    fileInput.value.value = '';
  }
};

// 验证标题
const validateTitle = () => {
  if (!form.title.trim()) {
    titleError.value = '请输入帖子标题';
  } else if (form.title.length > 30) {
    titleError.value = '标题最多30字';
  } else {
    titleError.value = '';
  }
};

// 验证封面图URL
const validateCoverImg = () => {
  if (!form.coverImg.trim()) {
    coverImgError.value = '请输入封面图URL或上传封面图';
  } else {
    coverImgError.value = '';
  }
};

// 验证分类
const validateCategoryId = () => {
  if (!form.categoryId) {
    categoryIdError.value = '请选择帖子分类';
  } else {
    categoryIdError.value = '';
  }
};

// 整体表单验证
const validateForm = () => {
  validateTitle();
  validateCoverImg();
  validateCategoryId();
  return !titleError.value && !coverImgError.value && !categoryIdError.value;
};

// 新增：更新分类的帖子数量
const updateCategoryPostCount = async (categoryId) => {
  try {
    // 1. 先获取该分类当前的postCount
    const category = categoryList.value.find(item => item.id === categoryId);
    if (!category) return;

    // 2. 调用分类更新接口，将postCount+1
    const res = await updatecategory({
      id: categoryId,
      name: category.name, // 保留原有名称
      postCount: category.postCount + 1, // 数量+1
      isShow: category.isShow // 保留原有显示状态
    });

    if (res.code === 200) {
      category.postCount += 1;
      console.log(`分类【${category.name}】帖子数更新为：${category.postCount}`);
    } else {
      ElMessage.warning(`分类计数更新失败：${res.msg || '接口异常'}`);
    }
  } catch (err) {
    console.error('更新分类帖子数错误：', err);
    // 仅警告，不阻断帖子发布流程
    ElMessage.warning('分类计数更新失败，但帖子发布成功');
  }
};

// 提交表单
const handleSubmit = () => {
  if (!validateForm()) {
    ElMessage.warning('请完善必填项后提交');
    return;
  }

  addpost(form)
      .then((res) => {
        if (res.code === 200) {
          ElMessage.success('帖子发布成功！');
          // 新增：发布成功后，更新对应分类的postCount
          updateCategoryPostCount(form.categoryId);
          handleReset();
        } else {
          ElMessage.error('发布失败：' + (res.msg || '未知错误'));
        }
      })
      .catch((err) => {
        ElMessage.error('请求失败，请稍后重试');
        console.error('发布帖子请求错误：', err);
      });
};

// 重置表单
const handleReset = () => {
  form.title = '';
  form.content = '';
  form.coverImg = '';
  form.categoryId = '';
  form.viewCount = 0;
  form.likeCount = 0;
  form.commentCount = 0;
  form.isShow = 1;
  form.createTime = new Date().toISOString();
  titleError.value = '';
  coverImgError.value = '';
  categoryIdError.value = '';
};

// 组件挂载初始化
onMounted(() => {
  initUserInfo();
  getCategories();
});
</script>

<style scoped>
/* 容器样式 */
.post-add-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
  font-family: "Microsoft Yahei", sans-serif;
}

/* 标题栏 */
.post-add-header {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  color: #333;
}

/* 表单容器 */
.post-add-form {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

/* 表单项组 */
.form-group {
  margin-bottom: 25px; /* 表单项间距 */
  font-size: 16px;
}

/* 标签样式 */
.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

/* 必填星号 */
.required {
  color: #f56c6c;
  margin-left: 4px;
}

/* 输入框通用样式 */
.form-input, .form-select {
  width: 100%;
  padding: 12px 15px;
  font-size: 16px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-input:focus, .form-select:focus {
  outline: none;
  border-color: #ff4040;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 上传行样式 */
.upload-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.upload-input {
  flex: 1;
}

.upload-btn {
  white-space: nowrap;
  padding: 12px 18px;
  background-color: #ff4040;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
}

.upload-btn:hover {
  background-color: #ff6666;
}

/* 隐藏的文件选择框 */
.file-input {
  display: none;
}

/* 预览图样式 */
.preview-container {
  margin-top: 10px;
}

.preview-img {
  width: 100px;
  height: auto;
  border-radius: 4px;
  border: 1px solid #eee;
}

/* 文本域样式 */
.form-textarea {
  width: 100%;
  padding: 12px 15px;
  font-size: 16px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  box-sizing: border-box;
  resize: vertical; /* 允许垂直拉伸 */
  transition: border-color 0.2s;
}

.form-textarea:focus {
  outline: none;
  border-color: #ff4040;
}

/* 错误提示 */
.error-tip {
  margin-top: 4px;
  font-size: 14px;
  color: #f56c6c;
}

/* 按钮组 */
.form-button-group {
  margin-top: 10px;
}

/* 按钮通用样式 */
.btn {
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-right: 15px;
}

/* 主按钮 */
.primary-btn {
  background-color: #ff0000;
  color: #fff;
}

.primary-btn:hover {
  background-color: #ff6666;
}

/* 默认按钮 */
.default-btn {
  background-color: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
}

.default-btn:hover {
  background-color: #e8eaed;
}
</style>