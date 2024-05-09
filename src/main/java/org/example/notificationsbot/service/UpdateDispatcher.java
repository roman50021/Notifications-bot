package org.example.notificationsbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationsbot.bot.Bot;
import org.example.notificationsbot.model.Action;
import org.example.notificationsbot.model.User;
import org.example.notificationsbot.repository.UserRepository;
import org.example.notificationsbot.service.handler.CallbackQueryHandler;
import org.example.notificationsbot.service.handler.CommandHandler;
import org.example.notificationsbot.service.handler.MessageHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateDispatcher {

    private final MessageHandler messageHandler;
    private final CommandHandler commandHandler;
    private final CallbackQueryHandler queryHandler;
    private final UserRepository userRepository;

    public BotApiMethod<?> distribute(Update update, Bot bot) {
        if(update.hasCallbackQuery()){
            checkUser(update.getCallbackQuery().getMessage().getChatId());
            return queryHandler.answer(update.getCallbackQuery(), bot);
        }
        if(update.hasMessage()){
            var message = update.getMessage();
            checkUser(message.getChatId());
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

    private void checkUser(Long chatId) {
        if(userRepository.existsByChatId(chatId)){
            return;
        }
        userRepository.save(
                User.builder()
                        .action(Action.FREE)
                        .registeredAt(LocalDateTime.now())
                        .chatId(chatId)
                        .firstName("Yo")
                        .build()
        );
    }
}
