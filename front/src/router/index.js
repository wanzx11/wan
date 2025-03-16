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
            path: '/index',
            name: 'index',
            component:() =>import('@/views/index.vue'),
            children:[
                /*{
                    path: '',
                    name: 'topic-list',
                    component:()=>import('@/views/forum/TopicList.vue'),
                    meta: { keepAlive: true },
                },
                {
                    path: 'topic-detail/:tid',
                    name: 'topic-detail',
                    component:()=>import('@/views/forum/TopicDetail.vue'),
                    meta: { keepAlive: true },
                },*/
                {
                    path: '',
                    name: 'topics',
                    component:()=> import('@/views/forum/Forum.vue'),
                    children:[
                        {
                            path: '',
                            name: 'topic-list',
                            component:()=>import('@/views/forum/TopicList.vue'),
                            meta: { keepAlive: true },
                        },
                        {
                            path: 'topic-detail/:tid',
                            name: 'topic-detail',
                            component:()=>import('@/views/forum/TopicDetail.vue')
                        }
                    ]
                },
                {
                    path: 'user-setting',
                    name: 'user-setting',
                    component:()=>import('@/views/settings/UserSetting.vue')
                },
                {
                    path:'safety-setting',
                    name:'safety-setting',
                    component:()=>import('@/views/settings/SafetySetting.vue')
                }

            ]
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