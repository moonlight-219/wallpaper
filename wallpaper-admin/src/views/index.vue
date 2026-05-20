<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card-wrapper" shadow="hover">
          <div class="stat-card blue">
            <div class="icon-wrapper">
              <el-icon class="icon"><Picture /></el-icon>
            </div>
            <div class="content">
              <div class="label">壁纸总数</div>
              <div class="value">{{ stats.wallpapers }}</div>
              <div class="trend">
                <el-icon color="#67c23a"><CaretTop /></el-icon>
                <span>{{ stats.wallpaperGrowth }}% 较上月</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card-wrapper" shadow="hover">
          <div class="stat-card green">
            <div class="icon-wrapper">
              <el-icon class="icon"><User /></el-icon>
            </div>
            <div class="content">
              <div class="label">创作者</div>
              <div class="value">{{ stats.authors }}</div>
              <div class="trend">
                <el-icon color="#67c23a"><CaretTop /></el-icon>
                <span>{{ stats.authorGrowth }}% 较上月</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card-wrapper" shadow="hover">
          <div class="stat-card orange">
            <div class="icon-wrapper">
              <el-icon class="icon"><Collection /></el-icon>
            </div>
            <div class="content">
              <div class="label">分类</div>
              <div class="value">{{ stats.categories }}</div>
              <div class="trend">
                <el-icon color="#67c23a"><CaretTop /></el-icon>
                <span>{{ stats.categoryGrowth }}% 较上月</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 壁纸类型和热门创作者 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">壁纸类型占比</span>
          </template>
          <div ref="typeChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">热门创作者 TOP 10</span>
              <el-button type="primary" link @click="handleMoreAuthor">查看全部</el-button>
            </div>
          </template>
          <div ref="authorChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 壁纸上传趋势 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">壁纸上传趋势</span>
              <el-radio-group v-model="trendPeriod" size="small">
                <el-radio-button value="week">近7天</el-radio-button>
                <el-radio-button value="month">近30天</el-radio-button>
                <el-radio-button value="year">近一年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据对比分析 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">月度数据对比</span>
          </template>
          <div ref="monthlyCompareChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">分类热度趋势</span>
          </template>
          <div ref="categoryTrendChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import { getDashboardStats, getDashboardData } from '@/api/dashboard'

const router = useRouter()

const stats = ref({
  wallpapers: 0,
  authors: 0,
  categories: 0
})

const trendPeriod = ref('week')
const uploadTrendData = ref(null)

const trendChart = ref(null)
const typeChart = ref(null)
const authorChart = ref(null)
const monthlyCompareChart = ref(null)
const categoryTrendChart = ref(null)

let trendChartInstance = null
let typeChartInstance = null
let authorChartInstance = null
let monthlyCompareChartInstance = null
let categoryTrendChartInstance = null

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadDashboardData = async () => {
  try {
    const res = await getDashboardData()
    if (res.code === 200) {
      const data = res.data
      uploadTrendData.value = data.uploadTrend
      
      updateTypeChart(data.typeDistribution)
      updateAuthorChart(data.topAuthors)
      updateTrendChartWithData(data.uploadTrend)
      updateMonthlyCompareChart(data.monthlyCompare)
      updateCategoryTrendChart(data.categoryTrend)
    }
  } catch (error) {
    console.error('加载图表数据失败:', error)
  }
}

const initTrendChart = () => {
  if (!trendChart.value) return
  
  trendChartInstance = echarts.init(trendChart.value)
}

const initTypeChart = () => {
  if (!typeChart.value) return
  
  typeChartInstance = echarts.init(typeChart.value)
}

const initAuthorChart = () => {
  if (!authorChart.value) return
  
  authorChartInstance = echarts.init(authorChart.value)
}

const initMonthlyCompareChart = () => {
  if (!monthlyCompareChart.value) return
  
  monthlyCompareChartInstance = echarts.init(monthlyCompareChart.value)
}

const initCategoryTrendChart = () => {
  if (!categoryTrendChart.value) return
  
  categoryTrendChartInstance = echarts.init(categoryTrendChart.value)
}

