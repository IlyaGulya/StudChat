package me.amasawa.studchat.handlers;

import android.content.Context;
import android.content.Intent;
import com.koushikdutta.async.http.socketio.Acknowledge;
import me.amasawa.studchat.R;
import me.amasawa.studchat.tools.NotificationTools;
import me.amasawa.studchat.tools.Toolbox;
import org.json.JSONArray;

public class ExitHandler extends BaseEventHandler {
	public ExitHandler(Context context) {
		super(context);
	}

	@Override
	public void onEvent(JSONArray jsonArray, Acknowledge acknowledge) {
		Toolbox.log("Userexit." + jsonArray);
		app.setState(Toolbox.DISCONNECTED);
		Intent userDisconnected = Toolbox.prepareIntent(Toolbox.ACTION_USER_DISCONNECTED);
		sendBroadcast(userDisconnected);
        if (!app.isActivityRunning()) {
            NotificationTools.getInstance(context).createInfoNotification(
                    context.getResources().getString(R.string.user_disconnected)
            );
        }
	}
}
