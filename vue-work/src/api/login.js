import request from "@/utils/request.js";

// 原有：用户名+密码登录
export function loginByPassword(data) {
    return request.post("/login/byPassword", data); // JSON格式传参（和你原有逻辑一致）
}

// 新增：邮箱+验证码登录
export function loginByEmail(data) {
    return request.post("/login/byEmail", data); // JSON格式传参（无需FormData）
}
