package com.example.telegram.bot;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**
 * @projectName: exampletelegrambot
 * @package: com.example.telegram.bot
 * @className: KeyboardService
 * @author: unnode
 * @description: TODO
 * @date: 2025/5/31 13:16
 * @version: 1.0
 */

/**
 * 负责生成不同页面的内联键盘。
 */
@Service
public class KeyboardService {

    // 定义动作前缀。当前页码将附加到这些前缀后面。
    public static final String ACTION_NEXT_PREFIX = "action:next";
    public static final String ACTION_PREV_PREFIX = "action:prev";
    public static final String ACTION_FIRST_PREFIX = "action:first";
    public static final String ACTION_LAST_PREFIX = "action:last";
    public static final String ACTION_CONFIG_PREFIX = "action:config"; // 配置按钮，无实际功能

    // 定义直接页面跳转回调 (如果需要，通常导航按钮就足够了)
    public static final String PAGE_1_CALLBACK = "page:1";
    public static final String PAGE_2_CALLBACK = "page:2";
    public static final String PAGE_3_CALLBACK = "page:3";

    /**
     * 根据页码创建对应的内联键盘。
     * @param pageNum 当前页码。
     * @return 对应页面的 InlineKeyboardMarkup。
     */
    public InlineKeyboardMarkup createInlineKeyboardForPage(int pageNum) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        // 添加顶部6个按钮 (3x2 布局)
        for (int i = 0; i < 2; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                // 按钮回调数据包含当前页码作为上下文 (例如："button:1-1")
                row.add(InlineKeyboardButton.builder()
                        .text("BUTTON " + (pageNum * 10 + i * 3 + j + 1)) // 示例按钮文本
                        .callbackData("button:" + pageNum + "-" + (i * 3 + j + 1)) // 示例回调数据
                        .build());
            }
            keyboardRows.add(row);
        }

        // 添加底部导航按钮
        List<InlineKeyboardButton> navRow = new ArrayList<>();
        if (pageNum == 1) {
            // 第一页：尾页 | 配置 | 下一页
            navRow.add(InlineKeyboardButton.builder().text("尾页").callbackData(ACTION_LAST_PREFIX + ":" + pageNum).build()); // 添加 pageNum
            navRow.add(InlineKeyboardButton.builder().text("配置").callbackData(ACTION_CONFIG_PREFIX + ":" + pageNum).build()); // 添加 pageNum
            navRow.add(InlineKeyboardButton.builder().text("下一页").callbackData(ACTION_NEXT_PREFIX + ":" + pageNum).build()); // 添加 pageNum
        } else if (pageNum == 2) {
            // 第二页：上一页 | 配置 | 下一页
            navRow.add(InlineKeyboardButton.builder().text("上一页").callbackData(ACTION_PREV_PREFIX + ":" + pageNum).build()); // 添加 pageNum
            navRow.add(InlineKeyboardButton.builder().text("配置").callbackData(ACTION_CONFIG_PREFIX + ":" + pageNum).build()); // 添加 pageNum
            navRow.add(InlineKeyboardButton.builder().text("下一页").callbackData(ACTION_NEXT_PREFIX + ":" + pageNum).build()); // 添加 pageNum
        } else if (pageNum == 3) {
            // 第三页：上一页 | 配置 | 首页
            navRow.add(InlineKeyboardButton.builder().text("上一页").callbackData(ACTION_PREV_PREFIX + ":" + pageNum).build()); // 添加 pageNum
            navRow.add(InlineKeyboardButton.builder().text("配置").callbackData(ACTION_CONFIG_PREFIX + ":" + pageNum).build()); // 添加 pageNum
            navRow.add(InlineKeyboardButton.builder().text("首页").callbackData(ACTION_FIRST_PREFIX + ":" + pageNum).build()); // 添加 pageNum
        }
        keyboardRows.add(navRow);

        markupInline.setKeyboard(keyboardRows);
        return markupInline;
    }
}