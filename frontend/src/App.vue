<template>
  <div id="app">
    <h1>訂單與幣別轉換系統</h1>
    
    <div class="card">
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

    <div class="card">
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

const API_BASE_URL = 'http://localhost:8080/api'

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
      }
    }
  },
  mounted() {
    this.loadOrders()
    this.loadCurrencies()
  },
  methods: {
    async loadOrders(searchOrderId = null) {
      try {
        const params = searchOrderId && searchOrderId.trim() 
          ? { searchOrderId: searchOrderId.trim() } 
          : {}
        const response = await axios.get(`${API_BASE_URL}/orders`, { params })
        this.orders = response.data
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
      // 設定新的計時器，500ms 後執行搜尋
      this.searchTimer = setTimeout(() => {
        this.loadOrders(this.searchOrderId)
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
        this.loadOrders(this.searchOrderId)
      } catch (error) {
        console.error('儲存訂單失敗:', error)
        alert('儲存訂單失敗')
      }
    },
    async deleteOrder(orderId) {
      if (confirm('確定要刪除這個訂單嗎？')) {
        try {
          await axios.delete(`${API_BASE_URL}/orders/${orderId}`)
          this.loadOrders(this.searchOrderId)
        } catch (error) {
          console.error('刪除訂單失敗:', error)
          alert('刪除訂單失敗')
        }
      }
    },
    clearSearch() {
      this.searchOrderId = ''
      this.loadOrders()
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

