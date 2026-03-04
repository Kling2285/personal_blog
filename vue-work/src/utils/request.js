import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router' // 导入路由，用于 Token 过期时跳转登录页

const request = axios.create({
    baseURL: 'http://localhost:8080', // 后端接口基础地址
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    }
})
request.interceptors.request.use(
    config => {
        let user = localStorage.getItem('login_user');
        if (user) {
            try {
                user = JSON.parse(user);
                // 简化：直接赋值，|| '' 保留（防止token为空）
                config.headers.token = user.token || '';
                // 等价于 config.headers['token'] = user.token || '';（两种写法都可）
            } catch (e) {
                console.error('解析login_user失败：', e);
                localStorage.removeItem('login_user');
            }
        } else {
            console.log('localStorage中无login_user');
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);


request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.msg || '操作失败，请重试')
            return Promise.reject(res.msg || '请求异常')
        }
        return res
    },
    error => {
        if (error.response) {
            const { status } = error.response
            switch (status) {
                case 401:
                    ElMessage.error('登录已过期，请重新登录')
                    sessionStorage.removeItem('token')
                    sessionStorage.removeItem('login_user')
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('没有权限访问，请联系管理员')
                    break
                case 404:
                    ElMessage.error('请求的接口不存在')
                    break
                case 500:
                    ElMessage.error('服务器内部错误，请稍后再试')
                    break
                default:
                    ElMessage.error(`请求失败（${status}）：${error.message}`)
            }
        } else {
            ElMessage.error('网络连接异常，请检查网络')
        }
        return Promise.reject(error)
    }
);

//通用批量导出的方法
export function download(url,params){
    const base_url=import.meta.env.VITE_API_BASE_URL;
    //条件查询
    let searchParam=new URLSearchParams();
    for(let key in params){
        searchParam.append(key,params[key])
    }
    let user=localStorage.getItem('login_user');
    if(user){
        user=JSON.parse(localStorage.getItem('login_user'));
        searchParam.set('token',user.token)
    }
    window.location=base_url+url+"?"+searchParam.toString();
}

export default request