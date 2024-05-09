package org.example.notificationsbot.service.handler;

import lombok.RequiredArgsConstructor;
import org.example.notificationsbot.bot.Bot;
import org.example.notificationsbot.service.contract.AbstractHandler;
import org.example.notificationsbot.service.manager.MainManager;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class CommandHandler extends AbstractHandler {
    private  final MainManager mainManager;
    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) {
        var message = (Message) object;
        if("/start".equals(message.getText())){
            return mainManager.answerCommand(message, bot);
        }
        throw new UnsupportedOperationException();
    }
}
