<template>
  <div>
    <div class="box">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="queryParams.nickname" placeholder="请输入用户昵称" clearable />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select
              v-model="queryParams.status"
              placeholder="用户状态"
              clearable
          >
            <el-option label="启用" value="0" />
            <el-option label="禁用" value="1" />
            <el-option label="禁言" value="2" />
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
        <el-table-column type="selection" prop="userId" width="50" />
        <el-table-column prop="userId" label="编号" width="100" />
        <el-table-column prop="avatar" label="头像" width="130" >
          <template #default="scope">
            <el-image :src="scope.row.avatar" style="width:40px"></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column prop="nickname" label="用户昵称" width="180" />
        <!-- 核心修正：状态显示逻辑，严格匹配数值类型+文本 -->
        <el-table-column prop="status" label="用户状态" width="180">
          <template #default="scope">
            <!-- 先统一转字符串，避免数值/字符串类型不匹配问题 -->
            <el-tag
                :type="getStatusTagType(String(scope.row.status))"
            >
              {{ getStatusText(String(scope.row.status)) }}
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" disabled type="password" autocomplete="off" />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickname">
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
        <el-form-item label="用户状态" prop="status">
          <!-- 保持原有蓝色填充，值匹配字符串类型 -->
          <el-radio-group v-model="form.status" size="large" fill="#6cf" autocomplete="off">
            <el-radio-button label="启用" value="0" />
            <el-radio-button label="禁用" value="1" />
            <el-radio-button label="禁言" value="2" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" autocomplete="off" />
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
  listuser,
  deleteuser,
  adduser,
  selectone,
  updateuser
} from '@/api/bloguser.js'
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,CloseBold,Download,Upload,EditPen
} from '@element-plus/icons-vue'
import {download} from '@/utils/request.js'

const uploadUrl=ref(import.meta.env.VITE_API_BASE_URL+'/user/import');
const ids = ref([])
const visible = ref(false);
const title = ref('添加用户');
const form = reactive({
  userId: '',
  username: '',
  password: '',
  nickname:'',
  avatar:'',
  phone: '',
  email: '',
  status: '0', // 默认启用，避免空值
  intro:''
});
const form1 = ref();

const queryParams = reactive({
  pageNum:1,
  pageSize:10,
  username: '',
  nickname:'',
  status:''
})

// 补充状态校验规则，避免提交空值
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '至少六位字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择用户状态', trigger: 'change' }
  ]
});

const data = reactive({
  total:0,
  tableData: []
})

// 核心函数：统一处理状态文本（避免模板内逻辑混乱）
const getStatusText = (status) => {
  switch(status) {
    case '0': return '启用';
    case '1': return '禁用';
    case '2': return '禁言';
    default: return '未知状态';
  }
}

// 核心函数：统一处理状态标签类型
const getStatusTagType = (status) => {
  switch(status) {
    case '0': return 'success'; // 启用-绿色
    case '1': return 'danger';  // 禁用-红色
    case '2': return 'warning'; // 禁言-黄色
    default: return 'info';     // 未知-蓝色
  }
}

// 获取用户列表
const getUserList = async () => {
  try {
    const res = await listuser(queryParams);
    data.total = res.data?.total || 0;
    data.tableData = res.data?.list || [];
    if (res.code !== 200) {
      ElMessage.error('获取用户列表失败：' + res.msg);
    }
  } catch (err) {
    ElMessage.error('接口调用异常：' + (err.response?.data?.msg || '网络错误'));
  }
}

//导出
const handleExport=()=>{
  download("/user/export",queryParams)
}

//导入
const handleImportSuccess=()=>{
  ElMessage.success("成功导入数据");
  getUserList();
}

//查询
const search = () => {
  queryParams.pageNum = 1;
  getUserList();
}

// 分页页码变化
const handleCurrentChange=(val)=>{
  queryParams.pageNum = val;
  getUserList();
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val;
  queryParams.pageNum = 1;
  getUserList();
};

// 重置
const list00 = () => {
  queryParams.pageNum = 1;
  queryParams.pageSize =10;
  queryParams.username = '';
  queryParams.nickname = '';
  queryParams.status = '';
  getUserList();
}

//选择框
const handleSelectionChange = selections => {
  ids.value = selections.map(item => item.userId);
}

//删除
const handleDelete = (row) => {
  let deleteId = null;
  if (row) {
    deleteId = row.userId;
  } else {
    if (ids.value.length === 0) {
      ElMessage.error("请选择要删除的数据");
      return;
    }
    deleteId = ids.value[0];
  }

  ElMessageBox.confirm(
      `是否删除所选择的用户`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        deleteuser(deleteId)
            .then((res) => {
              ElMessage.success('删除成功');
              getUserList();
            })
            .catch((err) => {
              ElMessage.error('删除失败：' + (err.response?.data?.msg || '网络异常'));
            });
      })
      .catch(() => {});
};

//添加用户
const handleAdd = () => {
  title.value = "添加用户";
  // 清空表单，状态默认启用
  Object.keys(form).forEach(key => {
    form[key] = key === 'status' ? '0' : '';
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
      const request = form.userId ? updateuser(form) : adduser(form);
      request.then(res=>{
        ElMessage.success(form.userId ? "修改成功" : "添加成功");
        visible.value = false;
        getUserList();
      }).catch(err => {
        ElMessage.error(form.userId ? "修改失败" : "添加失败：" + (err.response?.data?.msg || '网络错误'));
      });
    }
  });
};

//头像上传成功
const avatarUploadSuccess=(res)=>{
  form.avatar = res.data;
}

//修改
const handleEdit = (row) => {
  title.value = "编辑用户";
  selectone(row.userId).then(res => {
    if (res.code === 200 && res.data) {
      form.userId = res.data.userId || '';
      form.username = res.data.username || '';
      form.password = res.data.password || '';
      form.nickname = res.data.nickname || '';
      form.phone = res.data.phone || '';
      form.email = res.data.email || '';
      form.intro = res.data.intro || '';
      form.status = String(res.data.status) || '0'; // 强制转字符串，避免类型问题
      form.avatar = res.data.avatar || '';
      visible.value = true;
    } else {
      ElMessage.error('获取用户信息失败');
    }
  }).catch(err => {
    ElMessage.error('获取用户信息失败：' + (err.response?.data?.msg || '网络异常'));
  });
}

// 初始化加载列表
getUserList();
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