const updateTrendChartWithData = (uploadTrend) => {
  const period = trendPeriod.value
  const data = uploadTrend[period]
  
  if (!data) return

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        const data = params[0]
        return `<div style="padding: 12px;">
          <div style="font-size: 14px; font-weight: bold; margin-bottom: 8px; color: #303133;">
            ${data.name}
          </div>
          <div style="font-size: 13px; color: #606266;">
            上传数量: <span style="color: #409eff; font-weight: bold; font-size: 16px;">${data.value}</span>
          </div>
        </div>`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.dates,
      axisTick: {
        alignWithLabel: true
      },
      axisLabel: {
        interval: period === 'month' ? 0 : 0,
        rotate: period === 'month' ? 45 : 0,
        fontSize: 12,
        color: '#606266'
      },
      axisLine: {
        lineStyle: {
          color: '#e4e7ed'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '数量',
      nameTextStyle: {
        padding: [0, 0, 0, 10],
        fontSize: 12,
        color: '#909399'
      },
      axisLabel: {
        fontSize: 12,
        color: '#606266'
      },
      splitLine: {
        lineStyle: {
          color: '#f5f7fa',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '上传数量',
        type: 'bar',
        barWidth: '50%',
        data: data.values,
        itemStyle: {
          color: '#409eff',
          borderRadius: [4, 4, 0, 0]
        },
        label: {
          show: false
        },
        emphasis: {
          itemStyle: {
            color: '#66b1ff',
            shadowBlur: 10,
            shadowColor: 'rgba(64, 158, 255, 0.5)'
          }
        },
        animationDuration: 1000,
        animationEasing: 'cubicOut'
      }
    ]
  }
  
  trendChartInstance.setOption(option)
}


const updateTypeChart = (typeDistribution) => {
  if (!typeChart.value) return
  
  typeChartInstance = echarts.init(typeChart.value)  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: '5%',
      top: 'center',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '壁纸类型',
        type: 'pie',
        radius: '65%',
        center: ['60%', '50%'],
        data: typeDistribution || [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          },
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        }
      }
    ]
  }
  
  typeChartInstance.setOption(option)
}

