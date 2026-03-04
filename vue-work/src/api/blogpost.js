import request from "@/utils/request.js";

//查询
export function listpost(params) {
    return request.get('/post/list01', {
        params: params
    })
}

//随机展示
export function listpostRandom() {
    return request.get('/post/list-random');
}
//删除
export function deletepost(id) {
    return request.delete(`/post/${id}`);
}

//添加
export function addpost(data) {
    return request.post('/post', data);
}

//单个查询
export function selectonepost(id) {
    return request.get(`/post/${id}`);
}

//修改
export function updatepost(data) {
    return request.put('/post', data);
}