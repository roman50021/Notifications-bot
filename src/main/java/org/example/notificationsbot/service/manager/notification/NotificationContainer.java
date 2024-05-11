package org.example.notificationsbot.service.manager.notification;

import lombok.extern.slf4j.Slf4j;
import org.example.notificationsbot.bot.Bot;
import org.example.notificationsbot.model.Notification;
import org.example.notificationsbot.model.Status;
import org.example.notificationsbot.repository.NotificationRepository;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class NotificationContainer implements Runnable{
    private final Bot bot;
    private final Long chatId;
    private final Notification notification;
    private final NotificationRepository notificationRepository;

    public NotificationContainer(Bot bot,
                                 Long chatId,
                                 Notification notification,
                                 NotificationRepository notificationRepository
    ) {
        this.bot = bot;
        this.chatId = chatId;
        this.notification = notification;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(notification.getSeconds() * 1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        try {
            bot.execute(
                sendNotification()
            );
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        notification.setStatus(Status.FINISHED);
        notificationRepository.save(notification);
    }

    private BotApiMethod<?> sendNotification(){
        return SendMessage.builder()
                .chatId(chatId)
                .text(
                        "⚡\uFE0F Reminder : " + notification.getTitle() + "\n"
                        +"❗ " + notification.getDescription()
                )
                .build();
    }
}
