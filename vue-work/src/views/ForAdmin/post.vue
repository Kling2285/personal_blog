<template>
  <div>
    <div class="box">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="帖子标题">
          <el-input v-model="queryParams.title" placeholder="请输入帖子标题" clearable />
        </el-form-item>
        <!-- 分类下拉选择 -->
        <el-form-item label="分类名称">
          <el-select v-model="queryParams.categoryId" placeholder="请选择分类" clearable>
            <el-option
                v-for="category in categoryList"
                :key="category.id"
                :label="category.name"
                :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="queryParams.userId"
                    type="number"
                    placeholder="请输入用户ID（自动匹配用户名）"
                    autocomplete="off"
                    @input="handleUserIdInput"
         />
          <div v-if="queryParams.userId" style="margin-top:4px;color:#666;">
            匹配到用户名：{{ userMap.value[queryParams.userId] || '未找到' }}
          </div>
        </el-form-item>
        <el-form-item label="是否可见">
          <el-select v-model="queryParams.isShow" placeholder="请选择" clearable>
            <el-option label="显示" value="1" />
            <el-option label="隐藏" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button color="#3869f4" plain @click="search">
            <span style="margin-left: 4px;">查询</span>
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="info" plain @click="list00">
            <span style="margin-left: 4px;">重置</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="box" style="display: flex; align-items: center; gap: 8px;">
      <div class="button-row" style="display: flex; align-items: center; gap: 8px;">
        <el-button type="primary" plain @click="handleAdd">
          <el-icon><Plus /></el-icon>添加
        </el-button>
        <el-button type="danger" plain @click="handleDelete">
          <el-icon><CloseBold /></el-icon>批量删除
        </el-button>
        <el-button type="info" plain @click="handleExport">
          <el-icon><Upload /></el-icon>导出
        </el-button>
        <el-upload
            class="upload-demo"
            :show-file-list="false"
            :on-success="handleImportSuccess"
            :action="uploadUrl"
            style="margin-left: 15px;"
        >
          <el-button type="warning" plain>
            <el-icon><Download /></el-icon>导入
          </el-button>
        </el-upload>
      </div>
    </div>
    <div>
      <!-- 直接使用处理后的表格数据 -->
      <el-table :data="data.tableData" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" prop="id" width="50" />
        <el-table-column prop="id" label="帖子ID" width="100" />
        <el-table-column prop="coverImg" label="封面图" width="130"  >
          <template #default="scope">
            <el-image :src="scope.row.coverImg" style="width:40px;height: 40px"></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="帖子标题" width="300" />
        <el-table-column label="分类名称" width="100">
          <template #default="scope">
            {{ scope.row.categoryName || '未分类' }}
          </template>
        </el-table-column>
        <el-table-column label="用户名" width="120">
          <template #default="scope">
            {{ scope.row.userName || '未知用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="isShow" label="是否可见" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isShow === 1" type="success">显示</el-tag>
            <el-tag v-else type="danger">隐藏</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">
              <el-icon><EditPen /></el-icon>编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
            >
              <el-icon><CloseBold /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="pagination">
      <el-pagination
          background
          layout="prev, pager, next, jumper, ->, total, sizes"
          v-model:page-size="queryParams.pageSize"
          v-model:current-page="queryParams.pageNum"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :total="data.total"
          :page-sizes="[2, 5, 10, 20]"
      />
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="visible" :title="title" width="700">
      <el-form :model="form" :label-width="85" :rules="rules" ref="form1">
        <el-form-item label="帖子标题" prop="title">
          <el-input v-model="form.title" autocomplete="off" />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option
                v-for="category in categoryList"
                :key="category.id"
                :label="category.name"
                :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" type="number" autocomplete="off" />
        </el-form-item>
        <el-form-item label="是否可见" prop="isShow">
          <el-select v-model="form.isShow" placeholder="请选择">
            <el-option label="显示" value="1" />
            <el-option label="隐藏" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图">
          <el-row style="width: 100%">
            <el-col :span="18">
              <el-input v-model="form.coverImg" readonly disabled autocomplete="off" />
            </el-col>
            <el-col :span="6">
              <el-upload
                  action="http://localhost:8080/file/upload"
                  :show-file-list="false"
                  :on-success="coverImgUploadSuccess"
              >
                <el-button type="primary">上传封面图</el-button>
              </el-upload>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="帖子内容" prop="content">
          <div ref="editorRef" style="height: 320px;width: 500px; border: 1px solid #e6e6e6; border-radius: 4px;"></div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick, onBeforeUnmount, onMounted } from 'vue'
import {
  listpost,
  deletepost,
  addpost,
  selectonepost,
  updatepost
} from '@/api/blogpost.js'
import { listcategory } from '@/api/blogcategory.js'
import { selectone } from '@/api/bloguser.js'
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,CloseBold,Download,Upload,EditPen
} from '@element-plus/icons-vue'
import {download} from '@/utils/request.js'
import { createEditor } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'

