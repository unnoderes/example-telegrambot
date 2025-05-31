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
@Service
public class KeyboardService {

    // 定义页面常量
    public static final String PAGE_1_CALLBACK = "page:1";
    public static final String PAGE_2_CALLBACK = "page:2";
    public static final String PAGE_3_CALLBACK = "page:3";

    public static final String ACTION_NEXT = "action:next";
    public static final String ACTION_PREV = "action:prev";
    public static final String ACTION_FIRST = "action:first";
    public static final String ACTION_LAST = "action:last";
    public static final String ACTION_CONFIG = "action:config"; // 配置按钮，无实际功能

    /**
     * 根据页码创建对应的内联键盘。
     * @param pageNum 当前页码。
     * @return 对应页面的 InlineKeyboardMarkup。
     */
    public InlineKeyboardMarkup createInlineKeyboardForPage(int pageNum) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        // Add top 6 buttons (3x2 layout)
        for (int i = 0; i < 2; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                row.add(InlineKeyboardButton.builder()
                        .text("BUTTON " + (pageNum * 10 + i * 3 + j + 1)) // Example button text
                        .callbackData("button:" + pageNum + "-" + (i * 3 + j + 1)) // Example callback data
                        .build());
            }
            keyboardRows.add(row);
        }

        // Add bottom navigation buttons
        List<InlineKeyboardButton> navRow = new ArrayList<>();
        if (pageNum == 1) {
            // Page 1: Last Page | Config | Next Page
            navRow.add(InlineKeyboardButton.builder().text("尾页").callbackData(ACTION_LAST).build());
            navRow.add(InlineKeyboardButton.builder().text("配置").callbackData(ACTION_CONFIG).build());
            navRow.add(InlineKeyboardButton.builder().text("下一页").callbackData(ACTION_NEXT).build());
        } else if (pageNum == 2) {
            // Page 2: Previous Page | Config | Next Page
            navRow.add(InlineKeyboardButton.builder().text("上一页").callbackData(ACTION_PREV).build());
            navRow.add(InlineKeyboardButton.builder().text("配置").callbackData(ACTION_CONFIG).build());
            navRow.add(InlineKeyboardButton.builder().text("下一页").callbackData(ACTION_NEXT).build());
        } else if (pageNum == 3) {
            // Page 3: Previous Page | Config | Home Page
            navRow.add(InlineKeyboardButton.builder().text("上一页").callbackData(ACTION_PREV).build());
            navRow.add(InlineKeyboardButton.builder().text("配置").callbackData(ACTION_CONFIG).build());
            navRow.add(InlineKeyboardButton.builder().text("首页").callbackData(ACTION_FIRST).build());
        }
        keyboardRows.add(navRow);

        markupInline.setKeyboard(keyboardRows);
        return markupInline;
    }
}