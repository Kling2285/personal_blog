<template>
  <div>
    <div class="box">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="敏感词">
          <el-input v-model="queryParams.word" placeholder="请输入敏感词" clearable />
        </el-form-item>
        <el-form-item label="敏感词说明">
          <el-input v-model="queryParams.info" placeholder="请输入敏感词说明" clearable />
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
        <el-table-column prop="id" label="编号" width="100" />
        <el-table-column prop="word" label="敏感词" width="180" />
        <el-table-column prop="info" label="敏感词说明" min-width="200" />
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

    <el-dialog v-model="visible" :title="title" width="600px">
      <el-form :model="form" :label-width="85" :rules="rules" ref="form1">
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="form.word" autocomplete="off" placeholder="请输入敏感词" />
        </el-form-item>
        <el-form-item label="敏感词说明">
          <el-input v-model="form.info" autocomplete="off" placeholder="选填，如：涉赌类敏感词" />
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
  listword,
  deleteword,
  addword,
  selectoneword,
  updateword
} from '@/api/word.js' // 导入敏感词接口
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus, CloseBold, Download, Upload, EditPen
} from '@element-plus/icons-vue'
import { download } from '@/utils/request.js'

// 导入接口地址
const uploadUrl = ref(import.meta.env.VITE_API_BASE_URL + '/word/import');
// 批量选择的ID列表
const ids = ref([])
// 弹窗显隐
const visible = ref(false);
// 弹窗标题
const title = ref('添加敏感词');
// 表单数据
const form = reactive({
  id: '',
  word: '',
  info: ''
});
// 表单引用
const form1 = ref();

// 查询参数（无分页）
const queryParams = reactive({
  word: '',
  info: ''
})

// 表单校验规则
const rules = reactive({
  word: [
    { required: true, message: '请输入敏感词', trigger: 'blur' },
    { min: 1, max: 50, message: '敏感词长度1-50个字符', trigger: 'blur' }
  ]
});

// 表格数据
const data = reactive({
  total: 0,
  tableData: []
})

// 获取敏感词列表
const getWordList = async () => {
  try {
    const res = await listword(queryParams);
    data.total = res.data?.total || 0;
    data.tableData = res.data?.list || [];
    if (res.code !== 200) {
      ElMessage.error('获取敏感词列表失败：' + res.msg);
    }
  } catch (err) {
    ElMessage.error('接口调用异常：' + (err.response?.data?.msg || '网络错误'));
  }
}

// 导出敏感词
const handleExport = () => {
  download("/word/export", queryParams)
}

// 导入成功回调
const handleImportSuccess = () => {
  ElMessage.success("成功导入敏感词数据");
  getWordList();
}

// 查询
const search = () => {
  getWordList();
}

// 重置查询条件
const list00 = () => {
  queryParams.word = '';
  queryParams.info = '';
  getWordList();
}

// 选择框变化
const handleSelectionChange = selections => {
  ids.value = selections.map(item => item.id);
}

// 删除敏感词（单条/批量）
const handleDelete = (row) => {
  let deleteId = null;
  if (row) {
    // 单条删除
    deleteId = row.id;
  } else {
    // 批量删除
    if (ids.value.length === 0) {
      ElMessage.error("请选择要删除的敏感词");
      return;
    }
    deleteId = ids.value.join(','); // 批量删除（若后端支持批量，可调整）
  }

  ElMessageBox.confirm(
      `是否删除所选敏感词？`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        // 批量删除需后端支持，这里先按单条处理（可根据后端调整）
        const deletePromise = Array.isArray(deleteId)
            ? Promise.all(deleteId.map(id => deleteword(id)))
            : deleteword(deleteId);

        deletePromise.then(() => {
          ElMessage.success('删除成功');
          getWordList();
        }).catch((err) => {
          ElMessage.error('删除失败：' + (err.response?.data?.msg || '网络异常'));
        });
      })
      .catch(() => { });
};

// 添加敏感词
const handleAdd = () => {
  title.value = "添加敏感词";
  // 清空表单
  Object.keys(form).forEach(key => {
    form[key] = '';
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
      const request = form.id ? updateword(form) : addword(form);
      request.then(res => {
        ElMessage.success(form.id ? "修改成功" : "添加成功");
        visible.value = false;
        getWordList();
      }).catch(err => {
        ElMessage.error(form.id ? "修改失败" : "添加失败：" + (err.response?.data?.msg || '网络错误'));
      });
    }
  });
};

// 编辑敏感词
const handleEdit = (row) => {
  title.value = "编辑敏感词";
  selectoneword(row.id).then(res => {
    if (res.code === 200 && res.data) {
      form.id = res.data.id || '';
      form.word = res.data.word || '';
      form.info = res.data.info || '';
      visible.value = true;
    } else {
      ElMessage.error('获取敏感词信息失败');
    }
  }).catch(err => {
    ElMessage.error('获取敏感词信息失败：' + (err.response?.data?.msg || '网络异常'));
  });
}

// 初始化加载列表
getWordList();
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
</style>