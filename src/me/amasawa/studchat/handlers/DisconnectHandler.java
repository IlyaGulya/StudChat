package me.amasawa.studchat.handlers;

import android.content.Context;
import android.content.Intent;
import com.koushikdutta.async.http.socketio.Acknowledge;
import me.amasawa.studchat.R;
import me.amasawa.studchat.tools.NotificationTools;
import me.amasawa.studchat.tools.Toolbox;
import org.json.JSONArray;

public class DisconnectHandler extends BaseEventHandler {
	public DisconnectHandler(Context context) {
		super(context);
	}

	@Override
	public void onEvent(JSONArray jsonArray, Acknowledge acknowledge) {
		Toolbox.log("Disconnect handler received a message!");
		app.setState(Toolbox.DISCONNECTED);
		Intent disconnect = Toolbox.prepareIntent(Toolbox.ACTION_CONNECTION_LOST);
		sendBroadcast(disconnect);
        if (!app.isActivityRunning()) {
            NotificationTools.getInstance(context).createInfoNotification(
                    context.getResources().getString(R.string.connection_lost)
            );
        }
	}
}
