package com.challenge.ct.mbs.google.cloudmessage;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.challenge.ct.mbs.R;
import com.challenge.ct.mbs.ui.ContactActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles fire base cloud messages
 */
public class MessageHandler extends FirebaseMessagingService {
    // get atomic integer with arbitrary initial value of 1000
    private static final int NOTIFICATION_ID = new AtomicInteger(1000).get();
    private static final String NOTIFICATION_TITLE = "User Updated";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage == null) {
            Log.e("CloudMessageHandler", "null message received");
            return;
        }
        // defer to next context thread
        new Handler(getMainLooper()).post(() -> triggerNotification(remoteMessage));
    }

    private void triggerNotification(@NonNull RemoteMessage message) {
        String info = "";
        if (message.getData() != null && message.getData().containsKey("default")) {
            // this is a customized message, thus hard coding for simple parsing
            String[] msg = message.getData().get("default").split(",");
            String type = msg[1].split(":")[1].toLowerCase();
            String table = msg[2].split(":")[1].toLowerCase();

            info = "a record ".concat(type)
                    .concat(type.equals("remove") ? " from " : " on ")
                    .concat(table).concat(" table");
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setContentTitle(NOTIFICATION_TITLE)
                        .setSmallIcon(R.drawable.ic_supervisor_account_black_24dp)
                        .setAutoCancel(true)
                        .setContentText(info);

        Intent intent = new Intent(this, ContactActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(ContactActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pending =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pending);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
    }
}