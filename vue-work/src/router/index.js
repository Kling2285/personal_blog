import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        name:'index',
        redirect: '/umanager/uhome'
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('../views/login.vue')
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('../views/register.vue')
    },
    {
        path: '/getback',
        name: 'getback',
        component: () => import('../views/getback.vue')
    },
    {
        path: '/umanager',
        name: 'umanager',
        component: () => import('../views/ForUser/Umanager.vue'),
        children: [
            {
                path: '',
                name: 'umanager-index',
                redirect: 'uhome'
            },
            {
                path: 'center',
                name: 'manager-center',
                component: () => import('../views/ForUser/center.vue')
            },
            {
                path: 'uhome',
                name: 'manager-uhome',
                component: () => import('../views/ForUser/uhome.vue')
            },
            {
                path: 'unotice',
                name: 'manager-unotice',
                component: () => import('../views/ForUser/unotice.vue')
            },
            {
                path: 'upost',
                name: 'manager-upost',
                component: () => import('../views/ForUser/upost.vue')
            },
        ]
    },
    {
        path: '/manager',
        name: 'manager',
        component: () => import('../views/ForAdmin/Amanager.vue'),
        // 子路由写在这里！
        children: [
            {
                path: '',
                name: 'manager-index',
                redirect: 'home'
            },
            {
                path: 'home',
                name: 'manager-home',
                component: () => import('../views/ForAdmin/home.vue')
            },
            {
                path: 'user',
                name: 'manager-user',
                component: () => import('../views/ForAdmin/user.vue')
            },
            {
                path: 'category',
                name: 'manager-category',
                component: () => import('../views/ForAdmin/category.vue')
            },
            {
                path: 'notice',
                name: 'manager-notice',
                component: () => import('../views/ForAdmin/notice.vue')
            },
            {
                path: 'post',
                name: 'manager-post',
                component: () => import('../views/ForAdmin/post.vue')
            },
            {
                path: 'word',
                name: 'manager-word',
                component: () => import('../views/ForAdmin/word.vue')
            },
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('../views/404.vue')
    }
]


const router = createRouter({
    history: createWebHistory(),
    routes
})

//路由守卫
const whiteList = [
    '/login',
    '/register',
    '/notfound',
    '/getback',
    '/umanager/uhome',
];

// 路由拦截逻辑（控制管理员页面访问权限）
router.beforeEach((to, from, next) => {
    const user = localStorage.getItem("login_user"); // 获取登录状态

    if (user) {
        next();
    } else {
        if (whiteList.includes(to.path)) {
            next();
        } else {
            next('/login');
        }
    }
})


export default router