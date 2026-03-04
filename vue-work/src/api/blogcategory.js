import request from "@/utils/request.js";

// 分类列表查询
export function listcategory(params) {
    return request.get('/category/list', {
        params: params
    })
}

// 删除分类
export function deletecategory(id) {
    return request.delete(`/category/${id}`);
}

// 添加分类
export function addcategory(data) {
    return request.post('/category', data);
}

// 单个分类查询
export function selectonecategory(id) {
    return request.get(`/category/${id}`);
}

// 修改分类
export function updatecategory(data) {
    return request.put('/category', data);
}
