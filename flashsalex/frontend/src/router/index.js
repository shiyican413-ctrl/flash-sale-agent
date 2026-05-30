import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/user/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/user/Register.vue'),
    meta: { title: '注册' },
  },
  // 用户端
  {
    path: '/',
    component: () => import('../components/UserLayout.vue'),
    redirect: '/activities',
    children: [
      {
        path: 'activities',
        name: 'ActivityList',
        component: () => import('../views/user/ActivityList.vue'),
        meta: { title: '秒杀活动' },
      },
      {
        path: 'activities/:id',
        name: 'ActivityDetail',
        component: () => import('../views/user/ActivityDetail.vue'),
        meta: { title: '活动详情' },
      },
      {
        path: 'orders',
        name: 'MyOrders',
        component: () => import('../views/user/MyOrders.vue'),
        meta: { title: '我的订单', needAuth: true },
      },
    ],
  },
  // 管理端
  {
    path: '/admin',
    component: () => import('../components/AdminLayout.vue'),
    redirect: '/admin/goods',
    meta: { needAdmin: true },
    children: [
      {
        path: 'goods',
        name: 'AdminGoods',
        component: () => import('../views/admin/GoodsList.vue'),
        meta: { title: '商品管理', needAdmin: true },
      },
      {
        path: 'goods/add',
        name: 'AdminGoodsAdd',
        component: () => import('../views/admin/GoodsForm.vue'),
        meta: { title: '添加商品', needAdmin: true },
      },
      {
        path: 'goods/edit/:id',
        name: 'AdminGoodsEdit',
        component: () => import('../views/admin/GoodsForm.vue'),
        meta: { title: '编辑商品', needAdmin: true },
      },
      {
        path: 'activities',
        name: 'AdminActivities',
        component: () => import('../views/admin/ActivityList.vue'),
        meta: { title: '活动管理', needAdmin: true },
      },
      {
        path: 'activities/add',
        name: 'AdminActivityAdd',
        component: () => import('../views/admin/ActivityForm.vue'),
        meta: { title: '创建活动', needAdmin: true },
      },
      {
        path: 'blacklist',
        name: 'AdminBlacklist',
        component: () => import('../views/admin/Blacklist.vue'),
        meta: { title: '黑名单管理', needAdmin: true },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || 'FlashSaleX'} - FlashSaleX`

  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

  if (to.meta.needAuth && !token) {
    return next('/login')
  }

  if (to.meta.needAdmin && userInfo?.role !== 'ADMIN') {
    return next('/')
  }

  next()
})

export default router
