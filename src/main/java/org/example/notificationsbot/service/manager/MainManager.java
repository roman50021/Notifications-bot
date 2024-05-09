package org.example.notificationsbot.service.manager;

import lombok.RequiredArgsConstructor;
import org.example.notificationsbot.bot.Bot;
import org.example.notificationsbot.service.contract.AbstractManager;
import org.example.notificationsbot.service.contract.CommandListener;
import org.example.notificationsbot.service.contract.QueryListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class MainManager extends AbstractManager implements CommandListener, QueryListener {
    @Override
    public BotApiMethod<?> mainMenu(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return greetings(message.getChatId());
    }

    @Override
    public BotApiMethod<?> answerQuery(CallbackQuery query, Bot bot) {
        return null;
    }

    private BotApiMethod<?> greetings(Long chatId){
        return SendMessage.builder()
                .chatId(chatId)
                .text("Hello my dear friend!")
                .build();
    }
}
