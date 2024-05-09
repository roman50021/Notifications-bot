package org.example.notificationsbot.service.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationsbot.bot.Bot;
import org.example.notificationsbot.model.Notification;
import org.example.notificationsbot.model.Status;
import org.example.notificationsbot.repository.NotificationRepository;
import org.example.notificationsbot.repository.UserRepository;
import org.example.notificationsbot.service.contract.AbstractManager;
import org.example.notificationsbot.service.contract.CommandListener;
import org.example.notificationsbot.service.contract.MessageListener;
import org.example.notificationsbot.service.contract.QueryListener;
import org.example.notificationsbot.service.factory.KeyboardFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.example.notificationsbot.data.CallbackData.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationManager extends AbstractManager
        implements QueryListener, CommandListener, MessageListener {

    private final KeyboardFactory keyboardFactory;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public BotApiMethod<?> mainMenu(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot) {
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("###")
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("Add notification"),
                                List.of(1),
                                List.of(notification_new.name())
                        )
                )
                .build();
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerQuery(CallbackQuery query, String[] words, Bot bot) {
        switch (words.length){
            case 2 -> {
                switch (words[1]){
                    case "main" -> {
                        return mainMenu(query, bot);
                    }
                    case "new" -> {
                        return newNotification(query, bot); 
                    }
                }
            }

        }
        return null;
    }

    private BotApiMethod<?> newNotification(CallbackQuery query, Bot bot) {

        var user = userRepository.findByChatId(query.getMessage().getChatId());
        String id = String.valueOf(notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .status(Status.BUILDING)
                .build()
        ).getId());
        log.info("1");
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Setting notification")
                .replyMarkup(editNotificationReplyMarkup(id))
                .build();
    }

    private InlineKeyboardMarkup editNotificationReplyMarkup(String id) {
        List<String> text = new ArrayList<>();

        var notification = notificationRepository.findById(UUID.fromString(id)).orElseThrow();

        if (notification.getTitle() != null && !notification.getTitle().isBlank()){
            text.add("✅ Title");
        }else {
            text.add("❌ Title");
        }

        if (notification.getSeconds() != null && notification.getSeconds() != 0){
            text.add("✅Time");
        }else {
            text.add("❌ Time");
        }

        if (notification.getDescription() != null && !notification.getDescription().isBlank()){
            text.add("✅ Description");
        }else {
            text.add("❌ Description");
        }
        text.add("Main");
        text.add("Done");
        log.info("3 = " + text.size());
        log.info("4 = " + id);

        return keyboardFactory.createInlineKeyboard(
                text,
                List.of(2,1,2),
                List.of(notification_edit_title_.name() + id,notification_edit_time_.name() + id,
                        notification_edit_d_.name() + id,
                        main.name(),
                        notification_done_.name() + id
                )
        );
    }
}
