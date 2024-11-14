import {createRouter, createWebHistory} from "vue-router";
import {unauthorize} from "@/net/index.js";

const router = createRouter({
    history:createWebHistory(import.meta.env.BASE_URL),
    routes:[
        {
            path:'/',
            name:'Welcome',
            component:()=>import('@/views/WelcomeView.vue'),
            children:[
                {
                    path:'/',
                    name:'Welcome-login',
                    component:()=>import('@/views/welcome/login.vue'),
                },
                {
                    path:'/register',
                    name:'Welcome-register',
                    component:() => import('@/views/welcome/register.vue'),
                },
                {
                    path:'/forget',
                    name:'Welcome-forget',
                    component:()=> import('@/views/welcome/forget.vue')
                }
            ]
        },{
            path: '/test',
            name: 'test',
            component:() =>import('@/views/test.vue')
        }
    ]
})

router.beforeEach((to, from, next) => {
    const isUnauthorized = unauthorize()
    if(to.name.startsWith('welcome') && !isUnauthorized) {
        next('/test')
    } else if(to.fullPath.startsWith('/test') && isUnauthorized) {
        next('/')
    } else {
        next()
    }
})

export default router;