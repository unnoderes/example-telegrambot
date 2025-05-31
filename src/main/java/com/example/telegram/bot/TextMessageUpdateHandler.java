package com.example.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * @projectName: exampletelegrambot
 * @package: com.example.telegram.bot
 * @className: TextMessageUpdateHandler
 * @author: unnode
 * @description: 负责处理文本消息，特别是启动UI的命令
 * @date: 2025/5/31 13:18
 * @version: 1.0
 */
@Component
public class TextMessageUpdateHandler implements UpdateHandler {

    private static final Logger logger = LoggerFactory.getLogger(TextMessageUpdateHandler.class);

    private final KeyboardService keyboardService;

    // 通过构造函数注入 KeyboardService
    public TextMessageUpdateHandler(KeyboardService keyboardService) {
        this.keyboardService = keyboardService;
    }

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    @Override
    public void handle(Update update, AbsSender absSender) {
        Message message = update.getMessage();
        String messageText = message.getText();
        long chatId = message.getChatId();

        logger.info("Received text message from chat ID {}: {}", chatId, messageText);

        if (messageText.equals("/start") || messageText.equals("/menu")) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText("欢迎回来！这是您的主菜单。"); // 初始欢迎消息
            sendMessage.setReplyMarkup(keyboardService.createReplyKeyboard()); // <-- 设置回复键盘

            try {
                absSender.execute(sendMessage);
                logger.info("Sent initial welcome message with reply keyboard to chat ID {}", chatId);

                // 接着发送带有内联键盘的页面1消息
                SendMessage inlineKeyboardMessage = new SendMessage();
                inlineKeyboardMessage.setChatId(String.valueOf(chatId));
                inlineKeyboardMessage.setText("当前是第 1 页"); // 初始内联键盘消息
                inlineKeyboardMessage.setReplyMarkup(keyboardService.createInlineKeyboardForPage(1)); // <-- 设置内联键盘

                absSender.execute(inlineKeyboardMessage);
                logger.info("Sent initial page 1 menu to chat ID {}", chatId);

            } catch (TelegramApiException e) {
                logger.error("Failed to send initial messages to chat ID {}: {}", chatId, e.getMessage(), e);
            }
        } else if (messageText.equals("🏠 首页")) { // <--- 处理点击回复键盘上的“首页”
            // 当用户点击“首页”按钮时，机器人会收到一条文本消息
            // 这里可以执行相应的功能，例如重新发送主菜单的内联键盘
            SendMessage homePageMessage = new SendMessage();
            homePageMessage.setChatId(String.valueOf(chatId));
            homePageMessage.setText("您已回到首页。当前是第 1 页");
            homePageMessage.setReplyMarkup(keyboardService.createInlineKeyboardForPage(1));
            try {
                absSender.execute(homePageMessage);
                logger.info("Responded to '首页' click for chat ID {}", chatId);
            } catch (TelegramApiException e) {
                logger.error("Failed to respond to '首页' click for chat ID {}: {}", chatId, e.getMessage(), e);
            }
        } else if (messageText.equals("📦 我的订单")) { // <--- 处理“我的订单”
            SendMessage orderMessage = new SendMessage();
            orderMessage.setChatId(String.valueOf(chatId));
            orderMessage.setText("您点击了“我的订单”。请稍候，正在查询您的订单信息...");
            // 这里可以加入查询订单的逻辑
            try {
                absSender.execute(orderMessage);
            } catch (TelegramApiException e) {
                logger.error("Failed to respond to '我的订单' click for chat ID {}: {}", chatId, e.getMessage(), e);
            }
        } else if (messageText.equals("💰 邀请返利")) { // <--- 处理“邀请返利”
            SendMessage inviteMessage = new SendMessage();
            inviteMessage.setChatId(String.valueOf(chatId));
            inviteMessage.setText("您点击了“邀请返利”。邀请您的朋友加入我们，获取返利！");
            // 这里可以加入邀请返利的逻辑
            try {
                absSender.execute(inviteMessage);
            } catch (TelegramApiException e) {
                logger.error("Failed to respond to '邀请返利' click for chat ID {}: {}", chatId, e.getMessage(), e);
            }
        }
        else {
            // Echo function for other text messages
            SendMessage echoMessage = new SendMessage();
            echoMessage.setChatId(String.valueOf(chatId));
            echoMessage.setText("你说了: " + messageText + "\n发送 /start 或 /menu 查看UI页面。");
            // 可以选择在这里也显示回复键盘
            echoMessage.setReplyMarkup(keyboardService.createReplyKeyboard());
            try {
                absSender.execute(echoMessage);
                logger.info("Sent echo message to chat ID {}: {}", chatId, echoMessage.getText());
            } catch (TelegramApiException e) {
                logger.error("Failed to send echo message to chat ID {}: {}", chatId, e.getMessage(), e);
            }
        }
    }
}