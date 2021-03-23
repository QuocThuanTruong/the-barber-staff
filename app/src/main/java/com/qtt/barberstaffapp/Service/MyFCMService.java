package com.qtt.barberstaffapp.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.qtt.barberstaffapp.Common.Common;

import java.util.Random;

public class MyFCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Common.updateToken(this, s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("TOKEN_CLIENT_APP", "ccc");
        Common.showNotification(this, new Random().nextInt(),
                remoteMessage.getData().get(Common.TITLE_KEY),
               remoteMessage.getData().get(Common.CONTENT_KEY),
                null);
    }
}
