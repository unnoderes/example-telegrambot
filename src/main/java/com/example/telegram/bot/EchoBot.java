package com.example.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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

    // 从 application.properties 中注入机器人名称和令牌
    @Value("${telegram.bot.name}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    /**
     * 当接收到新的更新时调用此方法
     * @param update 接收到的更新对象
     */
    @Override
    public void onUpdateReceived(Update update) {
        // 检查更新是否包含消息且消息有文本内容
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            logger.info("Received message from chat ID {}: {}", chatId, messageText);

            // 创建一个 SendMessage 对象来回复用户
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId)); // 设置聊天ID
            message.setText("你说了: " + messageText); // 设置回复文本

            try {
                // 执行发送消息操作
                execute(message);
                logger.info("Sent echo message to chat ID {}: {}", chatId, message.getText());
            } catch (TelegramApiException e) {
                logger.error("Failed to send message to chat ID {}: {}", chatId, e.getMessage(), e);
            }
        }
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
