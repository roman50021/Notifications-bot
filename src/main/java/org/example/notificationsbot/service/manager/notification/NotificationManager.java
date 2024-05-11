package org.example.notificationsbot.service.manager.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationsbot.bot.Bot;
import org.example.notificationsbot.model.Action;
import org.example.notificationsbot.model.Notification;
import org.example.notificationsbot.model.Status;
import org.example.notificationsbot.model.User;
import org.example.notificationsbot.repository.NotificationRepository;
import org.example.notificationsbot.repository.UserRepository;
import org.example.notificationsbot.service.contract.AbstractManager;
import org.example.notificationsbot.service.contract.CommandListener;
import org.example.notificationsbot.service.contract.MessageListener;
import org.example.notificationsbot.service.contract.QueryListener;
import org.example.notificationsbot.service.factory.KeyboardFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

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
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Настройте уведомление")
                .replyMarkup(
                        editNotificationReplyMarkup(
                                String.valueOf(
                                        userRepository.findByChatId(message.getChatId())
                                                .getCurrentNotification()
                                )
                        )
                )
                .build();
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
    public BotApiMethod<?> answerMessage(Message message, Bot bot) throws TelegramApiException {
        var user = userRepository.findByChatId(message.getChatId());
        bot.execute(
                DeleteMessage.builder()
                        .chatId(message.getChatId())
                        .messageId(message.getMessageId() - 1)
                        .build()
        );
        switch (user.getAction()) {
            case SENDING_TIME -> {
                return editTime(message, user, bot);
            }
            case SENDING_DESCRIPTION -> {
                return editDescription(message, user, bot);
            }
            case SENDING_TITLE -> {
                return editTitle(message, user, bot);
            }
        }
        return null;
    }

    private BotApiMethod<?> editTitle(Message message, User user, Bot bot) {
        var notification = notificationRepository.findById(user.getCurrentNotification()).orElseThrow();
        notification.setTitle(message.getText());
        notificationRepository.save(notification);

        user.setAction(Action.FREE);
        userRepository.save(user);
        return mainMenu(message, bot);
    }

    private BotApiMethod<?> editDescription(Message message, User user, Bot bot) {
        var notification = notificationRepository.findById(user.getCurrentNotification()).orElseThrow();
        notification.setDescription(message.getText());
        notificationRepository.save(notification);

        user.setAction(Action.FREE);
        userRepository.save(user);
        return mainMenu(message, bot);
    }

    private BotApiMethod<?> editTime(Message message, User user, Bot bot) {
        var notification = notificationRepository.findById(user.getCurrentNotification()).orElseThrow();

        var messageText = message.getText().strip();
        var pattern = Pattern.compile("^[0-9]{2}:[0-9]{2}:[0-9]{2}$").matcher(messageText);
        if (pattern.matches()) {
            var nums = messageText.split(":");
            long seconds = Long.parseLong(nums[0]) * 3600 + Long.parseLong(nums[1]) * 60 + Long.parseLong(nums[2]);
            notification.setSeconds(seconds);
        } else {
            return SendMessage.builder()
                    .text("Incorrect input format\nHH:MM:SS (01:00:30 - one hour, zero minutes, thirty seconds)")
                    .chatId(message.getChatId())
                    .replyMarkup(
                            keyboardFactory.createInlineKeyboard(
                                    List.of("\uD83D\uDD19 Back"),
                                    List.of(1),
                                    List.of(notification_back_ + String.valueOf(user.getCurrentNotification()))
                            )
                    )
                    .build();
        }

        notificationRepository.save(notification);
        user.setAction(Action.FREE);
        userRepository.save(user);
        return mainMenu(message, bot);
    }

    @Override
    public BotApiMethod<?> answerQuery(CallbackQuery query, String[] words, Bot bot) throws TelegramApiException{
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
            case 3 -> {
                switch (words[1]) {
                    case "back" -> {
                        return editPage(query, words[2]);
                    }
                    case "done" -> {
                        return sendNotification(query, words[2], bot);
                    }
                }
            }
            case 4 -> {
                switch (words[1]) {
                    case "edit" -> {
                        switch (words[2]){
                            case "title" -> {
                                return askTitle(query, words[3]);
                            }
                            case "d" -> {
                                return askDescription(query, words[3]);
                            }
                            case "time" -> {
                                return askSeconds(query, words[3]);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> sendNotification(CallbackQuery query, String id, Bot bot) throws TelegramApiException {
        var notification = notificationRepository.findById(UUID.fromString(id)).orElseThrow();
        if (notification.getTitle() == null  || notification.getTitle().isBlank() || notification.getSeconds() == null) {
            return AnswerCallbackQuery.builder()
                    .callbackQueryId(query.getId())
                    .text("Заполните обязательные значения: Заголовок и Время")
                    .build();
        }
        bot.execute(
                AnswerCallbackQuery.builder()
                        .text("Уведомление придет к вам через " + notification.getSeconds() + " секунд \uD83D\uDC40")
                        .callbackQueryId(query.getId())
                        .build()
        );
        notification.setStatus(Status.WAITING);
        notificationRepository.save(notification);
        Thread.startVirtualThread(
                new NotificationContainer(
                        bot,
                        query.getMessage().getChatId(),
                        notification,
                        notificationRepository
                )
        );
        return EditMessageText.builder()
                .text("✅ Успешно")
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("На главную"),
                                List.of(1),
                                List.of(main.name())

                        )
                )
                .build();
    }

    private BotApiMethod<?> editPage(CallbackQuery query, String id){
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Customize the notification")
                .replyMarkup(editNotificationReplyMarkup(id))
                .build();
    }

    private BotApiMethod<?> askSeconds(CallbackQuery query, String id) {
        var user = userRepository.findByChatId(query.getMessage().getChatId());
        user.setAction(Action.SENDING_TITLE);
        user.setCurrentNotification(UUID.fromString(id));
        userRepository.save(user);
        return EditMessageText.builder()
                .text("⚡\uFE0F Enter the time after which you would like to receive the reminder\nFormat - HH:MM:SS\nFor example - (01.:30:00) - one and a half hours")
                .messageId(query.getMessage().getMessageId())
                .chatId(query.getMessage().getChatId())
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("Back"),
                                List.of(1),
                                List.of(notification_back_ + id)
                        )
                )
                .build();
    }

    private BotApiMethod<?> askDescription(CallbackQuery query, String id) {
        var user = userRepository.findByChatId(query.getMessage().getChatId());
        user.setAction(Action.SENDING_DESCRIPTION);
        user.setCurrentNotification(UUID.fromString(id));
        userRepository.save(user);
        return EditMessageText.builder()
                .text("⚡\uFE0F Add or change the description, just write the text you would like to receive in chat.")
                .messageId(query.getMessage().getMessageId())
                .chatId(query.getMessage().getChatId())
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("Back"),
                                List.of(1),
                                List.of(notification_back_ + id)
                        )
                )
                .build();
    }
    

    private BotApiMethod<?> askTitle(CallbackQuery query, String id) {
        var user = userRepository.findByChatId(query.getMessage().getChatId());
        user.setAction(Action.SENDING_TIME);
        user.setCurrentNotification(UUID.fromString(id));
        userRepository.save(user);
        return EditMessageText.builder()
                .text("⚡\uFE0F Опишите краткий заголовок в следующем сообщение, чтобы вам было сразу понятно, что я вам напоминаю")
                .messageId(query.getMessage().getMessageId())
                .chatId(query.getMessage().getChatId())
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("Back"),
                                List.of(1),
                                List.of(notification_back_ + id)
                        )
                )
                .build();
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
