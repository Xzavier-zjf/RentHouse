<template>
  <div class="home">
    <el-card class="welcome-card">
      <h2>欢迎来到极简租房网站</h2>
      <p>请登录或注册以开始使用我们的服务</p>
      <div class="actions">
        <el-button type="primary" @click="$router.push('/login')">用户登录</el-button>
        <el-button type="success" @click="$router.push('/register')">用户注册</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Home'
}
</script>

<style scoped>
.home {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.welcome-card {
  width: 400px;
  text-align: center;
}

.actions {
  margin-top: 20px;
  display: flex;
  justify-content: space-around;
}
</style>