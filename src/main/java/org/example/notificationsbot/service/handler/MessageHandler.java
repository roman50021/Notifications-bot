package org.example.notificationsbot.service.handler;

import lombok.RequiredArgsConstructor;
import org.example.notificationsbot.bot.Bot;
import org.example.notificationsbot.repository.UserRepository;
import org.example.notificationsbot.service.contract.AbstractHandler;
import org.example.notificationsbot.service.manager.notification.NotificationManager;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class MessageHandler extends AbstractHandler {
    private final UserRepository userRepository;
    private final NotificationManager notificationManager;
    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) throws TelegramApiException {
        var message = (Message) object;
        var user = userRepository.findByChatId(message.getChatId());
        switch (user.getAction()){
            case FREE -> {
                return null;
            }
            case SENDING_TIME, SENDING_DESCRIPTION, SENDING_TITLE -> {
                return notificationManager.answerMessage(message, bot);
            }
        }
        throw new UnsupportedOperationException();
    }
}
