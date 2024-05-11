package org.example.notificationsbot.service.manager;

import lombok.RequiredArgsConstructor;
import org.example.notificationsbot.bot.Bot;
import org.example.notificationsbot.service.contract.AbstractManager;
import org.example.notificationsbot.service.contract.CommandListener;
import org.example.notificationsbot.service.contract.QueryListener;
import org.example.notificationsbot.service.factory.KeyboardFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static org.example.notificationsbot.data.CallbackData.notification_main;

@Service
@RequiredArgsConstructor
public class MainManager extends AbstractManager implements CommandListener, QueryListener {
    private final KeyboardFactory keyboardFactory;

    @Override
    public BotApiMethod<?> mainMenu(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot) {
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Приветствую, дорогой друг!")
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("Напоминалки"),
                                List.of(1),
                                List.of(notification_main.name())
                        )
                )
                .build();
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Приветствую, дорогой друг!")
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("Напоминалки"),
                                List.of(1),
                                List.of(notification_main.name())
                        )
                )
                .build();
    }

    @Override
    public BotApiMethod<?> answerQuery(CallbackQuery query, String[] words, Bot bot) {
        return mainMenu(query, bot);
    }

}
