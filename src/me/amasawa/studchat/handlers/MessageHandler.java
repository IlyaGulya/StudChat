package me.amasawa.studchat.handlers;

import android.content.Context;
import android.content.Intent;
import com.koushikdutta.async.http.socketio.Acknowledge;
import me.amasawa.studchat.R;
import me.amasawa.studchat.tools.NotificationTools;
import me.amasawa.studchat.tools.Toolbox;
import org.json.JSONArray;
import org.json.JSONException;

public class MessageHandler extends BaseEventHandler {

	public MessageHandler(Context context) {
		super(context);
	}

	@Override
	public void onEvent(JSONArray jsonArray, Acknowledge acknowledge) {
		Toolbox.log("Message received: " + jsonArray.toString());
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.setAction(Toolbox.ACTION_GOT_MESSAGE);
        String message = null;
		try {
            message = jsonArray.getString(0);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        intent.putExtra("message", message);
        sendBroadcast(intent);
        if (!app.isActivityRunning()) {
            NotificationTools.getInstance(context).createInfoNotification(
                    context.getResources().getString(R.string.stranger) + ": " + message
            );
        }
	}
}
