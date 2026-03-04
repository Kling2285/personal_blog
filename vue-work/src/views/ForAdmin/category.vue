<template>
  <div>
    <div class="box">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="分类名称">
          <el-input v-model="queryParams.name" placeholder="请输入分类名称" clearable />
        </el-form-item>
        <el-form-item label="是否显示">
          <el-select
              v-model="queryParams.isShow"
              placeholder="是否显示"
              clearable
          >
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
      <el-table :data="data.tableData" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" prop="id" width="50" />
        <el-table-column prop="id" label="分类ID" width="100" />
        <el-table-column prop="name" label="分类名称" width="180" />
        <el-table-column prop="postCount" label="帖子数量" width="180" />
        <el-table-column prop="isShow" label="是否显示" width="180">
          <template #default="scope">
            <el-tag :type="scope.row.isShow === 1 ? 'success' : 'danger'">
              {{ scope.row.isShow === 1 ? '显示' : '隐藏' }}
            </el-tag>
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

    <el-dialog v-model="visible" :title="title" width="500px">
      <el-form :model="form" :label-width="85" :rules="rules" ref="form1">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="是否显示" prop="isShow">
          <el-radio-group v-model="form.isShow" size="large" fill="#6cf">
            <el-radio-button label="显示" value="1" />
            <el-radio-button label="隐藏" value="0" />
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick } from 'vue'
import {
  listcategory,
  deletecategory,
  addcategory,
  selectonecategory,
  updatecategory
} from '@/api/blogcategory.js'
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,CloseBold,Download,Upload,EditPen
} from '@element-plus/icons-vue'
import {download} from '@/utils/request.js'

// 导入接口地址
const uploadUrl=ref(import.meta.env.VITE_API_BASE_URL+'/category/import');
// 批量选择的ID集合
const ids = ref([])
// 弹窗显隐
const visible = ref(false);
// 弹窗标题
const title = ref('添加分类');
// 表单数据
const form = reactive({
  id: '',
  name: '',
  postCount: 0, // 帖子数量后端自动初始化，前端仅展示
  isShow: '1' // 默认显示
});
// 表单校验ref
const form1 = ref();

// 查询参数（无分页）
const queryParams = reactive({
  name: '',
  isShow: ''
})

// 表单校验规则
const rules = reactive({
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度1-50个字符', trigger: 'blur' }
  ],
  isShow: [
    { required: true, message: '请选择是否显示', trigger: 'change' }
  ]
});

// 表格数据
const data = reactive({
  tableData: []
})

// 获取分类列表（核心方法）
const getCategoryList = async () => {
  try {
    const res = await listcategory(queryParams);
    // 分类列表无分页，直接赋值
    data.tableData = res.data || [];
    if (res.code !== 200) {
      ElMessage.error('获取分类列表失败：' + res.msg);
    }
  } catch (err) {
    ElMessage.error('接口调用异常：' + (err.response?.data?.msg || '网络错误'));
  }
}

// 导出Excel
const handleExport=()=>{
  download("/category/export", queryParams)
}

// 导入Excel成功回调
const handleImportSuccess=()=>{
  ElMessage.success("成功导入数据");
  getCategoryList();
}

// 查询
const search = () => {
  getCategoryList();
}

// 重置查询条件
const list00 = () => {
  queryParams.name = '';
  queryParams.isShow = '';
  getCategoryList();
}

// 表格选择事件
const handleSelectionChange = selections => {
  ids.value = selections.map(item => item.id);
}

// 删除分类（单个/批量）
const handleDelete = (row) => {
  let deleteId = null;
  if (row) {
    // 单个删除
    deleteId = row.id;
  } else {
    // 批量删除
    if (ids.value.length === 0) {
      ElMessage.error("请选择要删除的数据");
      return;
    }
    // 批量删除可扩展为批量接口，这里先单删（后端若支持批量可改）
    deleteId = ids.value[0];
  }

  ElMessageBox.confirm(
      `是否删除所选择的分类`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        deletecategory(deleteId)
            .then((res) => {
              ElMessage.success('删除成功');
              getCategoryList();
            })
            .catch((err) => {
              ElMessage.error('删除失败：' + (err.response?.data?.msg || '网络异常'));
            });
      })
      .catch(() => {});
};

// 添加分类（完全复刻用户管理页面handleAdd逻辑）
const handleAdd = () => {
  title.value = "添加分类";
  // 清空表单，isShow默认显示（和用户管理页面status默认0逻辑完全一致）
  Object.keys(form).forEach(key => {
    form[key] = key === 'isShow' ? '1' : '';
    // 单独处理postCount数字字段（保持默认0，和用户管理页面无此字段逻辑一致）
    if (key === 'postCount') {
      form[key] = 0;
    }
  });
  visible.value = true;
  nextTick(() => {
    form1.value?.resetFields();
  });
}

// 提交表单（新增/修改）
const submitForm = () => {
  form1.value?.validate((isValid) => {
    if (isValid) {
      // 区分新增/修改
      const request = form.id ? updatecategory(form) : addcategory(form);
      request.then(res=>{
        ElMessage.success(form.id ? "修改成功" : "添加成功");
        visible.value = false;
        getCategoryList();
      }).catch(err => {
        ElMessage.error(form.id ? "修改失败" : "添加失败：" + (err.response?.data?.msg || '网络错误'));
      });
    }
  });
};

// 编辑分类（完全复刻用户管理页面handleEdit逻辑）
const handleEdit = (row) => {
  title.value = "编辑分类";
  selectonecategory(row.id).then(res => {
    if (res.code === 200 && res.data) {
      // 逐个赋值（和用户管理页面编辑逻辑完全一致）
      form.id = res.data.id || '';
      form.name = res.data.name || '';
      form.postCount = res.data.postCount || 0;
      form.isShow = String(res.data.isShow) || '1'; // 强制转字符串（和用户管理页面status处理一致）
      visible.value = true;
    } else {
      ElMessage.error('获取分类信息失败');
    }
  }).catch(err => {
    ElMessage.error('获取分类信息失败：' + (err.response?.data?.msg || '网络异常'));
  });
}

// 初始化加载列表
getCategoryList();
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