<template>
  <div>
    <div class="box">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="公告标题">
          <el-input v-model="queryParams.title" placeholder="请输入公告标题" clearable />
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
        <el-table-column prop="title" label="公告标题" width="180" />
        <el-table-column prop="content" label="公告正文" width="280" />
        <el-table-column prop="publishTime" label="发布时间" width="180" />
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

    <el-dialog v-model="visible" :title="title" width="700">
      <el-form :model="form" :label-width="85" :rules="rules" ref="form1">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" autocomplete="off" />
        </el-form-item>
        <el-form-item label="公告正文" prop="content">
          <el-input
              v-model="form.content"
              type="textarea"
              :rows="8"
              autocomplete="off"
              style="width: 100%;"
          />
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
import { reactive, ref, nextTick } from 'vue'
import {
  listnotice,
  deletenotice,
  addnotice,
  selectonenotice,
  updatenotice
} from '@/api/notice.js'
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, CloseBold, Download, Upload, EditPen } from '@element-plus/icons-vue'
import { download } from '@/utils/request.js'

const uploadUrl = ref(import.meta.env.VITE_API_BASE_URL + '/notice/import');
const ids = ref([])
const visible = ref(false);
const title = ref('添加公告');
const form = reactive({
  id: '',
  title: '',
  content: '',
  publishTime: '',
});
const form1 = ref();

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
})

// 补充校验规则
const rules = reactive({
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告正文', trigger: 'blur' }
  ]
});

const data = reactive({
  total: 0,
  tableData: []
})

// 获取公告列表
const getNoticeList = async () => {
  try {
    const res = await listnotice(queryParams);
    data.total = res.data?.total || 0;
    data.tableData = res.data?.list || [];
    if (res.code !== 200) {
      ElMessage.error('获取公告列表失败：' + res.msg);
    }
  } catch (err) {
    ElMessage.error('接口调用异常：' + (err.response?.data?.msg || '网络错误'));
  }
}

//导出
const handleExport = () => {
  download("/notice/export", queryParams)
}

//导入
const handleImportSuccess = () => {
  ElMessage.success("成功导入数据");
  getNoticeList();
}

//查询
const search = () => {
  queryParams.pageNum = 1;
  getNoticeList();
}

// 分页页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val;
  getNoticeList();
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val;
  queryParams.pageNum = 1;
  getNoticeList();
};

// 重置
const list00 = () => {
  queryParams.pageNum = 1;
  queryParams.pageSize = 10;
  queryParams.title = '';
  getNoticeList();
}

//选择框
const handleSelectionChange = (selections) => {
  ids.value = selections.map(item => item.id);
}

//删除
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
      `是否删除所选择的公告`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        deletenotice(deleteId)
            .then((res) => {
              ElMessage.success('删除成功');
              getNoticeList();
            })
            .catch((err) => {
              ElMessage.error('删除失败：' + (err.response?.data?.msg || '网络异常'));
            });
      })
      .catch(() => { });
};

//添加公告
const handleAdd = () => {
  title.value = "添加公告";
  // 清空表单
  Object.keys(form).forEach(key => {
    form[key] = '';
  });
  visible.value = true;
  nextTick(() => {
    form1.value?.resetFields();
  });
}

//提交表单
const submitForm = () => {
  form1.value?.validate((isValid) => {
    if (isValid) {
      const request = form.id ? updatenotice(form) : addnotice(form);
      request.then(res => {
        ElMessage.success(form.id ? "修改成功" : "添加成功");
        visible.value = false;
        getNoticeList();
      }).catch(err => {
        ElMessage.error(form.id ? "修改失败" : "添加失败：" + (err.response?.data?.msg || '网络错误'));
      });
    }
  });
};

//修改
const handleEdit = (row) => {
  title.value = "编辑公告";
  selectonenotice(row.id).then(res => {
    if (res.code === 200 && res.data) {
      form.id = res.data.id || '';
      form.title = res.data.title || '';
      form.content = res.data.content || '';
      form.publishTime = res.data.publishTime || '';
      visible.value = true;
    } else {
      ElMessage.error('获取公告信息失败');
    }
  }).catch(err => {
    ElMessage.error('获取公告信息失败：' + (err.response?.data?.msg || '网络异常'));
  });
}

// 初始化加载列表
getNoticeList();
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