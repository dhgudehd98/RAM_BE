package com.sh.Ram.notification.repository;

import com.sh.Ram.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
