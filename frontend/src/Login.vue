<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>訂單與幣別轉換系統</h1>
        <p>請登入以繼續</p>
      </div>
      
      <div v-if="showRegister" class="register-form">
        <h2>註冊新帳號</h2>
        <div class="form-group">
          <label>使用者名稱：</label>
          <input 
            type="text" 
            v-model="registerForm.username" 
            placeholder="至少3個字元"
            :disabled="loading"
          />
        </div>
        <div class="form-group">
          <label>電子郵件：</label>
          <input 
            type="email" 
            v-model="registerForm.email" 
            placeholder="example@email.com"
            :disabled="loading"
          />
        </div>
        <div class="form-group">
          <label>密碼：</label>
          <input 
            type="password" 
            v-model="registerForm.password" 
            placeholder="至少6個字元"
            :disabled="loading"
          />
        </div>
        <div class="form-group">
          <label>確認密碼：</label>
          <input 
            type="password" 
            v-model="registerForm.confirmPassword" 
            placeholder="再次輸入密碼"
            :disabled="loading"
            @keyup.enter="handleRegister"
          />
        </div>
        <button 
          class="btn-primary btn-block" 
          @click="handleRegister"
          :disabled="loading"
        >
          {{ loading ? '註冊中...' : '註冊' }}
        </button>
        <button 
          class="btn-secondary btn-block" 
          @click="showRegister = false"
          :disabled="loading"
        >
          返回登入
        </button>
      </div>
      
      <div v-else class="login-form">
        <h2>登入</h2>
        <div class="form-group">
          <label>使用者名稱：</label>
          <input 
            type="text" 
            v-model="loginForm.username" 
            placeholder="輸入使用者名稱"
            :disabled="loading"
            @keyup.enter="handleLogin"
          />
        </div>
        <div class="form-group">
          <label>密碼：</label>
          <input 
            type="password" 
            v-model="loginForm.password" 
            placeholder="輸入密碼"
            :disabled="loading"
            @keyup.enter="handleLogin"
          />
        </div>
        <button 
          class="btn-primary btn-block" 
          @click="handleLogin"
          :disabled="loading"
        >
          {{ loading ? '登入中...' : '登入' }}
        </button>
        <button 
          class="btn-link" 
          @click="showRegister = true"
          :disabled="loading"
        >
          還沒有帳號？立即註冊
        </button>
      </div>
      
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

const API_BASE_URL = '/api'

export default {
  name: 'Login',
  props: {
    onLoginSuccess: {
      type: Function,
      required: true
    }
  },
  data() {
    return {
      showRegister: false,
      loading: false,
      errorMessage: '',
      loginForm: {
        username: '',
        password: ''
      },
      registerForm: {
        username: '',
        email: '',
        password: '',
        confirmPassword: ''
      }
    }
  },
  methods: {
    async handleLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        this.errorMessage = '請輸入使用者名稱和密碼'
        return
      }
      
      this.loading = true
      this.errorMessage = ''
      
      try {
        const response = await axios.post(`${API_BASE_URL}/auth/login`, {
          username: this.loginForm.username,
          password: this.loginForm.password
        })
        
        const token = response.data.token
        if (token) {
          localStorage.setItem('token', token)
          localStorage.setItem('username', response.data.username || loginRequest.username)
          // 設置 axios 預設 header
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
          // 調用父組件的登入成功回調
          this.onLoginSuccess(token, response.data.username || loginRequest.username)
        } else {
          this.errorMessage = '登入失敗：未收到 token'
        }
      } catch (error) {
        console.error('登入失敗:', error)
        if (error.response && error.response.data) {
          this.errorMessage = error.response.data.message || '登入失敗，請檢查使用者名稱和密碼'
        } else {
          this.errorMessage = '網路連線錯誤，請稍後再試'
        }
      } finally {
        this.loading = false
      }
    },
    
    async handleRegister() {
      // 驗證表單
      if (!this.registerForm.username || this.registerForm.username.length < 3) {
        this.errorMessage = '使用者名稱至少需要3個字元'
        return
      }
      
      if (!this.registerForm.email || !this.registerForm.email.includes('@')) {
        this.errorMessage = '請輸入有效的電子郵件地址'
        return
      }
      
      if (!this.registerForm.password || this.registerForm.password.length < 6) {
        this.errorMessage = '密碼至少需要6個字元'
        return
      }
      
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        this.errorMessage = '兩次輸入的密碼不一致'
        return
      }
      
      this.loading = true
      this.errorMessage = ''
      
      try {
        await axios.post(`${API_BASE_URL}/auth/register`, {
          username: this.registerForm.username,
          email: this.registerForm.email,
          password: this.registerForm.password
        })
        
        // 註冊成功後自動登入
        this.loginForm.username = this.registerForm.username
        this.loginForm.password = this.registerForm.password
        this.showRegister = false
        await this.handleLogin()
      } catch (error) {
        console.error('註冊失敗:', error)
        if (error.response && error.response.data) {
          this.errorMessage = error.response.data.message || '註冊失敗，請稍後再試'
        } else {
          this.errorMessage = '網路連線錯誤，請稍後再試'
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #333;
  margin: 0 0 10px 0;
  font-size: 24px;
}

.login-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.login-form h2,
.register-form h2 {
  color: #333;
  margin: 0 0 20px 0;
  font-size: 20px;
  text-align: center;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #667eea;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  width: 100%;
}

.btn-primary:hover:not(:disabled) {
  background-color: #5568d3;
}

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  width: 100%;
  margin-top: 10px;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #5a6268;
}

.btn-link {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 14px;
  text-decoration: underline;
  padding: 10px 0;
  margin-top: 15px;
  display: block;
  width: 100%;
  text-align: center;
}

.btn-link:hover:not(:disabled) {
  color: #5568d3;
}

.btn-block {
  display: block;
  width: 100%;
}

.error-message {
  margin-top: 20px;
  padding: 12px;
  background-color: #fee;
  border: 1px solid #fcc;
  border-radius: 6px;
  color: #c33;
  font-size: 14px;
  text-align: center;
}
</style>

