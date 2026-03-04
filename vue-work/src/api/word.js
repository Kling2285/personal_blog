import request from "@/utils/request.js";

export function listword(params) {
    return request.get('/word/list01', {
        params: params
    })
}

// 删除敏感词
export function deleteword(id) {
    return request.delete(`/word/${id}`);
}

// 添加敏感词
export function addword(data) {
    return request.post('/word', data);
}

// 单个查询敏感词
export function selectoneword(id) {
    return request.get(`/word/${id}`);
}

// 修改敏感词
export function updateword(data) {
    return request.put('/word', data);
}