// 导入接口地址
const uploadUrl=ref(import.meta.env.VITE_API_BASE_URL+'/post/import');
const ids = ref([])
const visible = ref(false);
const title = ref('添加帖子');

// 分类列表
const categoryList = ref([])
// 分类映射表（用于快速查找）
const categoryMap = ref({})
const userMap = ref({})

// 表单数据
const form = reactive({
  id: '',
  title: '',
  content: '',
  categoryId: '',
  userId: '',
  coverImg: '',
  viewCount: 0,
  likeCount: 0,
  commentCount: 0,
  createTime: '',
  isShow: '1'
});
const form1 = ref();
const editorRef = ref(null)
let editor = null

// 查询参数
const queryParams = reactive({
  pageNum:1,
  pageSize:10,
  title: '',
  categoryId: '',
  userId: '',
  isShow: ''
})

// 表单校验规则
const rules = reactive({
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur', type: 'number' }
  ],
  coverImg: [
    { required: true, message: '请上传封面图', trigger: 'blur' }
  ],
  isShow: [
    { required: true, message: '请选择是否可见', trigger: 'change' }
  ]
});

// 表格数据
const data = reactive({
  total:0,
  tableData: []
})

// 获取分类列表并构建映射表
const getCategoryList = async () => {
  try {
    const res = await listcategory({})
    if (res.code === 200) {
      categoryList.value = res.data || []
      // 构建ID->名称的映射表
      categoryMap.value = {}
      categoryList.value.forEach(item => {
        categoryMap.value[item.id] = item.name
      })
    } else {
      ElMessage.error('获取分类列表失败：' + res.msg)
    }
  } catch (err) {
    ElMessage.error('获取分类接口异常：' + (err.response?.data?.msg || '网络错误'))
  }
}
// 获取用户名（缓存到映射表，避免重复调用接口）
const getUserName = async (userId) => {
  // 先查缓存，有则直接返回
  if (userMap.value[userId]) return userMap.value[userId]

  try {
    const res = await selectone(userId)
    if (res.code === 200 && res.data) {
      // 缓存用户名（假设返回的用户对象中字段是username）
      userMap.value[userId] = res.data.username || '未知用户'
      return userMap.value[userId]
    } else {
      userMap.value[userId] = '未知用户'
      return '未知用户'
    }
  } catch (err) {
    userMap.value[userId] = '获取失败'
    return '获取失败'
  }
}
// 输入用户ID时自动获取用户名
const handleUserIdInput = async () => {
  const userId = queryParams.userId
  if (userId) await getUserName(userId)
}

const getPostList = async () => {
  try {
    const res = await listpost(queryParams);
    data.total = res.data?.total || 0;
    const list = res.data?.list || []

    // 遍历每条数据，先批量获取所有需要的用户名（优化性能）
    const userIdList = [...new Set(list.map(item => item.userId))] // 去重用户ID
    for (const userId of userIdList) {
      if (userId) await getUserName(userId) // 提前缓存用户名
    }

    // 处理数据，添加分类名称+用户名
    data.tableData = list.map(item => {
      return {
        ...item,
        categoryName: categoryMap.value[item.categoryId] || '未知分类',
        // 新增 ↓ 从用户映射表取用户名
        userName: userMap.value[item.userId] || '未知用户'
      }
    })

    if (res.code !== 200) {
      ElMessage.error('获取帖子列表失败：' + res.msg);
    }
  } catch (err) {
    ElMessage.error('接口调用异常：' + (err.response?.data?.msg || '网络错误'));
  }
}

// 导出Excel
const handleExport=()=>{
  download("/post/export",queryParams)
}

// 导入成功回调
const handleImportSuccess=()=>{
  ElMessage.success("成功导入数据");
  getPostList();
}

// 查询按钮
const search = () => {
  queryParams.pageNum = 1;
  getPostList();
}

// 分页页码变化
const handleCurrentChange=(val)=>{
  queryParams.pageNum = val;
  getPostList();
}

// 分页条数变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val;
  queryParams.pageNum = 1;
  getPostList();
};

