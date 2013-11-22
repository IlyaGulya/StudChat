package me.amasawa.studchat.activities;

import android.content.*;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.yandex.metrica.Counter;
import me.amasawa.studchat.MainApplication;
import me.amasawa.studchat.R;
import me.amasawa.studchat.services.ConnectionService;
import me.amasawa.studchat.tools.FlowTextHelper;
import me.amasawa.studchat.tools.Toolbox;

public class MainActivity extends SherlockActivity implements View.OnClickListener {
    public final Handler ui = new Handler();
    LinearLayout chatContainer, messageControls;
    TextView onlineCount, typingNotify;
    Button sendButton, welcomeConnectButton;
    ImageButton connectButton, disconnectButton;
    EditText messageField;
    MainApplication app;
    ProgressBar progressBar;
    View bar;
    ScrollView scrollView;
    MyReceiver myReceiver = new MyReceiver();
    RelativeLayout welcomeLayout;
    boolean isWelcome;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
        if (!app.isServiceLaunched())
            bindService(new Intent(this, ConnectionService.class), new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    app.setServiceLaunched(true);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    app.setServiceLaunched(true);
                }
            }, BIND_AUTO_CREATE);
        fillChatContainer();
        if (app.getState() == Toolbox.DISCONNECTED) {
            toggleWelcome();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.menu_notification);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean notify = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("notify", true);
        menu.findItem(0).setCheckable(true);
        menu.findItem(0).setChecked(notify);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                item.setChecked(!item.isChecked());
                Toolbox.log(String.valueOf(item.isChecked()));
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("notify", item.isChecked()).commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Counter.sharedInstance().onResumeActivity(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Toolbox.ACTION_GOT_MESSAGE);
        intentFilter.addAction(Toolbox.ACTION_SENT_MESSAGE);
        intentFilter.addAction(Toolbox.ACTION_CONNECTION_ESTABLISHED);
        intentFilter.addAction(Toolbox.ACTION_CONNECTION_FAILED);
        intentFilter.addAction(Toolbox.ACTION_USER_DISCONNECTED);
        intentFilter.addAction(Toolbox.ACTION_CONNECTION_LOST);
        intentFilter.addAction(Toolbox.ACTION_ME_DISCONNECTED);
        intentFilter.addAction(Toolbox.ACTION_ONLINE_UPDATED);
        intentFilter.addAction(Toolbox.ACTION_GOT_USER_ID);
        intentFilter.addAction(Toolbox.ACTION_NOTIFY_STRANGER_TYPING);
        registerReceiver(myReceiver, intentFilter);
        app.setActivityRunning(true);
        setUIState(app.getState());
        for (Intent i:app.pendingIntents) {
            Toolbox.log("pendingIntent: " + i.toString());
            sendBroadcast(i);
        }
        app.pendingIntents.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        Counter.sharedInstance().onPauseActivity(this);
        app.setActivityRunning(false);
        unregisterReceiver(myReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.connectButton:
                switch (app.getState()) {
                    case Toolbox.CONNECTED:
                        reconnect();
                        break;
                    case Toolbox.DISCONNECTED:
                        if (isWelcome) toggleWelcome();
                        connect();
                        break;
                    case Toolbox.PENDING:
                        return;
                }
                break;
            case R.id.welcomeConnect:
                toggleWelcome();
                connect();
                break;
            case R.id.disconnectButton:
                disconnect();
                break;
            case R.id.chatContainer:
                hideKeyboard(view);
                break;
            case R.id.scrollView:
                hideKeyboard(view);
                break;
            case R.id.sendButton:
                String msg = messageField.getText().toString();
                if (!msg.trim().equals("")) {
                    sendMessage(msg.trim());
                    messageField.setText("");
                } else {
                    //TODO: Smooth transition for messageField border
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        scrollToLast();
    }

    public void addMessage(String message, int who) {
        Display display = getWindowManager().getDefaultDisplay();
        View msg = getLayoutInflater().inflate(R.layout.message, null);
        TextView sender = (TextView) msg.findViewById(R.id.sender);
        TextView messageView = (TextView) msg.findViewById(R.id.message);
        int padding = getResources().getDimensionPixelSize(R.dimen.messageContainerPadding);
        sender.setPadding(padding,0,padding,0);
        sender.setTextColor(Color.WHITE);
        msg.setPadding(0, padding, 0, 0);
        switch (who) {
            case Toolbox.ME:
                sender.setText(R.string.me);
                sender.setBackgroundResource(R.drawable.text_view_me);
                break;
            case Toolbox.ANON:
                sender.setText(R.string.stranger);
                sender.setBackgroundResource(R.drawable.text_view_anon);
                break;
            case Toolbox.SYSTEM:
                sender.setText(R.string.system);
                sender.setBackgroundResource(R.drawable.text_view_system);
        }
        FlowTextHelper.tryFlowText(message, sender, messageView, display, 6);
        chatContainer.addView(msg);
        scrollToLast();
    }

    public void scrollToLast() {
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    public void hideKeyboard(View view) {
        InputMethodManager ims = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        ims.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void sendMessage(String message) {
        Intent tmp = new Intent();
        tmp.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        tmp.setAction(Toolbox.ACTION_SEND_MESSAGE);
        tmp.putExtra("message", message);
        sendBroadcast(tmp);
    }

    public void showNotification(int id) {
        String message = getResources().getString(id);
        app.currentMessages.add(new MainApplication.Message(Toolbox.SYSTEM, message));
        addMessage(message, Toolbox.SYSTEM);
    }

    public void toggleWelcome() {
        if (welcomeLayout.getVisibility() == View.VISIBLE) {
            welcomeLayout.setVisibility(View.INVISIBLE);
            chatContainer.setVisibility(View.VISIBLE);
            messageControls.setVisibility(View.VISIBLE);
            connectButton.setVisibility(View.VISIBLE);
            disconnectButton.setVisibility(View.VISIBLE);
            isWelcome = false;
        } else {
            welcomeLayout.setVisibility(View.VISIBLE);
            chatContainer.setVisibility(View.INVISIBLE);
            messageControls.setVisibility(View.INVISIBLE);
            connectButton.setVisibility(View.INVISIBLE);
            disconnectButton.setVisibility(View.INVISIBLE);
            isWelcome = true;
        }
    }

    private void connect() {
        setUIState(Toolbox.PENDING);
        app.setState(Toolbox.PENDING);
        Intent connect = Toolbox.prepareIntent(Toolbox.ACTION_CONNECT);
        connect.putExtra("serverUrl", "http://socket.studchat.ru:8878/");
        sendBroadcast(connect);
    }

    private void disconnect() {
        setUIState(Toolbox.PENDING);
        app.setState(Toolbox.PENDING);
        Intent tmp = Toolbox.prepareIntent(Toolbox.ACTION_DISCONNECT);
        sendBroadcast(tmp);
    }

    private void reconnect() {
        setUIState(Toolbox.PENDING);
        app.setState(Toolbox.PENDING);
        disconnect();
        connect();
    }

    private void init() {
        app = (MainApplication) getApplicationContext();
        bar = LayoutInflater.from(this).inflate(R.layout.bar, null);
        chatContainer = (LinearLayout) findViewById(R.id.chatContainer);
        sendButton = (Button) findViewById(R.id.sendButton);
        messageField = (EditText) findViewById(R.id.messageField);
        onlineCount = (TextView) bar.findViewById(R.id.onlineCount);
        connectButton = (ImageButton) bar.findViewById(R.id.connectButton);
        disconnectButton = (ImageButton) bar.findViewById(R.id.disconnectButton);
        progressBar = (ProgressBar) bar.findViewById(R.id.progressBar);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        welcomeLayout = (RelativeLayout) findViewById(R.id.welcomeLayout);
        welcomeConnectButton = (Button) findViewById(R.id.welcomeConnect);
        messageControls = (LinearLayout) findViewById(R.id.messageControls);
        typingNotify = new TextView(this);
        chatContainer.setOnClickListener(this);
        scrollView.setOnClickListener(this);
        connectButton.setOnClickListener(this);
        disconnectButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        welcomeConnectButton.setOnClickListener(this);
        messageField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (app.isUserTyping()) return;
                app.setUserTyping(true);
                Intent sendTyping = Toolbox.prepareIntent(Toolbox.ACTION_NOTIFY_ME_TYPING);
                sendBroadcast(sendTyping);
                ui.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        app.setUserTyping(false);
                    }
                }, Toolbox.TYPING_DELAY);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        typingNotify.setTag("notify");
        typingNotify.setText(R.string.user_typing);
        typingNotify.setTextAppearance(this, R.style.Theme_Studchat_TypingTextStyle);
        getSupportActionBar().setCustomView(bar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void fillChatContainer() {
        for (MainApplication.Message i:app.currentMessages)
            addMessage(i.message, i.who);
    }

    public void setUIState(int state) {
        switch (state) {
            case Toolbox.CONNECTED:
                connectButton.setImageResource(R.drawable.ic_menu_refresh);
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(true);
                sendButton.setEnabled(true);
                messageField.setEnabled(true);
                progressBar.setVisibility(View.INVISIBLE);
                break;
            case Toolbox.DISCONNECTED:
                connectButton.setImageResource(R.drawable.ic_menu_play_clip);
                connectButton.setEnabled(true);
                sendButton.setEnabled(false);
                messageField.setEnabled(false);
                disconnectButton.setEnabled(false);
                progressBar.setVisibility(View.INVISIBLE);
                break;
            case Toolbox.PENDING:
                connectButton.setEnabled(false);
                sendButton.setEnabled(false);
                messageField.setEnabled(false);
                disconnectButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Toolbox.log("Action " + intent.getAction() + " invoked!");
            switch (intent.getAction()) {
                case Toolbox.ACTION_GOT_MESSAGE:
                    final String msg = intent.getStringExtra("message");
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            app.currentMessages.add(new MainApplication.Message(Toolbox.ANON, msg));
                            addMessage(msg, Toolbox.ANON);
                        }
                    });
                    break;
                case Toolbox.ACTION_SENT_MESSAGE:
                    final String msg1 = intent.getStringExtra("message");
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            app.currentMessages.add(new MainApplication.Message(Toolbox.ME, msg1));
                            addMessage(msg1, Toolbox.ME);
                        }
                    });
                    break;
                case Toolbox.ACTION_CONNECTION_ESTABLISHED:
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            setUIState(Toolbox.CONNECTED);
                            app.setState(Toolbox.CONNECTED);
                            showNotification(R.string.user_connected);
                        }
                    });
                    break;
                case Toolbox.ACTION_USER_DISCONNECTED:
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            setUIState(Toolbox.DISCONNECTED);
                            app.setState(Toolbox.DISCONNECTED);
                            showNotification(R.string.user_disconnected);
                        }
                    });
                    break;
                case Toolbox.ACTION_ME_DISCONNECTED:
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            setUIState(Toolbox.DISCONNECTED);
                            app.setState(Toolbox.DISCONNECTED);
                            showNotification(R.string.me_disconnect);
                        }
                    });
                    break;
                case Toolbox.ACTION_ONLINE_UPDATED:
                    final String online = intent.getStringExtra("online");
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            onlineCount.setText(getResources().getString(R.string.online) + ": " + online);
                        }
                    });
                    break;
                case Toolbox.ACTION_GOT_USER_ID:
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            showNotification(R.string.connection_established);
                            showNotification(R.string.user_waiting);
                        }
                    });
                    break;
                case Toolbox.ACTION_NOTIFY_STRANGER_TYPING:
                    Toolbox.log(String.valueOf(chatContainer.findViewWithTag("notify")));
                    if (chatContainer.findViewWithTag("notify") != null) return;
                    chatContainer.addView(typingNotify);
                    scrollToLast();
                    ui.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            chatContainer.removeView(typingNotify);
                            scrollToLast();
                        }
                    }, Toolbox.TYPING_DELAY);
                    break;
                case Toolbox.ACTION_CONNECTION_LOST:
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            app.setState(Toolbox.DISCONNECTED);
                            showNotification(R.string.connection_lost);
                        }
                    });
                    break;
                case Toolbox.ACTION_CONNECTION_FAILED:
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            setUIState(Toolbox.DISCONNECTED);
                            app.setState(Toolbox.DISCONNECTED);
                            showNotification(R.string.connection_failed);
                        }
                    });
            }
        }
    }
}
