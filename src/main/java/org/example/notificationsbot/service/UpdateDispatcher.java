package org.example.notificationsbot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class UpdateDispatcher {
    public BotApiMethod<?> distribute(Update update) {
        throw new UnsupportedOperationException();
    }
}