const updateAuthorChart = (topAuthors) => {
  if (!authorChart.value) return
  
  authorChartInstance = echarts.init(authorChart.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      show: true,
      enterable: false,
      confine: true,
      formatter: function(params) {
        const data = params[0]
        return `<div style="padding: 12px;">
          <div style="font-size: 14px; font-weight: bold; margin-bottom: 8px; color: #303133;">
            ${data.name}
          </div>
          <div style="font-size: 13px; color: #606266;">
            作品数量: <span style="color: #409eff; font-weight: bold; font-size: 16px;">${data.value}</span>
          </div>
        </div>`
      }
    },
    grid: {
      left: '5%',
      right: '5%',
      top: '10%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: topAuthors && topAuthors.length > 0 ? topAuthors.map(a => a.name) : ['暂无数据'],
      axisLabel: {
        interval: 0,
        fontSize: 12,
        color: '#606266'
      },
      axisLine: {
        lineStyle: {
          color: '#e4e7ed'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '作品数量',
      nameTextStyle: {
        padding: [0, 0, 0, 10],
        fontSize: 12,
        color: '#909399'
      },
      axisLabel: {
        fontSize: 12,
        color: '#606266'
      },
      splitLine: {
        lineStyle: {
          color: '#f5f7fa',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '作品数量',
        type: 'line',
        smooth: true,
        data: topAuthors && topAuthors.length > 0 ? topAuthors.map(a => a.count) : [0],
        itemStyle: {
          color: '#409eff'
        },
        lineStyle: {
          width: 3,
          color: '#409eff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        symbol: 'circle',
        symbolSize: 8,
        emphasis: {
          itemStyle: {
            color: '#66b1ff',
            borderColor: '#fff',
            borderWidth: 2,
            shadowBlur: 10,
            shadowColor: 'rgba(64, 158, 255, 0.5)'
          },
          symbolSize: 12
        },
        animationDuration: 1000,
        animationEasing: 'cubicOut'
      }
    ]
  }
  
  authorChartInstance.setOption(option)
  
  authorChartInstance.off('click')
  authorChartInstance.on('click', function(params) {
    const topAuthors = authorChartInstance.getOption().xAxis[0].data
    const authorId = params.dataIndex + 1
    router.push({
      name: 'AuthorDetail',
      params: { id: authorId }
    })
  })
}

const updateMonthlyCompareChart = (monthlyCompare) => {
  if (!monthlyCompareChart.value) return
  
  monthlyCompareChartInstance = echarts.init(monthlyCompareChart.value)  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['本月', '上月'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      axisLabel: {
        interval: 0,
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      name: '数量',
      nameTextStyle: {
        padding: [0, 0, 0, 10]
      }
    },
    series: [
      {
        name: '本月',
        type: 'line',
        data: monthlyCompare && monthlyCompare.current ? monthlyCompare.current : [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        smooth: true,
        itemStyle: { color: '#5470c6' },
        symbol: 'circle',
        symbolSize: 6
      },
      {
        name: '上月',
        type: 'line',
        data: monthlyCompare && monthlyCompare.previous ? monthlyCompare.previous : [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        smooth: true,
        itemStyle: { color: '#91cc75' },
        symbol: 'circle',
        symbolSize: 6
      }
    ]
  }
  
  monthlyCompareChartInstance.setOption(option)
}

const updateCategoryTrendChart = (categoryTrend) => {
  if (!categoryTrendChart.value) return
  
  categoryTrendChartInstance = echarts.init(categoryTrendChart.value)  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: categoryTrend && categoryTrend.length > 0 ? categoryTrend.map(c => c.name) : ['暂无数据'],
      top: 10,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: {
        fontSize: 12
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      axisLabel: {
        interval: 0,
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      name: '热度',
      nameTextStyle: {
        padding: [0, 0, 0, 10]
      }
    },
    series: categoryTrend && categoryTrend.length > 0 ? categoryTrend.map(trend => ({
      name: trend.name,
      type: 'line',
      data: trend.data || [],
      smooth: true,
      itemStyle: { color: getCategoryColor(trend.name) },
      symbol: 'circle',
      symbolSize: 5
    })) : [{
      name: '暂无数据',
      type: 'line',
      data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      smooth: true,
      itemStyle: { color: '#409eff' },
      symbol: 'circle',
      symbolSize: 5
    }]
  }
  
  categoryTrendChartInstance.setOption(option)
}

const getCategoryColor = (name) => {
  const colors = {
    '风景': '#5470c6',
    '动漫': '#91cc75',
    '明星': '#fac858',
    '游戏': '#ee6666',
    '其他': '#73c0de'
  }
  return colors[name] || '#409eff'
}

const handleMoreAuthor = () => {
  router.push({ name: 'Authors' })
}

watch(trendPeriod, () => {
  if (uploadTrendData.value) {
    updateTrendChartWithData(uploadTrendData.value)
  }
})

onMounted(async () => {
  await loadStats()
  
  initTrendChart()
  initTypeChart()
  initAuthorChart()
  initMonthlyCompareChart()
  initCategoryTrendChart()
  
  await loadDashboardData()
  
  window.addEventListener('resize', () => {
    trendChartInstance?.resize()
    typeChartInstance?.resize()
    authorChartInstance?.resize()
    monthlyCompareChartInstance?.resize()
    categoryTrendChartInstance?.resize()
  })
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card-wrapper {
  transition: all 0.3s;
}

.stat-card-wrapper:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
}

.stat-card.blue .icon-wrapper {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.green .icon-wrapper {
  background: linear-gradient(135deg, #42b983 0%, #0d9488 100%);
}

.stat-card.orange .icon-wrapper {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
}

.icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.icon {
  font-size: 28px;
  color: #fff;
}

.content {
  flex: 1;
}

.label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 5px;
}

.value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.trend {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #67c23a;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart {
  width: 100%;
  height: 350px;
}

:deep(.el-card__header) {
  padding: 18px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-button--primary.is-link) {
  color: #409eff;
  font-weight: 500;
}

:deep(.el-button--primary.is-link:hover) {
  color: #66b1ff;
}

:deep(.el-radio-button__inner) {
  padding: 8px 15px;
}
</style>
