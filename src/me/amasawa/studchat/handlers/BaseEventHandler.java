package me.amasawa.studchat.handlers;

import android.content.Context;
import android.content.Intent;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import me.amasawa.studchat.MainApplication;
import me.amasawa.studchat.tools.Toolbox;
import org.json.JSONArray;

public abstract class BaseEventHandler implements EventCallback {
	Context context;
	SocketIOClient client;
	MainApplication app;

	public BaseEventHandler(Context context) {
		this.context = context;
		this.app = (MainApplication) context.getApplicationContext();
	}

    protected void sendBroadcast(Intent intent) {
        Toolbox.sendBroadcast(app, intent);
    }

	@Override
	public abstract void onEvent(JSONArray jsonArray, Acknowledge acknowledge);
}
