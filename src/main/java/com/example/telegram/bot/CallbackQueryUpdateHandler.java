package com.example.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * @projectName: exampletelegrambot
 * @package: com.example.telegram.bot
 * @className: CallbackQueryUpdateHandler
 * @author: unnode
 * @description: 负责处理内联键盘回调查询，并管理页面跳转逻辑
 * @date: 2025/5/31 13:19
 * @version: 1.0
 */
@Component
public class CallbackQueryUpdateHandler implements UpdateHandler {

    private static final Logger logger = LoggerFactory.getLogger(CallbackQueryUpdateHandler.class);

    private final KeyboardService keyboardService;

    // 通过构造函数注入 KeyboardService
    public CallbackQueryUpdateHandler(KeyboardService keyboardService) {
        this.keyboardService = keyboardService;
    }

    @Override
    public boolean canHandle(Update update) {
        return update.hasCallbackQuery();
    }

    @Override
    public void handle(Update update, AbsSender absSender) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callData = callbackQuery.getData();
        long messageId = callbackQuery.getMessage().getMessageId();
        long chatId = callbackQuery.getMessage().getChatId();

        logger.info("Received callback query from chat ID {} with data: {}", chatId, callData);

        int currentPage = 1; // Default current page, will be updated based on callback data
        // Attempt to parse current page from callback data if it's a button click
        if (callData.startsWith("button:")) {
            String[] parts = callData.split(":");
            if (parts.length > 1 && parts[1].contains("-")) {
                try {
                    currentPage = Integer.parseInt(parts[1].split("-")[0]);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid page number in button callback data: {}", callData);
                }
            }
        } else if (callData.startsWith("page:")) {
            try {
                currentPage = Integer.parseInt(callData.split(":")[1]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid page number in page callback data: {}", callData);
            }
        }


        int targetPage = currentPage; // Default target page is current page

        // Determine target page based on callback data
        switch (callData) {
            case KeyboardService.ACTION_NEXT:
                targetPage = Math.min(currentPage + 1, 3); // Max 3 pages
                break;
            case KeyboardService.ACTION_PREV:
                targetPage = Math.max(currentPage - 1, 1); // Min 1 page
                break;
            case KeyboardService.ACTION_FIRST:
                targetPage = 1;
                break;
            case KeyboardService.ACTION_LAST:
                targetPage = 3; // Assuming 3 pages in total
                break;
            case KeyboardService.PAGE_1_CALLBACK:
                targetPage = 1;
                break;
            case KeyboardService.PAGE_2_CALLBACK:
                targetPage = 2;
                break;
            case KeyboardService.PAGE_3_CALLBACK:
                targetPage = 3;
                break;
            case KeyboardService.ACTION_CONFIG:
                // Config button, no page transition here, can send a prompt or new message
                logger.info("Config button clicked on page {}", currentPage);
                // Example: Send a temporary message
                // SendMessage configMessage = new SendMessage(String.valueOf(chatId), "你点击了配置按钮！");
                // try { absSender.execute(configMessage); } catch (TelegramApiException e) { /* handle error */ }
                return; // Do not update keyboard, return directly
            default:
                // Handle other button clicks, e.g., "button:1-1"
                // Here you can execute sub-functions based on the specific callData value
                logger.info("Button clicked: {}", callData);
                // Assume after clicking a regular button, stay on the current page and update the keyboard
                // Need to parse the current page from callData
                if (callData.startsWith("button:")) {
                    try {
                        targetPage = Integer.parseInt(callData.split(":")[1].split("-")[0]);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid page number in button callback data: {}", callData);
                    }
                }
                break;
        }

        // Build new inline keyboard
        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(String.valueOf(chatId))
                .messageId((int) messageId) // messageId must be int
                .replyMarkup(keyboardService.createInlineKeyboardForPage(targetPage))
                .build();

        try {
            absSender.execute(editMessageReplyMarkup);
            logger.info("Updated message {} in chat {} to display page {}", messageId, chatId, targetPage);
        } catch (TelegramApiException e) {
            logger.error("Failed to edit message {} in chat {}: {}", messageId, chatId, e.getMessage(), e);
        }
    }
}
