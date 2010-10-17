package com.googlecode.talkmyphone;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class XmppService extends Service {
    // Currently this class needs to be instantiated from here
    private BroadcastsHandlerService mBroadcastsAndCommandsHandler;

    // To receive message to transmit to the user
    private BroadcastReceiver mMessageReceiver;
    // To know when there are connectivity problems
    private BroadcastReceiver mNetworkReceiver;

    private static final int DISCONNECTED = 0;
    private static final int CONNECTING = 1;
    private static final int CONNECTED = 2;

    // Indicates the current state of the service (disconnected/connecting/connected)
    private int mStatus = DISCONNECTED;

    // Service instance
    private static XmppService instance = null;

    // XMPP connection
    private String mLogin;
    private String mPassword;
    private String mTo;
    private ConnectionConfiguration mConnectionConfiguration = null;
    private XMPPConnection mConnection = null;
    private PacketListener mPacketListener = null;
    private boolean notifyApplicationConnection;

    // notification stuff
    @SuppressWarnings("unchecked")
    private static final Class[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    @SuppressWarnings("unchecked")
    private static final Class[] mStopForegroundSignature = new Class[] {
        boolean.class};
    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    private PendingIntent contentIntent = null;

    // Our current retry attempt, plus a runnable and handler to implement retry
    private int mCurrentRetryCount = 0;
    Runnable mReconnectRunnable = null;
    Handler mReconnectHandler = new Handler();

    public final static String LOG_TAG = "talkmyphone";
    /** Updates the status about the service state (and the statusbar)*/
    private void updateStatus(int status) {
        if (status != mStatus) {
            Notification notification = new Notification();
            switch(status) {
                case CONNECTED:
                    notification = new Notification(
                            R.drawable.status_green,
                            "Connected",
                            System.currentTimeMillis());
                    notification.setLatestEventInfo(
                            getApplicationContext(),
                            "TalkMyPhone",
                            "Connected",
                            contentIntent);
                    break;
                case CONNECTING:
                    notification = new Notification(
                            R.drawable.status_orange,
                            "Connecting...",
                            System.currentTimeMillis());
                    notification.setLatestEventInfo(
                            getApplicationContext(),
                            "TalkMyPhone",
                            "Connecting...",
                            contentIntent);
                    break;
                case DISCONNECTED:
                    notification = new Notification(
                            R.drawable.status_red,
                            "Disconnected",
                            System.currentTimeMillis());
                    notification.setLatestEventInfo(
                            getApplicationContext(),
                            "TalkMyPhone",
                            "Disconnected",
                            contentIntent);
                    break;
                default:
                    break;
            }
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            stopForegroundCompat(mStatus);
            startForegroundCompat(status, notification);
            mStatus = status;
        }
    }
    /**
     * This is a wrapper around the startForeground method, using the older
     * APIs if it is not available.
     */
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            }
            return;
        }
        // Fall back on the old API.
        setForeground(true);
        mNM.notify(id, notification);
    }

    /**
     * This is a wrapper around the stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            }
            return;
        }

        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        setForeground(false);
    }

    /**
     * This makes the 2 previous wrappers possible
     */
    private void initNotificationStuff() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
        contentIntent =
            PendingIntent.getActivity(
                    this, 0, new Intent(this, MainScreen.class), 0);
    }

    /** imports the preferences */
    private void importPreferences() {
        SharedPreferences prefs = getSharedPreferences("TalkMyPhone", 0);
        String serverHost = prefs.getString("serverHost", "");
        int serverPort = prefs.getInt("serverPort", 0);
        String serviceName = prefs.getString("serviceName", "");
        mConnectionConfiguration = new ConnectionConfiguration(serverHost, serverPort, serviceName);
        mTo = prefs.getString("notifiedAddress", "");
        mPassword =  prefs.getString("password", "");
        boolean useDifferentAccount = prefs.getBoolean("useDifferentAccount", false);
        if (useDifferentAccount) {
            mLogin = prefs.getString("login", "");
        } else{
            mLogin = mTo;
        }
        notifyApplicationConnection = prefs.getBoolean("notifyApplicationConnection", true);
    }


    /** clears the XMPP connection */
    private void clearConnection() {
        if (mReconnectRunnable != null)
            mReconnectHandler.removeCallbacks(mReconnectRunnable);

        if (mConnection != null) {
            if (mPacketListener != null) {
                mConnection.removePacketListener(mPacketListener);
            }
            // don't try to disconnect if already disconnected
            if (isConnected()) {
                mConnection.disconnect();
            }
        }
        mConnection = null;
        mPacketListener = null;
        mConnectionConfiguration = null;
        updateStatus(DISCONNECTED);
    }

    private void maybeStartReconnect() {
        if (mCurrentRetryCount > 5) {
            // we failed after all the retries - just die.
            Log.v(LOG_TAG, "maybeStartReconnect ran out of retrys");
            updateStatus(DISCONNECTED);
            Toast.makeText(this, "Failed to connect.", Toast.LENGTH_SHORT).show();
            onDestroy();
            return;
        } else {
            mCurrentRetryCount += 1;
            // a simple linear-backoff strategy.
            int timeout = 5000 * mCurrentRetryCount;
            Log.e(LOG_TAG, "maybeStartReconnect scheduling retry in " + timeout);
            mReconnectHandler.postDelayed(mReconnectRunnable, timeout);
        }
    }

    /** init the XMPP connection */
    private void initConnection() {
        updateStatus(CONNECTING);
        NetworkInfo active = ((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (active==null || !active.isAvailable()) {
            Log.e(LOG_TAG, "connection request, but no network available");
            Toast.makeText(this, "Waiting for network to become available.", Toast.LENGTH_SHORT).show();
            // we don't destroy the service here - our network receiver will notify us when
            // the network comes up and we try again then.
            updateStatus(DISCONNECTED);
            return;
        }
        if (mConnectionConfiguration == null) {
            importPreferences();
        }
        XMPPConnection connection = new XMPPConnection(mConnectionConfiguration);
        try {
            connection.connect();
        } catch (XMPPException e) {
            Log.e(LOG_TAG, "xmpp connection failed: " + e);
            Toast.makeText(this, "Connection failed.", Toast.LENGTH_SHORT).show();
            maybeStartReconnect();
            return;
        }
        try {
            connection.login(mLogin, mPassword);
        } catch (XMPPException e) {
            connection.disconnect();
            Log.e(LOG_TAG, "xmpp login failed: " + e);
            // sadly, smack throws the same generic XMPPException for network
            // related messages (eg "no response from the server") as for
            // authoritative login errors (ie, bad password).  The only
            // differentiator is the message itself which starts with this
            // hard-coded string.
            if (e.getMessage().indexOf("SASL authentication")==-1) {
                // doesn't look like a bad username/password, so retry
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                maybeStartReconnect();
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                onDestroy();
            }
            return;
        }
        mConnection = connection;
        onConnectionComplete();
    }

    private void onConnectionComplete() {
        Log.v(LOG_TAG, "connection established");
        mCurrentRetryCount = 0;
        PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
        mPacketListener = new PacketListener() {
            public void processPacket(Packet packet) {
                Message message = (Message) packet;

                if (    message.getFrom().toLowerCase().startsWith(mTo.toLowerCase() + "/")
                    && !message.getFrom().equals(mConnection.getUser()) // filters self-messages
                ) {
                    if (message.getBody() != null) {
                        String commandLine = message.getBody();
                        String command;
                        String args;

                        if (-1 != commandLine.indexOf(":")) {
                            command = commandLine.substring(0, commandLine.indexOf(":"));
                            args = commandLine.substring(commandLine.indexOf(":") + 1);
                        } else {
                            command = commandLine;
                            args = "";
                        }
                        command = command.toLowerCase();
                        Intent intent = new Intent("ACTION_TALKMYPHONE_USER_COMMAND_RECEIVED");
                        intent.putExtra("command", command);
                        intent.putExtra("args", args);
                        sendBroadcast(intent);
                    }
                }
            }
        };
        mConnection.addPacketListener(mPacketListener, filter);
        updateStatus(CONNECTED);
        // Send welcome message
        if (notifyApplicationConnection) {
            send("Welcome to TalkMyPhone. Send \"?\" for getting help");
        }
    }

    /** returns true if the service is correctly connected */
    private boolean isConnected() {
        return    (mConnection != null
                && mConnection.isConnected()
                && mConnection.isAuthenticated());
    }

    private void _onStart() {
        // Get configuration
        if (instance == null)
        {
            instance = this;

            mBroadcastsAndCommandsHandler = new BroadcastsHandlerService(getApplicationContext());

            initNotificationStuff();

            updateStatus(DISCONNECTED);

            // first, clean everything
            clearConnection();

            // then, re-import preferences
            importPreferences();

            mCurrentRetryCount = 0;
            mReconnectRunnable = new Runnable() {
                public void run() {
                    Log.v(LOG_TAG, "attempting reconnection");
                    Toast.makeText(XmppService.this, "Reconnecting", Toast.LENGTH_SHORT).show();
                    initConnection();
                }
            };
            initConnection();

            mMessageReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String message = intent.getStringExtra("message");
                    send(message);
                }
            };
            registerReceiver(mMessageReceiver, new IntentFilter("ACTION_TALKMYPHONE_MESSAGE_TO_TRANSMIT"));
            mNetworkReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    XmppService service = XmppService.getInstance();
                    if (service != null) {
                        // is this notification telling us about a new network which is a
                        // 'failover' due to another network stopping?
                        boolean failover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
                        // Are we in a 'no connectivity' state?
                        boolean nocon = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                        NetworkInfo network = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                        // if no network, or if this is a "failover" notification
                        // (meaning the network we are connected to has stopped)
                        // and we are connected , we must disconnect.
                        if (network == null || !network.isConnected() || (failover && service.isConnected())) {
                            Log.i(XmppService.LOG_TAG, "network unavailable - closing connection");
                            service.clearConnection();
                        }
                        // connect if not already connected (eg, if we disconnected above) and we have connectivity
                        if (!nocon && !service.isConnected()) {
                            Log.i(XmppService.LOG_TAG, "network available and not connected - connecting");
                            service.initConnection();
                        }
                    }
                }
            };
            registerReceiver(mNetworkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    public static XmppService getInstance() {
        return instance;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        _onStart();
    };

    @Override
    public void onDestroy() {
        mBroadcastsAndCommandsHandler.destroy();
        unregisterReceiver(mNetworkReceiver);
        unregisterReceiver(mMessageReceiver);

        clearConnection();

        stopForegroundCompat(mStatus);

        instance = null;

        Toast.makeText(this, "TalkMyPhone stopped", Toast.LENGTH_SHORT).show();
    }

    /** sends a message to the user */
    private void send(String message) {
        if (isConnected()) {
            Message msg = new Message(mTo, Message.Type.chat);
            msg.setBody(message);
            mConnection.sendPacket(msg);
        }
    }
}
