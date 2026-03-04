import request from "@/utils/request.js";

// 公告条件查询
export function listnotice(params) {
    return request.get('/notice/list01', {
        params: params
    })
}

// 公告删除
export function deletenotice(id) {
    return request.delete(`/notice/${id}`);
}

// 公告添加
export function addnotice(data) {
    return request.post('/notice', data);
}

// 公告单个查询
export function selectonenotice(id) {
    return request.get(`/notice/${id}`);
}

// 公告修改
export function updatenotice(data) {
    return request.put('/notice', data);
}
