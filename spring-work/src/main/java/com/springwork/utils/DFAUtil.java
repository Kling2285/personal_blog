package com.springwork.utils;

import com.springwork.entity.Word;
import com.springwork.service.WordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DFA算法敏感词过滤工具类（最终可用版）
 * 完全消除类型转换错误，简化字典树设计
 */
@Slf4j
@Component
public class DFAUtil {
    // 敏感词集合
    private static Set<String> WORDS = new HashSet<>();
    // DFA字典树根节点（统一使用Object类型，避免泛型冲突）
    private static Map<Object, Object> ROOT_NODE = new ConcurrentHashMap<>();
    // 字典树结束标记
    public static final String END_FLAG = "END_FLAG";
    // 默认替换字符
    public static final char DEFAULT_REPLACEMENT = '*';
    // 默认高亮标签
    public static final String DEFAULT_START_TAG = "<font color=\"red\">";
    public static final String DEFAULT_END_TAG = "</font>";

    @Autowired
    private WordService wordService;

    /**
     * 项目启动初始化字典树
     */
    @Bean
    public ApplicationRunner initDFATree() {
        return args -> {
            List<Word> wordList = wordService.list();
            if (CollectionUtils.isEmpty(wordList)) {
                log.warn("DFA初始化：无敏感词数据");
                return;
            }

            WORDS.clear();
            ROOT_NODE.clear();
            for (Word word : wordList) {
                String sensitiveWord = word.getWord();
                if (StringUtils.hasText(sensitiveWord)) {
                    WORDS.add(sensitiveWord);
                    // 直接调用TreeGenerator构建字典树
                    TreeGenerator.addWordToTree(ROOT_NODE, sensitiveWord);
                }
            }
            log.info("DFA初始化完成，加载敏感词：{}个", WORDS.size());
        };
    }

    /**
     * 检测文本是否含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (!StringUtils.hasText(text)) return false;
        return !getSensitiveWords(text).isEmpty();
    }

    /**
     * 获取文本中所有敏感词
     */
    public Set<String> getSensitiveWords(String text) {
        Set<String> result = new HashSet<>();
        if (!StringUtils.hasText(text) || ROOT_NODE.isEmpty()) return result;

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            String word = findSensitiveWord(chars, i);
            if (StringUtils.hasText(word)) {
                result.add(word);
                i += word.length() - 1;
            }
        }
        return result;
    }


    /**
     * 查找敏感词核心方法
     */
    private String findSensitiveWord(char[] chars, int start) {
        Map<Object, Object> node = ROOT_NODE;
        StringBuilder sb = new StringBuilder();
        String matchWord = null;

        for (int i = start; i < chars.length; i++) {
            char c = chars[i];
            if (!node.containsKey(c)) break;

            sb.append(c);
            node = (Map<Object, Object>) node.get(c);

            // 匹配到结束标记
            if (node.containsKey(END_FLAG)) {
                matchWord = (String) node.get(END_FLAG);
            }
        }
        return matchWord;
    }

    /**
     * 替换敏感词
     */
    public String replaceSensitiveWord(String text) {
        return replaceSensitiveWord(text, DEFAULT_REPLACEMENT);
    }

    public String replaceSensitiveWord(String text, char replacement) {
        if (!StringUtils.hasText(text) || ROOT_NODE.isEmpty()) return text;

        char[] chars = text.toCharArray();
        boolean[] replaceMark = new boolean[chars.length];

        for (int i = 0; i < chars.length; i++) {
            String word = findSensitiveWord(chars, i);
            if (StringUtils.hasText(word)) {
                for (int j = 0; j < word.length() && (i + j) < chars.length; j++) {
                    replaceMark[i + j] = true;
                }
                i += word.length() - 1;
            }
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            result.append(replaceMark[i] ? replacement : chars[i]);
        }
        return result.toString();
    }

    /**
     * 高亮敏感词
     */
    public String highlightSensitiveWord(String text) {
        return highlightSensitiveWord(text, DEFAULT_START_TAG, DEFAULT_END_TAG);
    }

    public String highlightSensitiveWord(String text, String startTag, String endTag) {
        if (!StringUtils.hasText(text) || ROOT_NODE.isEmpty()) return text;

        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();
        int i = 0;

        while (i < chars.length) {
            String word = findSensitiveWord(chars, i);
            if (StringUtils.hasText(word)) {
                result.append(startTag).append(word).append(endTag);
                i += word.length();
            } else {
                result.append(chars[i]);
                i++;
            }
        }
        return result.toString();
    }

    /**
     * 刷新字典树
     */
    public void refreshDFATree() {
        log.info("开始刷新DFA字典树");
        List<Word> wordList = wordService.list();
        WORDS.clear();
        ROOT_NODE.clear();

        for (Word word : wordList) {
            String sensitiveWord = word.getWord();
            if (StringUtils.hasText(sensitiveWord)) {
                WORDS.add(sensitiveWord);
                TreeGenerator.addWordToTree(ROOT_NODE, sensitiveWord);
            }
        }
        log.info("DFA字典树刷新完成，当前敏感词数：{}", WORDS.size());
    }

    public int getSensitiveWordCount() {
        return WORDS.size();
    }
}