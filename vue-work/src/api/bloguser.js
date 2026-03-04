import request from "@/utils/request.js";

//查询
export function listuser(params) {
    return request.get('/user/list01', {
        params:params
    })
}


//删除
export function deleteuser(userId) {
    return request.delete(`/user/${userId}`);
}

//添加
export function adduser(data){
    return request.post('/user',data);
}

//单个查询
export function selectone(userId) {
    return request.get(`/user/${userId}`);
}

//修改
export function updateuser(data) {
    return request.put('/user', data);
}
