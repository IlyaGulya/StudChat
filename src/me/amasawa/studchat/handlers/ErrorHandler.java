package me.amasawa.studchat.handlers;

import android.content.Context;
import android.util.Log;
import com.koushikdutta.async.http.socketio.DisconnectCallback;
import com.koushikdutta.async.http.socketio.ErrorCallback;

public class ErrorHandler implements DisconnectCallback, ErrorCallback {
	Context context;


	public ErrorHandler(Context context) {
		this.context = context;
	}

	@Override
	public void onDisconnect(Exception e) {
		Log.e("amasawa", e.toString());
	}

	@Override
	public void onError(String error) {
		Log.e("amasawa", error);
	}
}