// 重置查询条件
const list00 = () => {
  queryParams.pageNum = 1;
  queryParams.pageSize =10;
  queryParams.title = '';
  queryParams.categoryId = '';
  queryParams.userId = '';
  queryParams.isShow = '';
  getPostList();
}

// 表格选择事件
const handleSelectionChange = selections => {
  ids.value = selections.map(item => item.id);
}

// 删除操作
const handleDelete = (row) => {
  let deleteId = null;
  if (row) {
    deleteId = row.id;
  } else {
    if (ids.value.length === 0) {
      ElMessage.error("请选择要删除的数据");
      return;
    }
    deleteId = ids.value[0];
  }

  ElMessageBox.confirm(
      `是否删除所选择的帖子`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        deletepost(deleteId)
            .then((res) => {
              ElMessage.success('删除成功');
              getPostList();
            })
            .catch((err) => {
              ElMessage.error('删除失败：' + (err.response?.data?.msg || '网络异常'));
            });
      })
      .catch(() => {});
};

// 添加帖子
const handleAdd = () => {
  title.value = "添加帖子";
  Object.keys(form).forEach(key => {
    form[key] = key === 'isShow' ? '1' : '';
    if (['viewCount','likeCount','commentCount'].includes(key)) {
      form[key] = 0;
    }
  });
  visible.value = true;
  nextTick(() => {
    form1.value?.resetFields();
    if (editor) editor.destroy()
    editor = createEditor({
      selector: editorRef.value,
      html: '',
      placeholder: '请输入帖子内容',
      onChange: (editorIns) => {
        form.content = editorIns.getHtml()
      }
    })
  });
}

// 提交表单
const submitForm = () => {
  form1.value?.validate((isValid) => {
    if (isValid) {
      const submitForm = {
        ...form,
        isShow: Number(form.isShow)
      };
      const request = form.id ? updatepost(submitForm) : addpost(submitForm);
      request.then(res=>{
        ElMessage.success(form.id ? "修改成功" : "添加成功");
        visible.value = false;
        getPostList();
      }).catch(err => {
        ElMessage.error(form.id ? "修改失败" : "添加失败：" + (err.response?.data?.msg || '网络错误'));
      });
    }
  });
};

// 封面图上传成功回调
const coverImgUploadSuccess=(res)=>{
  form.coverImg = res.data;
}

// 编辑帖子
const handleEdit = (row) => {
  title.value = "编辑帖子";
  selectonepost(row.id).then(res => {
    if (res.code === 200 && res.data) {
      form.id = res.data.id || '';
      form.title = res.data.title || '';
      form.content = res.data.content || '';
      form.categoryId = res.data.categoryId || '';
      form.userId = res.data.userId || '';
      form.coverImg = res.data.coverImg || '';
      form.viewCount = res.data.viewCount || 0;
      form.likeCount = res.data.likeCount || 0;
      form.commentCount = res.data.commentCount || 0;
      form.createTime = res.data.createTime || '';
      form.isShow = String(res.data.isShow) || '1';
      visible.value = true;
      nextTick(() => {
        if (editor) editor.destroy()
        editor = createEditor({
          selector: editorRef.value,
          html: form.content,
          placeholder: '请输入帖子内容',
          onChange: (editorIns) => {
            form.content = editorIns.getHtml()
          }
        })
      });
    } else {
      ElMessage.error('获取帖子信息失败');
    }
  }).catch(err => {
    ElMessage.error('获取帖子信息失败：' + (err.response?.data?.msg || '网络异常'));
  });
}

// 初始化编辑器
const initEditor = (content = '') => {
  if (!editorRef.value) return
  if (editor) editor.destroy()
  editor = createEditor({
    selector: editorRef.value,
    html: content,
    placeholder: '请输入帖子内容',
    onChange: (editorIns) => {
      form.content = editorIns.getHtml()
    }
  })
}

// 组件卸载时销毁编辑器
onBeforeUnmount(() => {
  if (editor) editor.destroy()
  editor = null
})

// 初始化加载
onMounted(async () => {
  await getCategoryList()
  getPostList();
})
</script>

<style scoped>
.demo-form-inline .el-input {
  --el-input-width: 220px;
}

.demo-form-inline .el-select {
  --el-select-width: 220px;
}

.button-row {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 5px;
  background-color: white;
  display: flex;
  justify-content: flex-start;
  padding: 10px;
}

.el-tag {
  --el-tag-padding: 2px 8px;
}
</style>