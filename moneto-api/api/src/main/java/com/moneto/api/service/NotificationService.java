package com.moneto.api.service;

import com.moneto.api.model.Notification;
import com.moneto.api.model.User;
import com.moneto.api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public List<Notification> getAllNotifications() {
        User user = userService.getCurrentUser();
        return notificationRepository.findByUserId(user.getId());
    }

    public List<Notification> getUnreadNotifications() {
        User user = userService.getCurrentUser();
        return notificationRepository.findByUserIdAndIsReadFalse(user.getId());
    }

    public Notification createNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setIsRead(false);
        return notificationRepository.save(notification);
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        User user = userService.getCurrentUser();
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalse(user.getId());
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}