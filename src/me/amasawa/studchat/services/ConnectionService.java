package me.amasawa.studchat.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import me.amasawa.studchat.MainApplication;
import me.amasawa.studchat.handlers.*;
import me.amasawa.studchat.tools.Toolbox;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class ConnectionService extends Service {
    MyReceiver myReceiver;
    Future<SocketIOClient> clientFuture = null;
    SocketIOClient clientIO = null;
    String url;
    MainApplication app;
    Timer updateOnline;
    @Override
    public void onCreate() {
        super.onCreate();
        this.app = (MainApplication) getApplicationContext();
        this.myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Toolbox.ACTION_SEND_MESSAGE);
        intentFilter.addAction(Toolbox.ACTION_DISCONNECT);
        intentFilter.addAction(Toolbox.ACTION_NOTIFY_ME_TYPING);
        intentFilter.addAction(Toolbox.ACTION_CONNECT);
        registerReceiver(myReceiver, intentFilter);
        updateOnline = new Timer();
        updateOnline.schedule(new TimerTask() {
            @Override
            public void run() {
                AsyncHttpClient.getDefaultInstance().getString("http://studchat.ru/onlines", new AsyncHttpClient.StringCallback() {
                    @Override
                    public void onCompleted(Exception e, AsyncHttpResponse source, String result) {
                        if (e!=null) {
                            e.printStackTrace();
                            return;
                        }
                        Intent onlineUpdate = Toolbox.prepareIntent(Toolbox.ACTION_ONLINE_UPDATED);
                        onlineUpdate.putExtra("online", result);
                        sendBroadcast(onlineUpdate);
                    }
                });
            }
        }, 0L, Toolbox.ONLINE_UPDATE_DELAY);
        Toolbox.log("ConnectionService created.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
        Toolbox.log("ConnectionService destroyed.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Toolbox.log("ConnectionService binded");
        return new Binder();
    }

    private void connect() {
        Toolbox.log("Got connect action");
        clientFuture = SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), url, new ConnectCallback() {
            @Override
            public void onConnectCompleted(Exception ex, SocketIOClient client) {
                Toolbox.log("onConnectCompleted launched");
                if (ex != null) {
                    Toolbox.log(ex.toString());
                    Intent connFailed = Toolbox.prepareIntent(Toolbox.ACTION_CONNECTION_FAILED);
                    connFailed.putExtra("exception", ex);
                    sendBroadcast(connFailed);
                    return;
                }
                Toolbox.log("Connection established");
                ErrorHandler errorHandler = new ErrorHandler(app);
                MessageHandler messageHandler = new MessageHandler(app);
                TypingHandler typingHandler = new TypingHandler(app);
                StartHandler startHandler = new StartHandler(app);
                ExitHandler exitHandler = new ExitHandler(app);
                DisconnectHandler disconnectHandler = new DisconnectHandler(app);
                client.setDisconnectCallback(errorHandler);
                client.setErrorCallback(errorHandler);
                client.addListener("start", startHandler);
                client.addListener("typing", typingHandler);
                client.addListener("msg", messageHandler);
                client.addListener("userexit", exitHandler);
                client.addListener("disconnect", disconnectHandler);
                JSONArray args = null;
                try {
                    args = new JSONArray("[{\"city\": null}]");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                client.emit("login", args, new Acknowledge() {
                    @Override
                    public void acknowledge(JSONArray userId) {
                        try {
                            Toolbox.log("Got userID: " + userId.getString(0));
                            app.setUserId(userId.getString(0));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Intent gotUserId = Toolbox.prepareIntent(Toolbox.ACTION_GOT_USER_ID);
                        Toolbox.sendBroadcast(app, gotUserId);
                    }
                });
                Toolbox.log("onConnectCompleted Ready!");
                clientIO = client;
            }
        });
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
            super();
        }
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Toolbox.log("Action " + intent.getAction() + " invoked!");
            try {
                switch (intent.getAction()) {
                    case Toolbox.ACTION_SEND_MESSAGE:
                        JSONArray tmp = new JSONArray();
                        tmp.put(intent.getStringExtra("message"));
                        Toolbox.log("Prepared message:" + tmp.toString());
                        clientIO.emit("msg", tmp, new Acknowledge() {
                            @Override
                            public void acknowledge(JSONArray jsonArray) {
                                Intent sent = Toolbox.prepareIntent(Toolbox.ACTION_SENT_MESSAGE);
                                sent.putExtra("message", intent.getStringExtra("message"));
                                Toolbox.sendBroadcast(app, sent);
                            }
                        });
                        break;
                    case Toolbox.ACTION_DISCONNECT:
                        clientIO.emitEvent("userexit", new Acknowledge() {
                            @Override
                            public void acknowledge(JSONArray jsonArray) {
                                app.setState(Toolbox.DISCONNECTED);
                                Intent disconnected = Toolbox.prepareIntent(Toolbox.ACTION_ME_DISCONNECTED);
                                Toolbox.sendBroadcast(app, disconnected);
                                clientIO.disconnect();
                            }
                        });
                        break;
                    case Toolbox.ACTION_NOTIFY_ME_TYPING:
                        clientIO.emitEvent("typing");
                        Toolbox.log("Typing send!");
                        break;
                    case Toolbox.ACTION_CONNECT:
                        url = intent.getStringExtra("serverUrl");
                        connect();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
