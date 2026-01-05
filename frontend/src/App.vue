<template>
  <div id="app">
    <!-- 登入頁面 -->
    <Login v-if="!isAuthenticated" :onLoginSuccess="handleLoginSuccess" />
    
    <!-- 主應用程式 -->
    <div v-else class="app-container">
      <!-- 左側選單 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h1>訂單與幣別轉換系統</h1>
          <div class="user-info">
            <span class="username">{{ currentUsername }}</span>
            <button class="btn-logout" @click="handleLogout">登出</button>
          </div>
        </div>
        <nav class="sidebar-menu">
          <div 
            v-for="menuItem in menuItems" 
            :key="menuItem.id"
            class="menu-item" 
            :class="{ active: currentPage === menuItem.route }"
            @click="changePage(menuItem.route)"
          >
            <span class="menu-icon">{{ menuItem.icon }}</span>
            <span class="menu-text">{{ menuItem.label }}</span>
          </div>
        </nav>
      </div>

      <!-- 確認對話框 -->
      <div v-if="confirmDialog.show" class="modal" @click.self="cancelConfirm">
        <div class="modal-content" style="max-width: 400px;">
          <h3 style="margin-top: 0; margin-bottom: 15px;">{{ confirmDialog.title }}</h3>
          <p style="margin-bottom: 20px; color: #666;">{{ confirmDialog.message }}</p>
          <div style="display: flex; gap: 10px; justify-content: flex-end;">
            <button class="btn-secondary" @click="cancelConfirm">取消</button>
            <button class="btn-danger" @click="executeConfirm">確定</button>
          </div>
        </div>
      </div>

      <!-- 訊息通知 -->
      <div v-if="notification.show" :class="['notification', notification.type]" @click="closeNotification">
        <div class="notification-content">
          <span class="notification-icon">
            <span v-if="notification.type === 'success'">✓</span>
            <span v-else-if="notification.type === 'error'">✕</span>
            <span v-else>ℹ</span>
          </span>
          <div class="notification-message">
            <div class="notification-title">{{ notification.title }}</div>
            <div v-if="notification.message" class="notification-detail">{{ notification.message }}</div>
            <div v-if="notification.fieldErrors && notification.fieldErrors.length > 0" class="notification-field-errors">
              <ul>
                <li v-for="(fieldError, index) in notification.fieldErrors" :key="index">
                  <strong>{{ fieldError.field }}:</strong> {{ fieldError.message }}
                </li>
              </ul>
            </div>
          </div>
          <span class="notification-close" @click.stop="closeNotification">&times;</span>
        </div>
      </div>

      <!-- 主內容區 -->
      <div class="main-content">
        <!-- 幣別轉換頁面 -->
        <div v-if="currentPage === 'currency'" class="card">
          <h2>幣別換算</h2>
          <div class="form-group">
            <label>金額：</label>
            <input type="number" v-model.number="convertAmount" placeholder="輸入金額" step="0.01" />
          </div>
          <div class="form-group">
            <label>來源幣別：</label>
            <select v-model="sourceCurrency">
              <option v-for="currency in currencies" :key="currency.currencyCode" :value="currency.currencyCode">
                {{ currency.currencyCode }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>目標幣別：</label>
            <select v-model="targetCurrency">
              <option v-for="currency in currencies" :key="currency.currencyCode" :value="currency.currencyCode">
                {{ currency.currencyCode }}
              </option>
            </select>
          </div>
          <button class="btn-primary" @click="convertCurrency">換算</button>
          <div v-if="convertedResult !== null" style="margin-top: 15px; padding: 10px; background-color: #e8f5e9; border-radius: 4px;">
            <strong>換算結果：{{ convertedResult.toFixed(2) }} {{ targetCurrency }}</strong>
          </div>
        </div>

        <!-- 匯率管理頁面 -->
        <div v-if="currentPage === 'rates'" class="card">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h2>匯率管理</h2>
            <div style="display: flex; gap: 10px; align-items: center;">
              <div style="display: flex; align-items: center; gap: 8px; padding: 8px 12px; background-color: #f0f0f0; border-radius: 4px;">
                <label style="display: flex; align-items: center; gap: 8px; cursor: pointer; margin: 0;">
                  <input 
                    type="checkbox" 
                    v-model="autoUpdateEnabled"
                    @change="toggleAutoUpdate"
                    style="width: 18px; height: 18px; cursor: pointer;"
                  />
                  <span style="font-weight: 600;">自動更新匯率</span>
                </label>
              </div>
              <button class="btn-success" @click="refreshRatesFromApi">從 API 更新匯率</button>
            </div>
          </div>
          
          <div style="margin-bottom: 15px; padding: 10px; background-color: #e8f4f8; border-radius: 4px;">
            <strong>說明：</strong>
            <ul style="margin: 5px 0; padding-left: 20px;">
              <li v-if="autoUpdateEnabled">系統會每小時自動從 ExchangeRate-API 更新匯率（已啟用）</li>
              <li v-else>自動更新已停用，請手動點擊「從 API 更新匯率」按鈕更新</li>
              <li>您也可以手動點擊「從 API 更新匯率」按鈕立即更新</li>
              <li>編輯匯率後會自動更新到資料庫和 Redis 快取</li>
            </ul>
          </div>
          
          <table>
            <thead>
              <tr>
                <th>幣別代碼</th>
                <th>對 TWD 匯率</th>
                <th>最後更新時間</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="currency in currencies" :key="currency.currencyCode">
                <td><strong>{{ currency.currencyCode }}</strong></td>
                <td>
                  <span v-if="!editingRates[currency.currencyCode]">
                    {{ currency.rateToTwd ? currency.rateToTwd.toFixed(6) : '-' }}
                  </span>
                  <input 
                    v-else
                    type="number" 
                    v-model.number="editingRates[currency.currencyCode]" 
                    step="0.000001"
                    min="0"
                    style="width: 150px; padding: 5px;"
                    @keyup.enter="saveRate(currency.currencyCode)"
                  />
                </td>
                <td>
                  {{ currency.lastUpdate ? formatDateTime(currency.lastUpdate) : '尚未更新' }}
                </td>
                <td>
                  <span v-if="!editingRates[currency.currencyCode]">
                    <button class="btn-primary" @click="startEditRate(currency)">編輯</button>
                  </span>
                  <span v-else>
                    <button class="btn-success" @click="saveRate(currency.currencyCode)">儲存</button>
                    <button class="btn-secondary" @click="cancelEditRate(currency.currencyCode)">取消</button>
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 訂單列表頁面 -->
        <div v-if="currentPage === 'orders'" class="card">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h2>訂單列表</h2>
            <button class="btn-success" @click="openAddModal">新增訂單</button>
          </div>
          
          <div class="form-group" style="margin-bottom: 15px;">
            <label>搜尋訂單ID：</label>
            <input 
              type="text" 
              v-model="searchOrderId" 
              @input="debounceSearch"
              placeholder="輸入訂單ID進行搜尋（後端搜尋，支援大量資料）" 
              style="width: 400px;"
            />
            <button class="btn-secondary" @click="clearSearch" style="margin-left: 10px;">清除</button>
          </div>
          
          <table>
            <thead>
              <tr>
                <th>訂單ID</th>
                <th>使用者名稱</th>
                <th>金額</th>
                <th>幣別</th>
                <th>折扣 (%)</th>
                <th>最終金額</th>
                <th>狀態</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in orders" :key="order.orderId">
                <td>{{ order.orderId }}</td>
                <td>{{ order.username }}</td>
                <td>{{ order.amount.toFixed(2) }}</td>
                <td>{{ order.currency }}</td>
                <td>{{ order.discount ? order.discount.toFixed(2) : '0.00' }}</td>
                <td>{{ order.finalAmount ? order.finalAmount.toFixed(2) : '-' }}</td>
                <td>{{ order.status }}</td>
                <td>
                  <button class="btn-primary" @click="openEditModal(order)">編輯</button>
                  <button class="btn-danger" @click="deleteOrder(order.orderId)">刪除</button>
                  <button class="btn-secondary" @click="convertOrderToTwd(order.orderId)">轉{{ CurrencyCodes.TWD }}</button>
                </td>
              </tr>
            </tbody>
          </table>
          
          <!-- 分頁控制元件 -->
          <div v-if="totalPages > 1" class="pagination" style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; gap: 15px; align-items: center;">
              <div style="display: flex; align-items: center; gap: 8px;">
                <label style="margin: 0; color: #666;">每頁顯示：</label>
                <select 
                  v-model.number="pageSize" 
                  @change="onPageSizeChange"
                  style="padding: 6px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;"
                >
                  <option :value="10">10 筆</option>
                  <option :value="20">20 筆</option>
                  <option :value="50">50 筆</option>
                </select>
              </div>
              <div style="color: #666;">
                顯示第 {{ (currentPageNumber * pageSize) + 1 }} - {{ Math.min((currentPageNumber + 1) * pageSize, totalElements) }} 筆，共 {{ totalElements }} 筆
              </div>
            </div>
            <div style="display: flex; gap: 10px; align-items: center;">
              <button 
                class="btn-secondary" 
                @click="changePageNumber(currentPageNumber - 1)"
                :disabled="currentPageNumber === 0"
                :style="currentPageNumber === 0 ? 'padding: 8px 16px; opacity: 0.5; cursor: not-allowed;' : 'padding: 8px 16px;'"
              >
                上一頁
              </button>
              <span style="font-weight: 600;">
                第 {{ currentPageNumber + 1 }} / {{ totalPages }} 頁
              </span>
              <button 
                class="btn-secondary" 
                @click="changePageNumber(currentPageNumber + 1)"
                :disabled="currentPageNumber >= totalPages - 1"
                :style="currentPageNumber >= totalPages - 1 ? 'padding: 8px 16px; opacity: 0.5; cursor: not-allowed;' : 'padding: 8px 16px;'"
              >
                下一頁
              </button>
            </div>
          </div>
          <div v-else-if="totalElements > 0" style="margin-top: 20px; display: flex; gap: 15px; align-items: center;">
            <div style="display: flex; align-items: center; gap: 8px;">
              <label style="margin: 0; color: #666;">每頁顯示：</label>
              <select 
                v-model.number="pageSize" 
                @change="onPageSizeChange"
                style="padding: 6px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;"
              >
                <option :value="10">10 筆</option>
                <option :value="20">20 筆</option>
                <option :value="50">50 筆</option>
              </select>
            </div>
            <div style="color: #666;">
              共 {{ totalElements }} 筆資料
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/編輯訂單 Modal -->
    <div v-if="showModal" class="modal" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditMode ? '編輯訂單' : '新增訂單' }}</h2>
          <span class="close" @click="closeModal">&times;</span>
        </div>
        
        <div class="modal-body">
          <Form @submit="onSubmit" :validation-schema="typedOrderSchema" v-slot="{ errors, meta, values, resetForm }">
          <!-- 表單狀態指示器 -->
          <div class="form-status-indicator" :class="{ 'valid': meta.valid && meta.touched, 'invalid': !meta.valid && meta.touched }">
            <span v-if="meta.valid && meta.touched" class="status-icon">✓</span>
            <span v-else-if="!meta.valid && meta.touched" class="status-icon">⚠</span>
            <span class="status-text">
              <span v-if="meta.valid && meta.touched">表單驗證通過</span>
              <span v-else-if="!meta.valid && meta.touched">請檢查表單欄位</span>
              <span v-else>請填寫表單</span>
            </span>
          </div>

          <div class="form-group">
            <label>使用者名稱：<span class="required">*</span></label>
            <div class="input-wrapper">
              <Field 
                name="username" 
                type="text" 
                v-model="currentOrder.username"
                :disabled="!isAdmin && !isEditMode"
                :readonly="!isAdmin && !isEditMode"
                :class="{ 'error': errors.username, 'success': !errors.username && meta.touched && values.username }"
                placeholder="輸入使用者名稱"
                @input="filterUsernameSuggestions"
              />
              <span v-if="!errors.username && meta.touched && values.username" class="input-icon success-icon">✓</span>
              <span v-if="errors.username" class="input-icon error-icon">✕</span>
            </div>
            <ErrorMessage name="username" class="error-message" />
            <!-- 使用者名稱建議 -->
            <div v-if="usernameSuggestions.length > 0 && !currentOrder.username" class="suggestions-box">
              <div 
                v-for="suggestion in usernameSuggestions" 
                :key="suggestion"
                class="suggestion-item"
                @click="selectUsername(suggestion)"
              >
                {{ suggestion }}
              </div>
            </div>
            <small v-if="!isAdmin && !isEditMode" style="color: #666; display: block; margin-top: 5px;">
              將使用您的帳號：{{ currentUsername }}
            </small>
          </div>
          
          <div class="form-group">
            <label>金額：<span class="required">*</span></label>
            <div class="input-wrapper">
              <Field 
                name="amount" 
                type="number" 
                v-model.number="currentOrder.amount"
                @input="handleAmountInput"
                :class="{ 'error': errors.amount, 'success': !errors.amount && meta.touched && currentOrder.amount }"
                placeholder="0.00"
                step="0.01"
              />
              <span v-if="!errors.amount && meta.touched && currentOrder.amount" class="input-icon success-icon">✓</span>
              <span v-if="errors.amount" class="input-icon error-icon">✕</span>
            </div>
            <div v-if="currentOrder.amount" class="formatted-amount-display">
              格式化顯示：{{ formatNumber(currentOrder.amount) }}
            </div>
            <ErrorMessage name="amount" class="error-message" />
            <small style="color: #666; display: block; margin-top: 5px;">
              請輸入大於 0 的金額
            </small>
          </div>
          
          <div class="form-group">
            <label>幣別：<span class="required">*</span></label>
            <div class="input-wrapper">
              <Field 
                name="currency" 
                as="select"
                v-model="currentOrder.currency"
                :class="{ 'error': errors.currency, 'success': !errors.currency && meta.touched && currentOrder.currency }"
              >
                <option value="">請選擇幣別</option>
                <option v-for="currency in currencies" :key="currency.currencyCode" :value="currency.currencyCode">
                  {{ currency.currencyCode }} - {{ getCurrencyName(currency.currencyCode) }}
                </option>
              </Field>
              <span v-if="!errors.currency && meta.touched && currentOrder.currency" class="input-icon success-icon">✓</span>
              <span v-if="errors.currency" class="input-icon error-icon">✕</span>
            </div>
            <ErrorMessage name="currency" class="error-message" />
          </div>
          
          <div class="form-group">
            <label>折扣 (%)：</label>
            <div class="input-wrapper">
              <Field 
                name="discount" 
                type="number" 
                v-model.number="currentOrder.discount"
                @input="handleDiscountInput"
                step="0.01"
                min="0"
                max="100"
                :class="{ 'error': errors.discount, 'success': !errors.discount && meta.touched && currentOrder.discount !== undefined }"
                placeholder="0"
              />
              <span v-if="!errors.discount && meta.touched && currentOrder.discount !== undefined" class="input-icon success-icon">✓</span>
              <span v-if="errors.discount" class="input-icon error-icon">✕</span>
            </div>
            <ErrorMessage name="discount" class="error-message" />
            <!-- 折扣滑桿 -->
            <div class="discount-slider-wrapper">
              <input 
                type="range" 
                v-model.number="currentOrder.discount"
                @input="handleDiscountSlider"
                min="0" 
                max="100" 
                step="1"
                class="discount-slider"
              />
              <div class="discount-labels">
                <span>0%</span>
                <span>50%</span>
                <span>100%</span>
              </div>
            </div>
            <small style="color: #666; display: block; margin-top: 5px;">
              折扣範圍：0% - 100%（不能為負數）
            </small>
            <div v-if="currentOrder.discount < 0 || currentOrder.discount > 100" class="discount-warning">
              ⚠️ 折扣必須在 0% 到 100% 之間
            </div>
          </div>
          
          <!-- 即時計算預覽 -->
          <div v-if="currentOrder.amount && currentOrder.currency" class="calculation-preview">
            <div class="preview-item">
              <span class="preview-label">原始金額：</span>
              <span class="preview-value">{{ formatCurrency(currentOrder.amount, currentOrder.currency) }}</span>
            </div>
            <div v-if="currentOrder.discount > 0" class="preview-item">
              <span class="preview-label">折扣 ({{ currentOrder.discount }}%)：</span>
              <span class="preview-value discount-value">-{{ formatCurrency(calculateDiscount(), currentOrder.currency) }}</span>
            </div>
            <div class="preview-item preview-total">
              <span class="preview-label">最終金額：</span>
              <span class="preview-value total-value">{{ formatCurrency(calculateFinalAmount(), currentOrder.currency) }}</span>
            </div>
          </div>
          
          <div class="form-group">
            <label>狀態：<span class="required">*</span></label>
            <div class="status-buttons">
              <button
                type="button"
                v-for="statusOption in statusOptions"
                :key="statusOption.value"
                @click="currentOrder.status = statusOption.value"
                :class="['status-btn', { 'active': currentOrder.status === statusOption.value, 'error': errors.status && meta.touched }]"
              >
                <span class="status-icon">{{ statusOption.icon }}</span>
                <span>{{ statusOption.label }}</span>
              </button>
            </div>
            <Field name="status" v-model="currentOrder.status" style="display: none;" />
            <ErrorMessage name="status" class="error-message" />
          </div>
          
          <div style="display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px;">
            <button type="button" class="btn-secondary" @click="resetOrderForm(resetForm)">重置</button>
            <button type="button" class="btn-secondary" @click="closeModal">取消</button>
            <button type="submit" class="btn-success" :disabled="!meta.valid">
              <span v-if="meta.valid">✓ 儲存</span>
              <span v-else>儲存 (請完成表單)</span>
            </button>
          </div>
        </Form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import Login from './Login.vue'
