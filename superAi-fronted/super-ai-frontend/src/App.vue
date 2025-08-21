<template>
  <div id="app">
    <!-- 删除任何残留的顶部导航栏 -->
    <!-- 确保只有路由视图 -->
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
/* 全局样式重置 - 确保没有残留样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* 删除任何可能的全局导航样式 */
.global-nav,
.top-nav,
.main-nav,
.app-header,
.app-main {
  display: none !important;
}

/* Windows系统全局字体优化 */
body {
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Windows系统特殊处理 */
@media screen and (-ms-high-contrast: active), (-ms-high-contrast: none) {
  body {
    font-family: 'Microsoft YaHei', 'Segoe UI', Tahoma, sans-serif;
  }
}

/* Windows系统特殊样式 */
body.windows-system {
  font-size: 14px;
  line-height: 1.6;
}
</style>