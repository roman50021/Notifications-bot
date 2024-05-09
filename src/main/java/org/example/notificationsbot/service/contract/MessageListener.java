package org.example.notificationsbot.service.contract;

import org.example.notificationsbot.bot.Bot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageListener {
    BotApiMethod<?> answerMessage(Message message, Bot bot);
}
