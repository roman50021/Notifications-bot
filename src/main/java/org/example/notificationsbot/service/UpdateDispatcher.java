package org.example.notificationsbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationsbot.bot.Bot;
import org.example.notificationsbot.service.handler.CallbackQueryHandler;
import org.example.notificationsbot.service.handler.CommandHandler;
import org.example.notificationsbot.service.handler.MessageHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateDispatcher {

    private final MessageHandler messageHandler;
    private final CommandHandler commandHandler;
    private final CallbackQueryHandler queryHandler;

    public BotApiMethod<?> distribute(Update update, Bot bot) {
        if(update.hasCallbackQuery()){
            return queryHandler.answer(update.getCallbackQuery(), bot);
        }
        if(update.hasMessage()){
            var message = update.getMessage();
            if(message.hasText()){
                if(message.getText().charAt(0) == '/'){
                    return commandHandler.answer(message, bot);
                }
                return messageHandler.answer(message, bot);
            }
        }
        log.warn("Unsupported update type: " + update);
        return null;
    }
}
