package com.druh.community.utils;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author DJY
 * @date 2023/3/11 22:41
 * @apiNote
 */

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode root = new TrieNode();

    // postconstruct注解的含义是 在调用构造器后执行此方法，就是说在创建实例后，就初始化前缀树
    @PostConstruct
    public void init() {
        // 写在try后面的括号里，会自动加上finally关闭InputStream, BufferedReader
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 把关键词添加进前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：", e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            // 如果子结点中没有c字符这个节点，则添加进去
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 指向子节点，进入下一轮循环
            tempNode = subNode;

            if (i == keyword.length() - 1) {
                tempNode.setKeyword(true);
            }
        }
    }

    /**
     * 输入待测字符串，返回处理完的字符串
     * @param text 待测字符串
     * @return 处理完的字符串
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        // 在text中遍历的开始指针
        int begin = 0;
        // 在text中遍历的结束指针
        int end = 0;
        // 在前缀树中遍历的指针
        TrieNode tempNode = root;
        // 用来存结果
        StringBuilder sb = new StringBuilder();

        // 开始遍历
        while (begin < text.length()) {
            if (end < text.length()) {

                char c = text.charAt(end);
                // 跳过符号
                if (isSymbol(c)) {
                    // 如果tempNode还没动，说明begin开始的字符串不是敏感词，begin后移
                    if (tempNode == root) {
                        begin++;
                        sb.append(c);
                    }
                    // 不管begin后不后移，end是肯定要移的，因为如果begin是敏感词的开头，那begin不用后移，要移动end，如果begin不是敏感词开头
                    // 那begin++， end肯定要至少和begin对齐，
                    end++;
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getSubNode(c);
                // begin开头的字符串不是敏感词
                if (tempNode == null) {
                    sb.append(text.charAt(begin));
                    // 移到下一个位置
                    end = ++begin;
                    // 前缀树的指针重置
                    tempNode = root;
                } else if (tempNode.isKeyword()) {
                    // 发现(begin, end)的字符是敏感词
                    sb.append(REPLACEMENT);
                    begin = ++end;
                } else {
                    // 还没到敏感词的结尾，检查下一个字符
                    end++;
                }
            } else {
                // end遍历越界仍未匹配到敏感词,说明(begin, end)的字符不是敏感词
                sb.append(text.charAt(begin));
                end = ++begin;
                tempNode = root;
            }
        }

        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // （0x2E80，0x9FFF）是东亚文字范围
        return !CharUtils.isAsciiNumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 前缀树节点
    private class TrieNode {
        // 关键词结束标识
        private boolean isKeyword = false;
        // 当前节点的子节点（key是子节点代表的字符，value是子节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyword() {
            return isKeyword;
        }
        // 设置当前路径的字符串为keyword
        public void setKeyword(boolean flag) {
            isKeyword = flag;
        }
        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

        public void setSubNodes(Map<Character, TrieNode> subNodes) {
            this.subNodes = subNodes;
        }
    }

}
