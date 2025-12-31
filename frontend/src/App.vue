<template>
  <div id="app">
    <div class="app-container">
      <!-- 左側選單 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h1>訂單與幣別轉換系統</h1>
        </div>
        <nav class="sidebar-menu">
          <div 
            class="menu-item" 
            :class="{ active: currentPage === 'orders' }"
            @click="changePage('orders')"
          >
            <span class="menu-icon">📋</span>
            <span class="menu-text">訂單列表</span>
          </div>
          <div 
            class="menu-item" 
            :class="{ active: currentPage === 'currency' }"
            @click="changePage('currency')"
          >
            <span class="menu-icon">💱</span>
            <span class="menu-text">幣別轉換系統</span>
          </div>
          <div 
            class="menu-item" 
            :class="{ active: currentPage === 'rates' }"
            @click="changePage('rates')"
          >
            <span class="menu-icon">📊</span>
            <span class="menu-text">匯率管理</span>
          </div>
        </nav>
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
        <span class="close" @click="closeModal">&times;</span>
        <h2>{{ isEditMode ? '編輯訂單' : '新增訂單' }}</h2>
        <div class="form-group">
          <label>使用者名稱：</label>
          <input type="text" v-model="currentOrder.username" />
        </div>
        <div class="form-group">
          <label>金額：</label>
          <input type="number" v-model.number="currentOrder.amount" step="0.01" />
        </div>
        <div class="form-group">
          <label>幣別：</label>
          <select v-model="currentOrder.currency">
            <option v-for="currency in currencies" :key="currency.currencyCode" :value="currency.currencyCode">
              {{ currency.currencyCode }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label>折扣 (%)：</label>
          <input type="number" v-model.number="currentOrder.discount" step="0.01" min="0" max="100" />
        </div>
        <div class="form-group">
          <label>狀態：</label>
          <select v-model="currentOrder.status">
            <option value="PENDING">待處理</option>
            <option value="CONFIRMED">已確認</option>
            <option value="CANCELLED">已取消</option>
            <option value="COMPLETED">已完成</option>
          </select>
        </div>
        <button class="btn-success" @click="saveOrder">儲存</button>
        <button class="btn-secondary" @click="closeModal">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

const API_BASE_URL = '/api'

// Currency Code Enum Constants
const CurrencyCode = {
  TWD: 'TWD',
  USD: 'USD',
  EUR: 'EUR',
  JPY: 'JPY',
  CNY: 'CNY'
}

export default {
  name: 'App',
  data() {
    return {
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
      currentPage: 'orders',  // 預設顯示訂單列表
      // 分頁相關
      currentPageNumber: 0,
      pageSize: 10,
      totalPages: 0,
      totalElements: 0,
      // 匯率編輯相關
      editingRates: {},  // 儲存正在編輯的匯率 { currencyCode: rate }
      autoUpdateEnabled: true  // 自動更新開關狀態
    }
  },
  mounted() {
    this.loadOrders()
    this.loadCurrencies()
  },
  methods: {
    changePage(page) {
      // 如果點擊的是當前頁面，不執行任何操作，避免重複載入
      if (this.currentPage === page) {
        return
      }
      this.currentPage = page
      // 如果切換到訂單列表頁面，確保載入訂單資料
      if (page === 'orders') {
        this.currentPageNumber = 0  // 重置到第一頁
        this.loadOrders()
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
        
        const response = await axios.get(`${API_BASE_URL}/orders`, { params })
        
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
        alert('載入訂單失敗')
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
        const response = await axios.get(`${API_BASE_URL}/currencies`)
        this.currencies = response.data
        if (this.currencies.length > 0) {
          this.sourceCurrency = this.currencies[0].currencyCode
        }
      } catch (error) {
        console.error('載入幣別失敗:', error)
        // 如果沒有幣別資料，使用預設值
        this.currencies = [
          { currencyCode: CurrencyCode.TWD, rateToTwd: 1 },
          { currencyCode: CurrencyCode.USD, rateToTwd: 0.032 }
        ]
      }
    },
    async convertCurrency() {
      try {
        const response = await axios.post(`${API_BASE_URL}/currencies/convert`, null, {
          params: {
            amount: this.convertAmount,
            sourceCurrency: this.sourceCurrency,
            targetCurrency: this.targetCurrency
          }
        })
        this.convertedResult = response.data
      } catch (error) {
        console.error('幣別換算失敗:', error)
        alert('幣別換算失敗')
      }
    },
    async convertOrderToTwd(orderId) {
      try {
        const response = await axios.get(`${API_BASE_URL}/orders/${orderId}/convert/twd`)
        alert(`轉換為 ${CurrencyCode.TWD}: ${response.data.toFixed(2)}`)
      } catch (error) {
        console.error('轉換失敗:', error)
        alert('轉換失敗')
      }
    },
    openAddModal() {
      this.isEditMode = false
      this.currentOrder = {
        username: '',
        amount: 0,
        currency: CurrencyCode.USD,
        discount: 0,
        status: 'PENDING'
      }
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
      this.showModal = true
    },
    closeModal() {
      this.showModal = false
    },
    async saveOrder() {
      try {
        if (this.isEditMode) {
          await axios.put(`${API_BASE_URL}/orders/${this.currentOrder.orderId}`, this.currentOrder)
        } else {
          await axios.post(`${API_BASE_URL}/orders`, this.currentOrder)
        }
        this.closeModal()
        // 重新載入當前頁面的資料
        this.loadOrders(this.searchOrderId, this.currentPageNumber, this.pageSize)
      } catch (error) {
        console.error('儲存訂單失敗:', error)
        alert('儲存訂單失敗')
      }
    },
    async deleteOrder(orderId) {
      if (confirm('確定要刪除這個訂單嗎？')) {
        try {
          await axios.delete(`${API_BASE_URL}/orders/${orderId}`)
          // 重新載入當前頁面的資料
          this.loadOrders(this.searchOrderId, this.currentPageNumber, this.pageSize)
        } catch (error) {
          console.error('刪除訂單失敗:', error)
          alert('刪除訂單失敗')
        }
      }
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
        alert('匯率必須大於 0')
        return
      }
      
      try {
        await axios.put(`${API_BASE_URL}/currencies/${currencyCode}/rate`, newRate, {
          headers: {
            'Content-Type': 'application/json'
          }
        })
        delete this.editingRates[currencyCode]
        // 重新載入匯率資料
        await this.loadCurrencies()
        alert('匯率更新成功！')
      } catch (error) {
        console.error('更新匯率失敗:', error)
        alert('更新匯率失敗：' + (error.response?.data?.message || error.message))
      }
    },
    async refreshRatesFromApi() {
      if (!confirm('確定要從 ExchangeRate-API 更新所有匯率嗎？這會覆蓋目前的匯率設定。')) {
        return
      }
      
      try {
        const response = await axios.post(`${API_BASE_URL}/currencies/refresh`)
        alert('匯率更新成功！已從 ExchangeRate-API 取得最新匯率。')
        // 重新載入匯率資料
        await this.loadCurrencies()
      } catch (error) {
        console.error('更新匯率失敗:', error)
        alert('更新匯率失敗：' + (error.response?.data?.message || error.message))
      }
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
        const response = await axios.get(`${API_BASE_URL}/currencies/auto-update/status`)
        this.autoUpdateEnabled = response.data.enabled
      } catch (error) {
        console.error('載入自動更新狀態失敗:', error)
        // 預設為啟用狀態
        this.autoUpdateEnabled = true
      }
    },
    async toggleAutoUpdate() {
      try {
        if (this.autoUpdateEnabled) {
          // 啟用自動更新
          const response = await axios.post(`${API_BASE_URL}/currencies/auto-update/enable`)
          alert(response.data.message)
          // 重新載入匯率資料
          await this.loadCurrencies()
        } else {
          // 停用自動更新
          const response = await axios.post(`${API_BASE_URL}/currencies/auto-update/disable`)
          alert(response.data.message)
        }
      } catch (error) {
        console.error('切換自動更新狀態失敗:', error)
        // 恢復原狀態
        this.autoUpdateEnabled = !this.autoUpdateEnabled
        alert('切換自動更新狀態失敗：' + (error.response?.data?.message || error.message))
      }
    }
  },
  // Expose CurrencyCode to template
  computed: {
    CurrencyCodes() {
      return CurrencyCode
    }
  }
}
</script>

