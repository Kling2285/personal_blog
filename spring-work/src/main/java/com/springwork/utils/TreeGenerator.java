package com.springwork.utils;

import java.util.Map;
import java.util.HashMap;

/**
 * 字典树构建工具类（最终可用版）
 * 完全适配DFAUtil的Map<Object, Object>类型，无任何编译错误
 */
public class TreeGenerator {

    /**
     * 核心方法：添加敏感词到字典树（唯一对外方法，避免重载冲突）
     */
    public static void addWordToTree(Map<Object, Object> rootNode, String sensitiveWord) {
        if (rootNode == null || sensitiveWord == null || sensitiveWord.isEmpty()) {
            return;
        }

        Map<Object, Object> currentNode = rootNode;
        char[] chars = sensitiveWord.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            // 不存在则创建新节点
            if (!currentNode.containsKey(c)) {
                currentNode.put(c, new HashMap<Object, Object>());
            }
            // 移动到子节点
            currentNode = (Map<Object, Object>) currentNode.get(c);
            // 最后一个字符标记结束并存储敏感词
            if (i == chars.length - 1) {
                currentNode.put(DFAUtil.END_FLAG, sensitiveWord);
            }
        }
    }
}