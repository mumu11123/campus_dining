import { createRouter, createWebHistory } from 'vue-router'
import Apply from '../views/Merchant/Apply.vue'
import ApplyStatus from '../views/Merchant/ApplyStatus.vue'
import Login from '../views/Merchant/Login.vue'
import Layout from '../views/Merchant/Layout.vue'
import Home from '../views/Merchant/Home.vue'
import MerchantInfo from '../views/Merchant/MerchantInfo.vue'
import Dish from '../views/Merchant/Dish.vue'
import Order from '../views/Merchant/Order.vue'
import Message from '../views/Merchant/Message.vue'
import Statistics from '../views/Merchant/Statistics.vue'
import DishRank from '../views/Merchant/DishRank.vue'

import AdminLogin from '../views/Admin/Login.vue'
import AdminLayout from '../views/Admin/Layout.vue'
import AdminApplyList from '../views/Admin/ApplyList.vue'
import AdminBlacklist from '../views/Admin/Blacklist.vue'
import AdminMerchantList from '../views/Admin/MerchantList.vue'
import AdminSystemMaintenance from '../views/Admin/SystemMaintenance.vue'
import AdminDishSales from '../views/Admin/DishSales.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },

  {
    path: '/apply',
    name: 'Apply',
    component: Apply
  },

  {
    path: '/apply-status',
    name: 'ApplyStatus',
    component: ApplyStatus
  },

  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      { path: 'home', name: '首页', component: Home },
      { path: 'merchant/info', name: '商家信息管理', component: MerchantInfo },
      { path: 'dish', name: '菜品管理', component: Dish },
      { path: 'order', name: '订单管理', component: Order },
      { path: 'message', name: '消息聊天', component: Message },
      { path: 'statistics', name: '销售总览', component: Statistics },
      { path: 'dish-rank', name: '菜品排行', component: DishRank }
    ]
  },

  // 管理员端路由
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: AdminLogin
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/apply',
    children: [
      { path: 'apply', name: '入驻审核', component: AdminApplyList },
      { path: 'merchants', name: '商家管理', component: AdminMerchantList },
      { path: 'dishes', name: '菜品销量', component: AdminDishSales },
      { path: 'blacklist', name: '黑名单管理', component: AdminBlacklist },
      { path: 'system', name: '系统维护', component: AdminSystemMaintenance }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫（权限控制）
router.beforeEach((to, from, next) => {
  document.title = to.path.startsWith('/admin') ? '管理员端 - 校园点餐' : '商家端 - 校园点餐'

  const merchantToken = localStorage.getItem('merchantToken')
  const adminToken = localStorage.getItem('adminToken')

  // 商家端白名单
  const merchantWhiteList = ['/login', '/apply', '/apply-status']
  // 管理员白名单
  const adminWhiteList = ['/admin/login']

  // 管理员路由保护
  if (to.path.startsWith('/admin')) {
    if (adminWhiteList.includes(to.path)) {
      next()
    } else {
      if (adminToken) {
        next()
      } else {
        next('/admin/login')
      }
    }
    return
  }

  // 商家端路由保护
  if (merchantWhiteList.includes(to.path)) {
    next()
  } else {
    if (merchantToken) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
