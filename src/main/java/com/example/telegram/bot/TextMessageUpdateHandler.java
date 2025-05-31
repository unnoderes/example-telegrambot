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
            // Send message with the first page inline keyboard
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText("当前是第 1 页"); // Message text displays current page number
            sendMessage.setReplyMarkup(keyboardService.createInlineKeyboardForPage(1));

            try {
                absSender.execute(sendMessage);
                logger.info("Sent initial page 1 menu to chat ID {}", chatId);
            } catch (TelegramApiException e) {
                logger.error("Failed to send initial message to chat ID {}: {}", chatId, e.getMessage(), e);
            }
        } else {
            // Echo function for other text messages
            SendMessage echoMessage = new SendMessage();
            echoMessage.setChatId(String.valueOf(chatId));
            echoMessage.setText("你说了: " + messageText + "\n发送 /start 或 /menu 查看UI页面。");
            try {
                absSender.execute(echoMessage);
                logger.info("Sent echo message to chat ID {}: {}", chatId, echoMessage.getText());
            } catch (TelegramApiException e) {
                logger.error("Failed to send echo message to chat ID {}: {}", chatId, e.getMessage(), e);
            }
        }
    }
}
