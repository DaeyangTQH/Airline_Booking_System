package com.group3.domain.repository;

import com.group3.domain.aggregate.Notification;
import com.group3.domain.vo.NotificationId;

public interface NotificationRepository {
    Notification save(Notification notification);
    Notification findById(NotificationId id);
}