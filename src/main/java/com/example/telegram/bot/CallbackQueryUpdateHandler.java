package com.example.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText; // <--- 导入这个类
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * 负责处理内联键盘回调查询，并管理页面跳转逻辑。
 */
@Component
public class CallbackQueryUpdateHandler implements UpdateHandler {

    private static final Logger logger = LoggerFactory.getLogger(CallbackQueryUpdateHandler.class);

    private final KeyboardService keyboardService;

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

        int currentPage = 1; // 默认回退值，应该总会被回调数据覆盖
        int targetPage = 1;  // 默认目标页

        String[] dataParts = callData.split(":"); // 例如："action:next:2" 或 "button:1-1"

        // 安全地从 action 回调的第三部分或 button 回调的第二部分中解析当前页码
        if (dataParts.length > 2 && dataParts[0].equals("action")) {
            try {
                currentPage = Integer.parseInt(dataParts[2]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid current page number in action callback data: {}", callData);
            }
        } else if (dataParts.length > 1 && dataParts[0].equals("button") && dataParts[1].contains("-")) {
            try {
                currentPage = Integer.parseInt(dataParts[1].split("-")[0]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid page number in button callback data: {}", callData);
            }
        } else if (dataParts.length > 1 && dataParts[0].equals("page")) { // 直接页面跳转回调
            try {
                currentPage = Integer.parseInt(dataParts[1]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid page number in direct page callback data: {}", callData);
            }
        }

        // 初始化目标页为当前页，然后根据动作调整
        targetPage = currentPage;

        String actionIdentifier = dataParts[0] + (dataParts.length > 1 ? ":" + dataParts[1] : ""); // 构造动作标识符，例如 "action:next"

        // 根据回调数据决定目标页
        switch (actionIdentifier) {
            case KeyboardService.ACTION_NEXT_PREFIX:
                targetPage = Math.min(currentPage + 1, 3); // 最多3页
                break;
            case KeyboardService.ACTION_PREV_PREFIX:
                targetPage = Math.max(currentPage - 1, 1); // 最少1页
                break;
            case KeyboardService.ACTION_FIRST_PREFIX:
                targetPage = 1;
                break;
            case KeyboardService.ACTION_LAST_PREFIX:
                targetPage = 3; // 假设总共3页
                break;
            case KeyboardService.ACTION_CONFIG_PREFIX:
                logger.info("Config button clicked on page {}", currentPage);
                // 选项：发送一个回答回调查询，给用户反馈（例如一个短暂的通知）
                // try { absSender.execute(AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.getId()).text("配置功能待实现").showAlert(false).build()); } catch (TelegramApiException e) { logger.error("Failed to answer callback query: {}", e.getMessage(), e); }
                return; // 不更新键盘/页面，直接返回
            case KeyboardService.PAGE_1_CALLBACK: // 直接页面跳转回调
                targetPage = 1;
                break;
            case KeyboardService.PAGE_2_CALLBACK:
                targetPage = 2;
                break;
            case KeyboardService.PAGE_3_CALLBACK:
                targetPage = 3;
                break;
            default:
                // 处理通用按钮 (例如："button:X-Y")
                if (actionIdentifier.startsWith("button:")) {
                    logger.info("Generic button clicked: {}", callData);
                    // 对于通用按钮，默认停留在同一页面。targetPage 已经设置为 currentPage。
                } else {
                    logger.warn("Unknown callback data received: {}", callData);
                    return; // 未知回调，不做任何处理
                }
                break;
        }

        // 使用 EditMessageText 同时修改消息文本和回复键盘
        EditMessageText editMessageText = EditMessageText.builder()
                .chatId(String.valueOf(chatId))
                .messageId((int) messageId)
                .text("当前是第 " + targetPage + " 页") // 更新文本以反映新页码
                .replyMarkup(keyboardService.createInlineKeyboardForPage(targetPage))
                .build();

        try {
            absSender.execute(editMessageText);
            logger.info("Updated message {} in chat {} to display page {}", messageId, chatId, targetPage);
        } catch (TelegramApiException e) {
            logger.error("Failed to edit message {} in chat {}: {}", messageId, chatId, e.getMessage(), e);
        }
    }
}