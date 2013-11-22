package me.amasawa.studchat.handlers;

import android.content.Context;
import android.content.Intent;
import com.koushikdutta.async.http.socketio.Acknowledge;
import me.amasawa.studchat.R;
import me.amasawa.studchat.tools.NotificationTools;
import me.amasawa.studchat.tools.Toolbox;
import org.json.JSONArray;
import org.json.JSONException;

public class StartHandler extends BaseEventHandler {
	public StartHandler(Context context) {
		super(context);
	}

	@Override
	public void onEvent(JSONArray jsonArray, Acknowledge acknowledge) {
		Toolbox.log("Got ToID: " + jsonArray.toString());
		try {
			app.setToId(jsonArray.getString(0));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		app.setState(Toolbox.CONNECTED);
		Intent connectionEstablished = Toolbox.prepareIntent(Toolbox.ACTION_CONNECTION_ESTABLISHED);
		sendBroadcast(connectionEstablished);
        if (!app.isActivityRunning()) {
            NotificationTools.getInstance(context).createInfoNotification(
                    context.getResources().getString(R.string.connection_established)
            );
        }
	}
}
