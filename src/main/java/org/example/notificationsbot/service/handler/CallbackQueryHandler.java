package org.example.notificationsbot.service.handler;

import lombok.RequiredArgsConstructor;
import org.example.notificationsbot.bot.Bot;
import org.example.notificationsbot.service.contract.AbstractHandler;
import org.example.notificationsbot.service.manager.MainManager;
import org.example.notificationsbot.service.manager.notification.NotificationManager;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class CallbackQueryHandler extends AbstractHandler {
    private final NotificationManager notificationManager;
    private final MainManager mainManager;
    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) throws TelegramApiException {
        var query = (CallbackQuery) object;
        String[] words = query.getData().split("_");
        switch (words[0]){
            case "notification" -> {
                return notificationManager.answerQuery(query, words, bot);
            }
            case "main" -> {
                return mainManager.answerQuery(query, words, bot);
            }
        }
        throw new UnsupportedOperationException();
    }
}
