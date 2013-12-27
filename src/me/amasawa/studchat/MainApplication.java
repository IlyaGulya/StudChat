package me.amasawa.studchat;

import android.app.Application;
import android.content.Intent;
import com.yandex.metrica.Counter;
import me.amasawa.studchat.handlers.Connection;
import me.amasawa.studchat.tools.Toolbox;

import java.util.ArrayList;

public class MainApplication extends Application{
	private boolean userTyping = false;
    private boolean serviceLaunched = false;
    private boolean activityRunning = false;
	private int state = Toolbox.DISCONNECTED;
    public ArrayList<Message> currentMessages = new ArrayList<Message>();
    public ArrayList<Intent> pendingIntents = new ArrayList<Intent>();
	private String userId = "";
	private String toId = "";
    private Connection connection;

    @Override
    public void onCreate() {
        super.onCreate();
        Counter.initialize(getApplicationContext());
        this.connection = new Connection(this);
    }

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public boolean isUserTyping() {
		return userTyping;
	}

	public void setUserTyping(boolean userTyping) {
		this.userTyping = userTyping;
	}

    public boolean isActivityRunning() {
        return activityRunning;
    }

    public void setActivityRunning(boolean activityRunning) {
        this.activityRunning = activityRunning;
    }

    public Connection getConnection() {
        return connection;
    }

    public static class Message {
        public int who;
        public String message;
        public Message(int who, String message) {
            this.message = message;
            this.who = who;
        }
    }
}
