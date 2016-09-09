package com.example.smack.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.util.Log;

import com.example.smack.Singleton;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.net.ssl.SSLContext;
//import eu.geekplace.javapinning.JavaPinning;
import com.example.smack.MemorizingTrustManager.MemorizingTrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by dmolloy on 9/7/16.
 */
public class SmackConnection implements ConnectionListener, ChatManagerListener, RosterListener, ChatMessageListener, PingFailedListener {

    public enum ConnectionState {
        CONNECTED, CONNECTING, RECONNECTING, DISCONNECTED,CONNECTEDBUTNOTAUTHENTICATED
    }

    private static final String TAG = "SMACK";
    private final Context mApplicationContext;
    private final String mPassword;
    private final String mUsername;
    private final String mServiceName;

    private AbstractXMPPConnection mConnection;//XMPPTCPConnection
    private ArrayList<String> mRosterList;
    private BroadcastReceiver mReceiver;
    private Roster mRoster;

    public SmackConnection(Context pContext) {
        //Log.i(TAG, "ChatConnection()");

        mApplicationContext = pContext.getApplicationContext();
        //mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext).getString("xmpp_password", null);
        //String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext).getString("xmpp_jid", null);

        mPassword = Singleton.getInstance().getmPass();
        String jid = Singleton.getInstance().getmUser();

        if (jid.length()>2){
            mServiceName = jid.split("@")[1];
            mUsername = jid.split("@")[0];
        }else{
            mServiceName = "";
            mUsername = "";
        }


    }

    public void connect() throws IOException, XMPPException, SmackException {
        //Log.i(TAG, "connect()");

        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();

        builder.setServiceName(mServiceName);
        builder.setResource("SmackAndroidTestClient");
        builder.setUsernameAndPassword(mUsername, mPassword);
        //builder.setRosterLoadedAtLogin(true);
        builder.setSendPresence(true);
        builder.setDebuggerEnabled(false);

        //Security
        builder.setPort(5222);
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
        builder.setCompressionEnabled(true);

        //X509TrustManager pinning = JavaPinning.trustManagerForPin("CERTSHA256:8A:C6:6A:44:3B:92:08:62:D5:19:54:EC:38:C2:41:B8:65:A8:E9:DE:E8:F1:08:46:82:EC:C8:ED:36:82:CE:96");
        MemorizingTrustManager mtm  = new MemorizingTrustManager(mApplicationContext);
        try {
            //SSLContext sc = JavaPinning.forPin("CERTSHA256:8A:C6:6A:44:3B:92:08:62:D5:19:54:EC:38:C2:41:B8:65:A8:E9:DE:E8:F1:08:46:82:EC:C8:ED:36:82:CE:96");
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new X509TrustManager[]{mtm}, new java.security.SecureRandom());
            builder.setCustomSSLContext(sc);
            builder.setHostnameVerifier(mtm.wrapHostnameVerifier(new org.apache.http.conn.ssl.StrictHostnameVerifier()));

            mConnection = new XMPPTCPConnection(builder.build());

            //mConnection.getRoster().addRosterListener(this);
            mRoster = Roster.getInstanceFor(mConnection);
            mRoster.addRosterListener(this);

            //Set ConnectionListener here to catch initial connect();
            mConnection.addConnectionListener(this);

            mConnection.connect();
            mConnection.login();


            PingManager.setDefaultPingInterval(600); //Ping every 10 minutes
            PingManager pingManager = PingManager.getInstanceFor(mConnection);
            pingManager.registerPingFailedListener(this);

            setupSendMessageReceiver();

            ChatManager.getInstanceFor(mConnection).addChatListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void disconnect() {
        //Log.i(TAG, "disconnect()");
        /*
        try {
            if(mConnection != null){
                mConnection.disconnect();
            }
        }catch (SmackException.NotConnectedException e){
            e.printStackTrace();
        }
*/
        if (mConnection != null){
            mConnection.disconnect();
        }
        mConnection = null;
        if(mReceiver != null){
            mApplicationContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }


    private void rebuildRoster() {

        mRosterList = new ArrayList<>();
        String status;
        if (!mRoster.isLoaded()){
            try {
                mRoster.reloadAndWait();
            }catch (SmackException.NotLoggedInException | InterruptedException | SmackException.NotConnectedException e){
                e.printStackTrace();
            }
        }else{
            Collection <RosterEntry> entries = mRoster.getEntries();
            for (RosterEntry entry : entries)
            {
                if (mRoster.getPresence(entry.getUser()).isAvailable())
                {
                    status = "Online";
                } else {
                    status = "Offline";
                }
                mRosterList.add(entry.getUser()+": "+status);
            }
            Intent intent = new Intent(SmackService.NEW_ROSTER);
            intent.setPackage(mApplicationContext.getPackageName());
            intent.putStringArrayListExtra(SmackService.BUNDLE_ROSTER, mRosterList);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            }
            mApplicationContext.sendBroadcast(intent);
        }
    }

    private void setupSendMessageReceiver() {
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(SmackService.SEND_MESSAGE)) {
                    sendMessage(intent.getStringExtra(SmackService.BUNDLE_MESSAGE_BODY), intent.getStringExtra(SmackService.BUNDLE_TO));
                }
            }

        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(SmackService.SEND_MESSAGE);
        mApplicationContext.registerReceiver(mReceiver, filter);
    }

    private void sendMessage(String body, String toJid) {
        Chat chat = ChatManager.getInstanceFor(mConnection).createChat(toJid, this);
        try {
            chat.sendMessage(body);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    //ChatListener
    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
       // Log.i(TAG, "chatCreated()");
        chat.addMessageListener(this);
    }

    //MessageListener
    @Override
    public void processMessage(Chat chat, Message message) {
        if (message.getType().equals(Message.Type.chat) || message.getType().equals(Message.Type.normal)) {
            if (message.getBody() != null) {
                Intent intent = new Intent(SmackService.NEW_MESSAGE);
                intent.setPackage(mApplicationContext.getPackageName());
                intent.putExtra(SmackService.BUNDLE_MESSAGE_BODY, message.getBody());
                intent.putExtra(SmackService.BUNDLE_FROM_JID, message.getFrom());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                }
                mApplicationContext.sendBroadcast(intent);
            }
        }
    }

    //ConnectionListener
    @Override
    public void connected(XMPPConnection connection) {
        SmackService.sConnectionState = ConnectionState.CONNECTED;
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean bool) {
        if (bool){
            SmackService.sConnectionState = ConnectionState.CONNECTED;
        }else {
            SmackService.sConnectionState = ConnectionState.CONNECTEDBUTNOTAUTHENTICATED;
        }
    }

    @Override
    public void connectionClosed() {
        SmackService.sConnectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        SmackService.sConnectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public void reconnectingIn(int seconds) {
        SmackService.sConnectionState = ConnectionState.RECONNECTING;
    }

    @Override
    public void reconnectionSuccessful() {
        SmackService.sConnectionState = ConnectionState.CONNECTED;
    }

    @Override
    public void reconnectionFailed(Exception e) {
        SmackService.sConnectionState = ConnectionState.DISCONNECTED;
    }

    //RosterListener
    @Override
    public void entriesAdded(Collection<String> addresses) {
        rebuildRoster();
    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {
        rebuildRoster();
    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {
        rebuildRoster();
    }

    @Override
    public void presenceChanged(Presence presence) {
        rebuildRoster();
    }

    //PingFailedListener
    @Override
    public void pingFailed() {
    }
}
