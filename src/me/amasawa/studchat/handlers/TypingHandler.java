package me.amasawa.studchat.handlers;

import android.content.Context;
import android.content.Intent;
import com.koushikdutta.async.http.socketio.Acknowledge;
import me.amasawa.studchat.tools.Toolbox;
import org.json.JSONArray;

public class TypingHandler extends BaseEventHandler {
	public TypingHandler(Context context) {
		super(context);
	}

	@Override
	public void onEvent(JSONArray jsonArray, Acknowledge acknowledge) {
		Toolbox.log("Someone's typing! Data received:" + jsonArray);
		Intent typingEvent = Toolbox.prepareIntent(Toolbox.ACTION_NOTIFY_STRANGER_TYPING);
        sendBroadcast(typingEvent);
	}
}
