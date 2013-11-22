package me.amasawa.studchat.tools;

import android.content.Intent;
import android.util.Log;
import me.amasawa.studchat.MainApplication;

public class Toolbox {
	public static final int DISCONNECTED = 0;
	public static final int PENDING = 1;
	public static final int CONNECTED = 2;
    public static final int CONNECTION_REQUESTED = 3;
	public static final int ME = 0;
	public static final int ANON = 1;
	public static final int SYSTEM = 2;
	public static final int TYPING_DELAY = 2000;
    public static final long ONLINE_UPDATE_DELAY = 20000L;
	public static final boolean DEBUG = true;
	public static final String TAG = "amasawa";
	public static final String PACKAGE = "me.amasawa.studchat.";
    public static final String ACTION_CONNECT = PACKAGE + "connect";
	public static final String ACTION_SEND_MESSAGE = PACKAGE + "sendMessage";
	public static final String ACTION_SENT_MESSAGE = PACKAGE + "sentMessage";
	public static final String ACTION_GOT_MESSAGE = PACKAGE + "gotMessage";
	public static final String ACTION_GOT_USER_ID = PACKAGE + "gotUserId";
	public static final String ACTION_CONNECTION_ESTABLISHED = PACKAGE + "connEstablished";
	public static final String ACTION_CONNECTION_LOST = PACKAGE + "connLost";
	public static final String ACTION_CONNECTION_FAILED = PACKAGE + "connFailed";
	public static final String ACTION_ONLINE_UPDATED = PACKAGE + "onlineUpdated";
	public static final String ACTION_DISCONNECT = PACKAGE + "disconnect";
	public static final String ACTION_USER_DISCONNECTED = PACKAGE + "userDisconnected";
	public static final String ACTION_ME_DISCONNECTED = PACKAGE + "meDisconnected";
	public static final String ACTION_NOTIFY_ME_TYPING = PACKAGE + "meTyping";
	public static final String ACTION_NOTIFY_STRANGER_TYPING = PACKAGE + "strangerTyping";
	public static void log(String message) {
		if (DEBUG)
			Log.d(TAG, message);
	}
	public static Intent prepareIntent(String action) {
		Intent tmp = new Intent();
		tmp.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		tmp.setAction(action);
		return tmp;
	}
    public static void sendBroadcast(MainApplication app, Intent intent) {
        if (app.isActivityRunning()) {
            app.sendBroadcast(intent);
        } else {
            if (!intent.getAction().equals(ACTION_NOTIFY_STRANGER_TYPING))
                app.pendingIntents.add(intent);
        }
    }
}