import { Form, Field, ErrorMessage } from 'vee-validate'
import * as yup from 'yup'
import { toTypedSchema } from '@vee-validate/yup'

const API_BASE_URL = '/api'

// Currency Code Enum Constants
const CurrencyCode = {
  TWD: 'TWD',
  USD: 'USD',
  EUR: 'EUR',
  JPY: 'JPY',
  CNY: 'CNY'
}

// 表單驗證規則
const orderSchema = yup.object({
  username: yup.string().required('使用者名稱為必填項').min(3, '使用者名稱至少需要 3 個字元').max(50, '使用者名稱不能超過 50 個字元'),
  amount: yup.number().required('金額為必填項').min(0.01, '金額必須大於 0'),
  currency: yup.string().required('請選擇幣別'),
  discount: yup.number().nullable().min(0, '折扣不能小於 0').max(100, '折扣不能超過 100'),
  status: yup.string().required('請選擇狀態')
})

export default {
  name: 'App',
  components: {
    Login,
    Form,
    Field,
    ErrorMessage
  },
  data() {
    return {
      isAuthenticated: false,
      currentUsername: '',
      userRoles: [], // 用戶角色列表
      menuItems: [], // 選單項目列表（從後端載入）
      orders: [],
      currencies: [],
      convertAmount: 0,
      sourceCurrency: CurrencyCode.USD,
      targetCurrency: CurrencyCode.TWD,
      convertedResult: null,
      showModal: false,
      isEditMode: false,
      searchOrderId: '',
      searchTimer: null,
      currentOrder: {
        username: '',
        amount: 0,
        currency: CurrencyCode.USD,
        discount: 0,
        status: 'PENDING'
      },
      formattedAmount: '',
      usernameSuggestions: [],
      statusOptions: [
        { value: 'PENDING', label: '待處理', icon: '⏳' },
        { value: 'CONFIRMED', label: '已確認', icon: '✓' },
        { value: 'CANCELLED', label: '已取消', icon: '✕' },
        { value: 'COMPLETED', label: '已完成', icon: '✅' }
      ],
      currentPage: 'orders',  // 預設顯示訂單列表
      // 分頁相關
      currentPageNumber: 0,
      pageSize: 10,
      totalPages: 0,
      totalElements: 0,
      // 匯率編輯相關
      editingRates: {},  // 儲存正在編輯的匯率 { currencyCode: rate }
      autoUpdateEnabled: true,  // 自動更新開關狀態
      // 訊息通知
      notification: {
        show: false,
        type: 'info', // 'success', 'error', 'info', 'warning'
        title: '',
        message: '',
        fieldErrors: []
      },
      notificationTimer: null,
      // 確認對話框
      confirmDialog: {
        show: false,
        title: '',
        message: '',
        onConfirm: null,
        onCancel: null
      }
    }
  },
  mounted() {
    // 檢查是否有保存的 token
    const token = localStorage.getItem('token')
    const username = localStorage.getItem('username')
    
    if (token && username) {
      this.isAuthenticated = true
      this.currentUsername = username
      // 設置 axios 預設 header
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      // 獲取用戶角色信息和選單
      this.loadUserRoles().then(async () => {
        await this.loadMenu()
        this.loadOrders()
        this.loadCurrencies()
      })
    }
  },
  methods: {
    // 認證相關方法
    async handleLoginSuccess(token, username) {
      this.isAuthenticated = true
      this.currentUsername = username
      // 設置 axios 預設 header
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      // 獲取用戶角色信息和選單
      await this.loadUserRoles()
      await this.loadMenu()
      this.loadOrders()
      this.loadCurrencies()
    },
    handleLogout() {
      this.showConfirmDialog(
        '確認登出',
        '確定要登出系統嗎？',
        () => {
          localStorage.removeItem('token')
          localStorage.removeItem('username')
          delete axios.defaults.headers.common['Authorization']
          this.isAuthenticated = false
          this.currentUsername = ''
          this.userRoles = []
          this.menuItems = []
          this.orders = []
          this.currencies = []
          this.showNotification('success', '已登出', '您已成功登出系統')
        }
      )
    },
    // 確認對話框相關方法
    showConfirmDialog(title, message, onConfirm, onCancel = null) {
      this.confirmDialog = {
        show: true,
        title: title,
        message: message,
        onConfirm: onConfirm,
        onCancel: onCancel
      }
    },
    executeConfirm() {
      if (this.confirmDialog.onConfirm) {
        this.confirmDialog.onConfirm()
      }
      this.confirmDialog.show = false
      this.confirmDialog.onConfirm = null
      this.confirmDialog.onCancel = null
    },
    cancelConfirm() {
      if (this.confirmDialog.onCancel) {
        this.confirmDialog.onCancel()
      }
      this.confirmDialog.show = false
      this.confirmDialog.onConfirm = null
      this.confirmDialog.onCancel = null
    },
    // 載入用戶角色信息
    async loadUserRoles() {
      try {
        const response = await axios.get(`${API_BASE_URL}/auth/me`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.userRoles = response.data.authorities || []
      } catch (error) {
        console.error('載入用戶角色失敗:', error)
        this.userRoles = []
      }
    },
    // 從後端載入選單
    async loadMenu() {
      try {
        const response = await axios.get(`${API_BASE_URL}/auth/menu`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.menuItems = response.data || []
        // 如果選單為空，設置默認選單（向後兼容）
        if (this.menuItems.length === 0) {
          this.menuItems = [
            { id: 'orders', label: '訂單列表', icon: '📋', route: 'orders' },
            { id: 'currency', label: '幣別轉換系統', icon: '💱', route: 'currency' }
          ]
        }
      } catch (error) {
        console.error('載入選單失敗:', error)
        // 如果載入失敗，使用默認選單
        this.menuItems = [
          { id: 'orders', label: '訂單列表', icon: '📋', route: 'orders' },
          { id: 'currency', label: '幣別轉換系統', icon: '💱', route: 'currency' }
        ]
      }
    },
    // 訊息通知相關方法
    showNotification(type, title, message = '', fieldErrors = []) {
      this.notification = {
        show: true,
        type: type,
        title: title,
        message: message,
        fieldErrors: fieldErrors
      }
      // 自動關閉通知（錯誤訊息顯示 8 秒，成功訊息顯示 5 秒）
      if (this.notificationTimer) {
        clearTimeout(this.notificationTimer)
      }
      const duration = type === 'error' ? 8000 : 5000
      this.notificationTimer = setTimeout(() => {
        this.closeNotification()
      }, duration)
    },
    closeNotification() {
      this.notification.show = false
      if (this.notificationTimer) {
        clearTimeout(this.notificationTimer)
        this.notificationTimer = null
      }
    },
    // 解析錯誤響應
    parseError(error) {
      if (error.response && error.response.data) {
        const errorData = error.response.data
        // 後端返回的 ErrorResponse 格式
        const message = errorData.message || errorData.error || '發生未知錯誤'
        const fieldErrors = errorData.fieldErrors || []
        return { message, fieldErrors }
      }
      return { message: error.message || '網路連線錯誤，請稍後再試', fieldErrors: [] }
    },
    changePage(page) {
      // 如果點擊的是當前頁面，不執行任何操作，避免重複載入
      if (this.currentPage === page) {
        return
      }
      // 檢查選單項是否存在（權限檢查）
      const menuItem = this.menuItems.find(item => item.route === page)
      if (!menuItem) {
        this.showNotification('error', '權限不足', '您沒有權限訪問此功能')
        return
      }
      this.currentPage = page
      // 如果切換到訂單列表頁面，確保載入訂單資料
      if (page === 'orders') {
        this.currentPageNumber = 0  // 重置到第一頁
        this.loadOrders()
      }
      // 如果切換到幣別轉換頁面，確保載入幣別資料
      if (page === 'currency') {
        this.loadCurrencies()
      }
      // 如果切換到匯率管理頁面，確保載入匯率資料和自動更新狀態
      if (page === 'rates') {
        this.loadCurrencies()
        this.loadAutoUpdateStatus()
      }
    },
    async loadOrders(searchOrderId = null, page = null, size = null) {
      try {
        const params = {
          page: page !== null ? page : this.currentPageNumber,
          size: size !== null ? size : this.pageSize
        }
        
        if (searchOrderId && searchOrderId.trim()) {
          params.searchOrderId = searchOrderId.trim()
        }
        
        const response = await axios.get(`${API_BASE_URL}/orders`, { 
          params,
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        // 處理分頁響應
        if (response.data.content) {
          this.orders = response.data.content
          this.totalPages = response.data.totalPages
          this.totalElements = response.data.totalElements
          this.currentPageNumber = response.data.number
        } else {
          // 向後兼容：如果後端返回的是列表
          this.orders = response.data
          this.totalPages = 1
          this.totalElements = response.data.length
          this.currentPageNumber = 0
        }
      } catch (error) {
        console.error('載入訂單失敗:', error)
        // 如果是 401 未授權，則登出
        if (error.response && error.response.status === 401) {
          this.handleLogout()
          return
        }
        const { message } = this.parseError(error)
        this.showNotification('error', '載入訂單失敗', message)
      }
    },
    debounceSearch() {
      // 清除之前的計時器
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
      }
      // 重置到第一頁
      this.currentPageNumber = 0
      // 設定新的計時器，500ms 後執行搜尋
      this.searchTimer = setTimeout(() => {
        this.loadOrders(this.searchOrderId, 0, this.pageSize)
      }, 500)
    },
    async loadCurrencies() {
      try {
        const response = await axios.get(`${API_BASE_URL}/currencies`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.currencies = response.data
        if (this.currencies.length > 0) {
          this.sourceCurrency = this.currencies[0].currencyCode
        }
      } catch (error) {
        console.error('載入幣別失敗:', error)
        // 如果是 401 未授權，則登出
        if (error.response && error.response.status === 401) {
          this.handleLogout()
          return
        }
        // 如果沒有幣別資料，使用預設值（僅在非認證錯誤時）
        if (!error.response || error.response.status !== 403) {
          this.currencies = [
            { currencyCode: CurrencyCode.TWD, rateToTwd: 1 },
            { currencyCode: CurrencyCode.USD, rateToTwd: 0.032 }
          ]
        } else {
          const { message } = this.parseError(error)
          this.showNotification('error', '載入幣別失敗', message)
        }
      }
    },
    async convertCurrency() {
      try {
        const response = await axios.post(`${API_BASE_URL}/currencies/convert`, null, {
          params: {
            amount: this.convertAmount,
            sourceCurrency: this.sourceCurrency,
            targetCurrency: this.targetCurrency
          },
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.convertedResult = response.data
      } catch (error) {
        console.error('幣別換算失敗:', error)
        const { message, fieldErrors } = this.parseError(error)
        this.showNotification('error', '幣別換算失敗', message, fieldErrors)
      }
    },
    async convertOrderToTwd(orderId) {
      try {
        const response = await axios.get(`${API_BASE_URL}/orders/${orderId}/convert/twd`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.showNotification('success', '轉換成功', `轉換為 ${CurrencyCode.TWD}: ${response.data.toFixed(2)}`)
      } catch (error) {
        console.error('轉換失敗:', error)
        const { message, fieldErrors } = this.parseError(error)
        this.showNotification('error', '轉換失敗', message, fieldErrors)
      }
    },
    openAddModal() {
      this.isEditMode = false
      this.currentOrder = {
        username: this.currentUsername, // 自動填入當前登入用戶名
        amount: 0,
        currency: CurrencyCode.USD,
        discount: 0,
        status: 'PENDING'
      }
      this.formattedAmount = ''
      this.usernameSuggestions = []
      this.showModal = true
    },
    openEditModal(order) {
      this.isEditMode = true
      this.currentOrder = {
        orderId: order.orderId,
        username: order.username,
        amount: order.amount,
        currency: order.currency,
        discount: order.discount || 0,
        status: order.status
      }
      this.formattedAmount = this.formatNumber(order.amount)
      this.usernameSuggestions = []
      this.showModal = true
    },
    closeModal() {
      this.showModal = false
    },
    resetOrderForm(resetForm) {
      // 重置表單驗證狀態
      if (resetForm) {
        resetForm()
      }
      // 重置訂單資料到初始狀態
      if (this.isEditMode) {
        // 編輯模式：保持當前訂單資料，只重置表單驗證
        // 不重置資料，讓用戶可以繼續編輯
      } else {
        // 新增模式：重置所有欄位
        this.currentOrder = {
          username: this.currentUsername,
          amount: 0,
          currency: CurrencyCode.USD,
          discount: 0,
          status: 'PENDING'
        }
        this.formattedAmount = ''
        this.usernameSuggestions = []
      }
      // 顯示重置成功提示
      this.showNotification('info', '表單已重置', '所有欄位已恢復為初始值')
    },
    async onSubmit(values, { resetForm }) {
      // VeeValidate 已經驗證過表單，直接儲存
      console.log('表單提交，值:', values)
      console.log('當前訂單:', this.currentOrder)
      try {
        await this.saveOrder()
        // 成功後重置表單
        if (resetForm) resetForm()
      } catch (error) {
        console.error('表單提交錯誤:', error)
        // 錯誤已在 saveOrder 中處理
      }
    },
    async saveOrder() {
      try {
        const headers = {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
        if (this.isEditMode) {
          await axios.put(`${API_BASE_URL}/orders/${this.currentOrder.orderId}`, this.currentOrder, { headers })
        } else {
          await axios.post(`${API_BASE_URL}/orders`, this.currentOrder, { headers })
        }
        this.closeModal()
        // 重新載入當前頁面的資料
        this.loadOrders(this.searchOrderId, this.currentPageNumber, this.pageSize)
        this.showNotification('success', '儲存成功', '訂單已成功儲存')
      } catch (error) {
        console.error('儲存訂單失敗:', error)
        const { message, fieldErrors } = this.parseError(error)
        this.showNotification('error', '儲存訂單失敗', message, fieldErrors)
      }
    },
    async deleteOrder(orderId) {
      this.showConfirmDialog(
        '刪除訂單',
        '確定要刪除這個訂單嗎？此操作無法復原。',
        async () => {
          try {
            await axios.delete(`${API_BASE_URL}/orders/${orderId}`, {
              headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
              }
            })
            // 重新載入當前頁面的資料
            this.loadOrders(this.searchOrderId, this.currentPageNumber, this.pageSize)
            this.showNotification('success', '刪除成功', '訂單已成功刪除')
          } catch (error) {
            console.error('刪除訂單失敗:', error)
            const { message, fieldErrors } = this.parseError(error)
            this.showNotification('error', '刪除訂單失敗', message, fieldErrors)
          }
        }
      )
    },
    clearSearch() {
      this.searchOrderId = ''
      this.currentPageNumber = 0  // 重置到第一頁
      this.loadOrders()
    },
    changePageNumber(page) {
      if (page < 0 || page >= this.totalPages) {
        return
      }
      this.currentPageNumber = page
      this.loadOrders(this.searchOrderId, page, this.pageSize)
    },
    onPageSizeChange() {
      // 當改變每頁顯示筆數時，重置到第一頁並重新載入資料
      this.currentPageNumber = 0
      this.loadOrders(this.searchOrderId, 0, this.pageSize)
    },
    // 匯率管理相關方法
    startEditRate(currency) {
      // Vue 3 不需要 $set，直接賦值即可
      this.editingRates[currency.currencyCode] = currency.rateToTwd
    },
    cancelEditRate(currencyCode) {
      // Vue 3 不需要 $delete，使用 delete 關鍵字即可
      delete this.editingRates[currencyCode]
    },
    async saveRate(currencyCode) {
      const newRate = this.editingRates[currencyCode]
      if (newRate === undefined || newRate === null || newRate <= 0) {
        this.showNotification('error', '輸入錯誤', '匯率必須大於 0')
        return
      }
      
      try {
        await axios.put(`${API_BASE_URL}/currencies/${currencyCode}/rate`, newRate, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        delete this.editingRates[currencyCode]
        // 重新載入匯率資料
        await this.loadCurrencies()
        this.showNotification('success', '更新成功', '匯率已成功更新')
      } catch (error) {
        console.error('更新匯率失敗:', error)
        const { message, fieldErrors } = this.parseError(error)
        this.showNotification('error', '更新匯率失敗', message, fieldErrors)
      }
    },
    async refreshRatesFromApi() {
      this.showConfirmDialog(
        '更新匯率',
        '確定要從 ExchangeRate-API 更新所有匯率嗎？這會覆蓋目前的匯率設定。',
        async () => {
          try {
            const response = await axios.post(`${API_BASE_URL}/currencies/refresh`, null, {
              headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
              }
            })
            this.showNotification('success', '更新成功', '已從 ExchangeRate-API 取得最新匯率')
            // 重新載入匯率資料
            await this.loadCurrencies()
          } catch (error) {
            console.error('更新匯率失敗:', error)
            const { message, fieldErrors } = this.parseError(error)
            this.showNotification('error', '更新匯率失敗', message, fieldErrors)
          }
        }
      )
    },
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '-'
      try {
        const date = new Date(dateTimeString)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      } catch (e) {
        return dateTimeString
      }
    },
    // 自動更新開關相關方法
    async loadAutoUpdateStatus() {
      try {
        const response = await axios.get(`${API_BASE_URL}/currencies/auto-update/status`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.autoUpdateEnabled = response.data.enabled
      } catch (error) {
        console.error('載入自動更新狀態失敗:', error)
        // 預設為啟用狀態
        this.autoUpdateEnabled = true
      }
    },
    async toggleAutoUpdate() {
      try {
        const headers = {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
        if (this.autoUpdateEnabled) {
          // 啟用自動更新
          const response = await axios.post(`${API_BASE_URL}/currencies/auto-update/enable`, null, { headers })
          this.showNotification('success', '操作成功', response.data.message || '自動更新已啟用')
          // 重新載入匯率資料
          await this.loadCurrencies()
        } else {
          // 停用自動更新
          const response = await axios.post(`${API_BASE_URL}/currencies/auto-update/disable`, null, { headers })
          this.showNotification('success', '操作成功', response.data.message || '自動更新已停用')
        }
      } catch (error) {
        console.error('切換自動更新狀態失敗:', error)
        // 恢復原狀態
        this.autoUpdateEnabled = !this.autoUpdateEnabled
        const { message, fieldErrors } = this.parseError(error)
        this.showNotification('error', '操作失敗', message, fieldErrors)
      }
    },
    // 金額格式化相關方法
    formatNumber(value) {
      if (!value && value !== 0) return ''
      const num = parseFloat(value)
      if (isNaN(num)) return ''
      return num.toLocaleString('zh-TW', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    },
    handleAmountInput(event) {
      const value = event.target.value
      const num = parseFloat(value)
      if (!isNaN(num) && num > 0) {
        this.currentOrder.amount = num
        this.formattedAmount = this.formatNumber(num)
      } else if (value === '' || value === null) {
        this.currentOrder.amount = 0
        this.formattedAmount = ''
      }
    },
    formatCurrency(amount, currency) {
      if (!amount) return '0.00'
      const formatted = this.formatNumber(amount)
      return `${formatted} ${currency}`
    },
    calculateDiscount() {
      if (!this.currentOrder.amount || !this.currentOrder.discount) return 0
      return this.currentOrder.amount * (this.currentOrder.discount / 100)
    },
    calculateFinalAmount() {
      if (!this.currentOrder.amount) return 0
      const discount = this.calculateDiscount()
      return this.currentOrder.amount - discount
    },
    // 使用者名稱建議相關
    filterUsernameSuggestions() {
      const input = this.currentOrder.username.toLowerCase()
      if (input.length < 2) {
        this.usernameSuggestions = []
        return
      }
      // 從現有訂單中提取使用者名稱建議
      const uniqueUsernames = [...new Set(this.orders.map(o => o.username))]
      this.usernameSuggestions = uniqueUsernames
        .filter(u => u.toLowerCase().includes(input) && u !== this.currentOrder.username)
        .slice(0, 5)
    },
    selectUsername(username) {
      this.currentOrder.username = username
      this.usernameSuggestions = []
    },
    getCurrencyName(code) {
      const names = {
        'TWD': '新台幣',
        'USD': '美元',
        'EUR': '歐元',
        'JPY': '日圓',
        'CNY': '人民幣'
      }
      return names[code] || code
    },
    // 處理折扣輸入限制
    handleDiscountInput(event) {
      let value = parseFloat(event.target.value)
      // 如果是空值或 NaN，設為 0
      if (isNaN(value) || value === null || value === undefined) {
        this.currentOrder.discount = 0
        return
      }
      // 限制不能為負數
      if (value < 0) {
        value = 0
        this.currentOrder.discount = 0
        this.showNotification('warning', '輸入限制', '折扣不能為負數，已自動調整為 0%')
        return
      }
      // 限制不能超過 100
      if (value > 100) {
        value = 100
        this.currentOrder.discount = 100
        this.showNotification('warning', '輸入限制', '折扣不能超過 100%，已自動調整為 100%')
        return
      }
      // 正常範圍內的值
      this.currentOrder.discount = value
    },
    // 處理折扣滑桿輸入
    handleDiscountSlider(event) {
      let value = parseFloat(event.target.value)
      // 確保值在 0-100 範圍內
      if (isNaN(value)) {
        this.currentOrder.discount = 0
        return
      }
      if (value < 0) {
        this.currentOrder.discount = 0
      } else if (value > 100) {
        this.currentOrder.discount = 100
      } else {
        this.currentOrder.discount = value
      }
    }
  },
  // Expose CurrencyCode to template
  computed: {
    CurrencyCodes() {
      return CurrencyCode
    },
    // 檢查是否為管理員
    isAdmin() {
      return this.userRoles.includes('ROLE_ADMIN')
    },
    // 表單驗證規則（轉換為 VeeValidate 格式）
    typedOrderSchema() {
      return toTypedSchema(orderSchema)
    }
  }
}
</script>

