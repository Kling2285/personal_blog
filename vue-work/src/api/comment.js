import request from "@/utils/request.js";

// 条件查询评论（支持按帖子ID/用户ID/父评论ID筛选）
export function listcomment(params) {
    return request.get('/comment/list01', {
        params: params
    })
}

// 删除评论
export function deletecomment(id) {
    return request.delete(`/comment/${id}`);
}

// 添加评论
export function addcomment(data){
    return request.post('/comment', data);
}

// 单个查询评论
export function selectonecomment(id) {
    return request.get(`/comment/${id}`);
}

// 额外补充：查询所有评论（适配后端/list接口，可选）
export function listallcomment() {
    return request.get('/comment/list');
}