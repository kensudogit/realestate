<template>
  <div id="app">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>不動産管理システム</h1>
          <el-menu
            :default-active="$route.path"
            mode="horizontal"
            router
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
          >
            <el-menu-item index="/">ホーム</el-menu-item>
            <el-menu-item index="/properties">物件管理</el-menu-item>
            <el-menu-item index="/clients">顧客管理</el-menu-item>
            <el-menu-item index="/contracts">契約管理</el-menu-item>
            <el-menu-item index="/transactions">取引管理</el-menu-item>
          </el-menu>
        </div>
      </el-header>
      
      <el-main>
        <router-view />
      </el-main>
      
      <el-footer>
        <p>&copy; 2024 不動産管理システム. All rights reserved.</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<style scoped>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.el-header {
  background-color: #545c64;
  color: white;
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

.el-main {
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.el-footer {
  text-align: center;
  padding: 20px;
  background-color: #f5f5f5;
  color: #666;
}
</style>
