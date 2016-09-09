package com.example.smack;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import java.util.ArrayList;

import com.example.smack.service.SmackService;
import co.devcenter.androiduilibrary.ChatView;
import co.devcenter.androiduilibrary.ChatViewEventListener;
import co.devcenter.androiduilibrary.SendButton;

/**
 * Created by dmolloy on 9/7/16.
 */
public class ChatActivity extends Activity {

    private TextView edTo;
    private BroadcastReceiver mReceiver;

    private ListView contactsRecyclerView;
    private ChatView mChatView;
    private SendButton mSendButton;
    private ArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        edTo = (TextView)this.findViewById(R.id.ed_to);

        ArrayList<String> array = new ArrayList<>();
        mAdapter = new ArrayAdapter(getApplicationContext(),R.layout.list_item_contact,array);

        contactsRecyclerView = (ListView) findViewById(R.id.contact_list_recycler_view);
        contactsRecyclerView.setAdapter(mAdapter);
        contactsRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                edTo.setText(mAdapter.getItem(position).toString());
            }

        });

        mChatView =(ChatView) findViewById(R.id.rooster_chat_view);
        mChatView.setEventListener(new ChatViewEventListener() {
            @Override
            public void userIsTyping() {
                //Here you know that the user is typing
            }

            @Override
            public void userHasStoppedTyping() {
                //Here you know that the user has stopped typing.
            }
        });

        mSendButton = mChatView.getSendButton();
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edTo.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please select a contact first",Toast.LENGTH_LONG).show();
                }else{
                    sendMessage();
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                switch (action){
                    case SmackService.NEW_MESSAGE:
                        //String from = intent.getStringExtra(SmackService.BUNDLE_FROM_JID);
                        String message = intent.getStringExtra(SmackService.BUNDLE_MESSAGE_BODY);

                        //log = from+": "+message+"\n"+log;

                        //Log.d("Message",from+": "+message+"\n");

                        mChatView.receiveMessage(message);

                        break;
                    case SmackService.NEW_ROSTER:
                        final ArrayList<String> roster = intent.getStringArrayListExtra(SmackService.BUNDLE_ROSTER);
                        if(roster == null){
                            return;
                        }

                        //Log.d("Contacts","Loading Contacts");

                       // contactsRecyclerView.setAdapter(mAdapter);

                        ChatActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                //Log.d("UI thread", "I am the UI thread");
                                mAdapter.clear();
                                mAdapter.addAll(roster);
                                mAdapter.notifyDataSetChanged();
                            }
                        });

                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter(SmackService.NEW_ROSTER);
        filter.addAction(SmackService.NEW_MESSAGE);
        this.registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mReceiver);

        //Stop the smack service, disconnecting us from the server
        Intent intent = new Intent(this, SmackService.class);
        this.stopService(intent);

        //Start a new login activity
        intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }

    private void sendMessage() {
        //Log.d("TAG","sendMessage");
        Intent intent = new Intent(SmackService.SEND_MESSAGE);
        intent.setPackage(this.getPackageName());
        intent.putExtra(SmackService.BUNDLE_MESSAGE_BODY, mChatView.getTypedString());
        //intent.putExtra(SmackService.BUNDLE_TO, edTo.getText().toString());
        intent.putExtra(SmackService.BUNDLE_TO, removeStatus(edTo.getText().toString()));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        }
        this.sendBroadcast(intent);

        //Update Chatview
        mChatView.sendMessage();

    }

    private String removeStatus(String contact){
        String clearContact = contact.split(":")[0];
        clearContact = clearContact.trim();
        return clearContact;
    }
}
