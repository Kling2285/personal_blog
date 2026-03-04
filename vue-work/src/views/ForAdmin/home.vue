<template>
  <div>
    <el-row :gutter="10">
      <el-col :span="8">
        <el-card style="max-width: 480px;">
          <template #header>
            <div class="card-header">
              <span>今日新增用户</span>
            </div>
          </template>
          <p>{{daily_data.data1}}位</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card style="max-width: 480px;">
          <template #header>
            <div class="card-header">
              <span>今日新增帖子数量</span>
            </div>
          </template>
          <p>{{daily_data.data2}}</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card style="max-width: 480px;">
          <template #header>
            <div class="card-header">
              <span>今日发出验证码</span>
            </div>
          </template>
          <p>{{daily_data.data3}}</p>
        </el-card>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <div class="zhuzhuangtu" id="zhuzhuangtu"></div>
      </el-col>
    </el-row>
    <el-row>
      <!-- 修复：span=14 加引号 -->
      <el-col :span="10">
        <div class="category" id="category"></div>
      </el-col>
      <el-col :span="14">
        <div class="email" id="email"></div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import * as echarts from 'echarts';
import { onMounted, ref } from "vue";

const daily_data = ref({
  data1: 7,
  data2: 2,
  data3: 3,
})

onMounted(() => {

  const chartDom1 = document.getElementById('zhuzhuangtu');
  if (chartDom1) {
    const myChart1 = echarts.init(chartDom1);
    const option1 = {
      title: {
        show: true,
        text: '近一个月新增用户'
      },
      xAxis: {
        type: 'category',
        name: '日期',
        data: ['5号', '10号', '15号', '20号', '25号']
      },
      yAxis: {
        type: 'value',
        name: '新增用户数量',
        nameLocation: 'center'
      },
      series: [
        {
          data: [1, 2, 3, 2, 1],
          type: 'bar'
        }
      ]
    };
    myChart1.setOption(option1);
    window.addEventListener('resize', () => myChart1.resize());
  }


  const chartDom2 = document.getElementById('category');
  if (chartDom2) {
    const myChart2 = echarts.init(chartDom2);
    const option2 = {
      title: {
        text: '分类'
      },
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: 'Access From',
          type: 'pie',
          radius: '50%',
          data: [
            { value: 2, name: '穿搭' },
            { value: 1, name: '美食' },
            { value: 3, name: '电影' },
            { value: 1, name: '旅游' },
            { value: 2, name: '游戏' }
          ],
        }
      ]
    };
    myChart2.setOption(option2);
    window.addEventListener('resize', () => myChart2.resize());
  }


  const chartDom3 = document.getElementById('email');
  if (chartDom3) {
    const myChart3 = echarts.init(chartDom3);
    const option3 = {
      title: {
        text: '邮件发送统计'
      },
      xAxis: {
        type: 'category',
        data: ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期天']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: [3, 5, 4, 1, 2, 3, 2],
          type: 'line'
        }
      ]
    };
    myChart3.setOption(option3);
    window.addEventListener('resize', () => myChart3.resize());
  }
})
</script>

<style scoped>
#zhuzhuangtu, #category, #email {
  height: 380px;
  background-color: white;
  margin-top: 10px;
}
</style>