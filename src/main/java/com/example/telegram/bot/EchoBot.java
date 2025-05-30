package com.example.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * @projectName: exampletelegrambot
 * @package: com.example.telegram.bot
 * @className: ExampleTelegramBotApplication
 * @author: unnode
 * @description: 回声机器人实现类, 继承 TelegramLongPollingBot 以处理长轮询更新
 * @date: 2025/5/30 19:10
 * @version: 1.0
 */
@Component
public class EchoBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(EchoBot.class);

    @Value("${telegram.bot.name}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    // 定义页面常量
    private static final String PAGE_1_CALLBACK = "page:1";
    private static final String PAGE_2_CALLBACK = "page:2";
    private static final String PAGE_3_CALLBACK = "page:3";

    private static final String ACTION_NEXT = "action:next";
    private static final String ACTION_PREV = "action:prev";
    private static final String ACTION_FIRST = "action:first";
    private static final String ACTION_LAST = "action:last";
    private static final String ACTION_CONFIG = "action:config"; // 配置按钮，无实际功能

    /**
     * 当接收到新的更新时调用此方法
     * @param update 接收到的更新对象
     */
    @Override
    public void onUpdateReceived(Update update) {
        // 处理普通文本消息（例如 /start 命令来显示第一页）
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update.getMessage());
        }
        // 处理内联键盘回调
        else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    /**
     * 处理文本消息，通常用于启动UI
     * @param message 接收到的消息对象
     */
    private void handleTextMessage(Message message) {
        String messageText = message.getText();
        long chatId = message.getChatId();

        logger.info("Received message from chat ID {}: {}", chatId, messageText);

        if (messageText.equals("/start") || messageText.equals("/menu")) {
            // 发送带有第一页内联键盘的消息
            try {
                execute(createPageMessage(chatId, 1));
                logger.info("Sent initial page 1 menu to chat ID {}", chatId);
            } catch (TelegramApiException e) {
                logger.error("Failed to send initial message to chat ID {}: {}", chatId, e.getMessage(), e);
            }
        } else {
            // 对于其他文本消息，执行回声功能
            SendMessage echoMessage = new SendMessage();
            echoMessage.setChatId(String.valueOf(chatId));
            echoMessage.setText("你说了: " + messageText + "\n发送 /start 或 /menu 查看UI页面。");
            try {
                execute(echoMessage);
                logger.info("Sent echo message to chat ID {}: {}", chatId, echoMessage.getText());
            } catch (TelegramApiException e) {
                logger.error("Failed to send echo message to chat ID {}: {}", chatId, e.getMessage(), e);
            }
        }
    }

    /**
     * 处理内联键盘回调查询
     * @param callbackQuery 接收到的回调查询对象
     */
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callData = callbackQuery.getData();
        long messageId = callbackQuery.getMessage().getMessageId();
        long chatId = callbackQuery.getMessage().getChatId();

        logger.info("Received callback query from chat ID {} with data: {}", chatId, callData);

        int currentPage = 1; // 默认当前页为1，如果回调数据中没有明确指定页码，则从这里开始判断

        // 尝试从回调数据中解析出当前页码（如果存在）
        // 比如 "button:1-1" 或 "action:next" 这样的回调，需要知道当前是哪一页才能决定下一页是哪页
        // 这里我们简化处理，直接根据action决定跳转到哪一页
        if (callData.startsWith("button:")) {
            // 如果是按钮点击，我们假设它属于当前显示的页面，但这里不处理具体按钮逻辑
            // 可以在这里解析出是哪个页面的哪个按钮，但本例只关注页面跳转
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


        int targetPage = currentPage; // 默认目标页面是当前页面

        // 根据回调数据决定目标页面
        switch (callData) {
            case ACTION_NEXT:
                targetPage = Math.min(currentPage + 1, 3); // 最多3页
                break;
            case ACTION_PREV:
                targetPage = Math.max(currentPage - 1, 1); // 最少1页
                break;
            case ACTION_FIRST:
                targetPage = 1;
                break;
            case ACTION_LAST:
                targetPage = 3; // 假设总共3页
                break;
            case PAGE_1_CALLBACK:
                targetPage = 1;
                break;
            case PAGE_2_CALLBACK:
                targetPage = 2;
                break;
            case PAGE_3_CALLBACK:
                targetPage = 3;
                break;
            case ACTION_CONFIG:
                // 配置按钮，此处不进行页面跳转，可以弹出一个提示或者发送一个新消息
                logger.info("Config button clicked on page {}", currentPage);
                // 可以发送一个提示消息，或者根据需要更新当前消息
                // SendMessage configMessage = new SendMessage(String.valueOf(chatId), "你点击了配置按钮！");
                // try { execute(configMessage); } catch (TelegramApiException e) { /* handle error */ }
                return; // 不更新键盘，直接返回
            default:
                // 处理其他按钮点击，例如 "button:1-1"
                // 在这里可以根据 callData 的具体值执行子功能
                logger.info("Button clicked: {}", callData);
                // 假设点击常规按钮后，仍然停留在当前页面，并更新键盘
                // 需要从 callData 中解析出当前页面
                if (callData.startsWith("button:")) {
                    try {
                        targetPage = Integer.parseInt(callData.split(":")[1].split("-")[0]);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid page number in button callback data: {}", callData);
                    }
                }
                break;
        }

        // 构建新的内联键盘
        InlineKeyboardMarkup newKeyboard = createInlineKeyboardForPage(targetPage);

        // 创建 EditMessageReplyMarkup 对象来更新消息的键盘
        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(String.valueOf(chatId))
                .messageId((int) messageId) // messageId 必须是 int
                .replyMarkup(newKeyboard)
                .build();

        try {
            execute(editMessageReplyMarkup);
            logger.info("Updated message {} in chat {} to display page {}", messageId, chatId, targetPage);
        } catch (TelegramApiException e) {
            logger.error("Failed to edit message {} in chat {}: {}", messageId, chatId, e.getMessage(), e);
        }
    }

    /**
     * 根据页码创建对应的内联键盘
     * @param pageNum 当前页码
     * @return 对应页面的 InlineKeyboardMarkup
     */
    private InlineKeyboardMarkup createInlineKeyboardForPage(int pageNum) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        // 添加顶部6个按钮 (3x2 布局)
        for (int i = 0; i < 2; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
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
            navRow.add(InlineKeyboardButton.builder().text("尾页").callbackData(ACTION_LAST).build());
            navRow.add(InlineKeyboardButton.builder().text("配置").callbackData(ACTION_CONFIG).build());
            navRow.add(InlineKeyboardButton.builder().text("下一页").callbackData(ACTION_NEXT).build());
        } else if (pageNum == 2) {
            // 第二页：上一页 | 配置 | 下一页
            navRow.add(InlineKeyboardButton.builder().text("上一页").callbackData(ACTION_PREV).build());
            navRow.add(InlineKeyboardButton.builder().text("配置").callbackData(ACTION_CONFIG).build());
            navRow.add(InlineKeyboardButton.builder().text("下一页").callbackData(ACTION_NEXT).build());
        } else if (pageNum == 3) {
            // 第三页：上一页 | 配置 | 首页
            navRow.add(InlineKeyboardButton.builder().text("上一页").callbackData(ACTION_PREV).build());
            navRow.add(InlineKeyboardButton.builder().text("配置").callbackData(ACTION_CONFIG).build());
            navRow.add(InlineKeyboardButton.builder().text("首页").callbackData(ACTION_FIRST).build());
        }
        keyboardRows.add(navRow);

        markupInline.setKeyboard(keyboardRows);
        return markupInline;
    }

    /**
     * 创建一个包含指定页码内联键盘的 SendMessage 对象
     * @param chatId 聊天ID
     * @param pageNum 初始显示的页码
     * @return SendMessage 对象
     */
    private SendMessage createPageMessage(long chatId, int pageNum) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("当前是第 " + pageNum + " 页"); // 消息文本显示当前页码
        message.setReplyMarkup(createInlineKeyboardForPage(pageNum));
        return message;
    }


    /**
     * 返回机器人的用户名
     * @return 机器人的用户名
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * 返回机器人的令牌
     * @return 机器人的令牌
     */
    @Override
    public String getBotToken() {
        return botToken;
    }
}
