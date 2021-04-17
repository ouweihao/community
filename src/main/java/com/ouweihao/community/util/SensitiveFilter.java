package com.ouweihao.community.util;

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

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 初始化

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyWord = null;
            while ((keyWord = bufferedReader.readLine()) != null) {
                // 把关键词添加到前缀树中
                this.addKeyword(keyWord);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件异常：" + e.getMessage());
        }

    }

    // 将一个敏感词添加到前缀树中

    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;

        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);

            TrieNode subNode = tempNode.getSubNode(c);

            // 若该字符对应的节点为空，则创建一个新节点，并把新节点挂载在当前节点之下
            if (subNode == null) {
                // 初始化节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 指针指向子节点开始下轮循环
            tempNode = subNode;

            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }

        }
    }

    /**
     * 过滤传进来文本中的敏感词
     *
     * @param text 待过滤的文本
     * @return 已过滤的文本，其中敏感词用其他字符代替
     */
    public String filter(String text) {

        // 空值检查
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 已过滤的字符串
        StringBuilder sb = new StringBuilder();

        // 前缀树指针
        TrieNode tempNode = rootNode;
        // 文本指针1
        int begin = 0;
        // 文本指针2
        int position = 0;

        while (position < text.length()) {

            char c = text.charAt(position);

            // 判断该字符是不是符号
            if (isSymbol(c)) {

                // 若树的指针指向开始，则将该字符记入结果
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }

                // 无论如何，文本指针2都要向后移动一位
                position++;
                continue;

            }

            // 检查是否有节点的值为文本指针2所指向的值
            tempNode = tempNode.getSubNode(c);

            // 中断，从begin至position的字符不是敏感字符
            if (tempNode == null) {
                // charAt(begin)不是敏感字符，所以可以加入结果
                sb.append(text.charAt(begin));
                // begin后移，position指向begin
                begin++;
                position = begin;
                // position = ++begin;
                // 树指针归位
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 替换
                sb.append(REPLACEMENT);
                // 指针后移
                begin = ++position;
                // 树指针指向根
                tempNode = rootNode;
            } else {
                position++;
            }

        }

        // position指向最后一个字符，但其任然不是敏感字符，将其加入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // 判断是否是符号

    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF是东亚文字的范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 前缀树
    private class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点(Key为下级的字符，value为字符对应的节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 得到子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }

}
