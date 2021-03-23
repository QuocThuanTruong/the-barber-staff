package com.qtt.barberstaffapp.Interface;

import com.google.firebase.firestore.DocumentSnapshot;
import com.qtt.barberstaffapp.Model.MyNotification;

import java.util.List;

public interface INotificationLoadListener {
    void onNotificationLoadSuccess(List<MyNotification> notificationList, DocumentSnapshot lastDocument);
    void onNotificationLoadFailed(String message);
}
