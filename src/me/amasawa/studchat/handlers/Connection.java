package me.amasawa.studchat.handlers;

import android.content.ComponentName;
import android.os.IBinder;
import me.amasawa.studchat.MainApplication;

/**
 * Created by Yuuko Amasawa on 27.12.13.
 */
public class Connection implements android.content.ServiceConnection {
    private MainApplication app;

    public Connection(MainApplication application) {
        this.app = application;
    }
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
