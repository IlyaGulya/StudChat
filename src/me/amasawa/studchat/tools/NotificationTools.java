package me.amasawa.studchat.tools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import me.amasawa.studchat.R;
import me.amasawa.studchat.activities.MainActivity;

import java.util.HashMap;

public class NotificationTools {
    private static final String TAG = NotificationTools.class.getSimpleName();
    private static NotificationTools instance;
    private static Context context;
    private NotificationManager manager;
    private int lastId = 0;
    private HashMap<Integer, Notification> notifications;
    private NotificationTools(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifications = new HashMap<Integer, Notification>();
    }

    public static NotificationTools getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationTools(context);
        } else {
            instance.context = context;
        }
        return instance;
    }

    public int createInfoNotification(String message) {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notify", true)) return 0;
        Intent notificationIntent = new Intent(context, MainActivity.class);
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context).
                setSmallIcon(android.R.drawable.ic_dialog_email).
                setAutoCancel(true).
                setTicker(message).
                setContentText(message).
                setContentIntent(
                        PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                ).setWhen(System.currentTimeMillis()).
                setContentTitle(context.getResources().getString(R.string.app_name)).
                setDefaults(Notification.DEFAULT_ALL);
        Notification notification = nb.getNotification();
        manager.notify(lastId, notification);
        notifications.put(lastId, notification);
        return lastId++;
    }
}
