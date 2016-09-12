package com.example.smack;

/**
 * Created by dmolloy on 9/7/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.smack.service.SmackConnection;
import com.example.smack.service.SmackService;


public class LoginActivity extends Activity implements View.OnClickListener {

    private Button button;
    private EditText edPW;
    private EditText edJID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        String Password = PreferenceManager.getDefaultSharedPreferences(this).getString("xmpp_password", null);
        String Service = PreferenceManager.getDefaultSharedPreferences(this).getString("xmpp_jid", null);

        button = (Button)this.findViewById(R.id.button);

        if(!SmackService.getState().equals(SmackConnection.ConnectionState.DISCONNECTED)){
            button.setText(R.string.disconnect);
            this.startActivity(new Intent(this, ChatActivity.class));
        }

        edPW = (EditText)this.findViewById(R.id.ed_password);
        edJID = (EditText)this.findViewById(R.id.ed_jid);

        if(Password != null){
            edPW.setText(Password);
        }
        if(Service != null){
            edJID.setText(Service);
        }

        button.setOnClickListener(this);

    }

    private boolean isPasswordValid(String password) {

        PasswordValidator passwordValidator = new PasswordValidator();

        return passwordValidator.validate(password);
    }

    private void save() {

        if (button.getText().toString().equalsIgnoreCase("Connect"))
        {
            if(!verifyJabberID(edJID.getText().toString())){
                Toast.makeText(this, "Invalid JID", Toast.LENGTH_SHORT).show();
                return;
            }

            if (edPW.getText().toString().length()<8){
                Toast.makeText(this,"Password is too short",Toast.LENGTH_LONG).show();
                return;
            }else if (!isPasswordValid(edPW.getText().toString())){
                Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
                return;
            }else{
                Singleton.getInstance().setmUser(edJID.getText().toString());
                Singleton.getInstance().setmPass(edPW.getText().toString());

                button.setText(R.string.disconnect);
                Intent intent = new Intent(this, SmackService.class);
                this.startService(intent);

                this.startActivity(new Intent(this, ChatActivity.class));
            }
        } else {
            //Log.d("Login","stopping intent");
            button.setText(R.string.connect);
            Intent intent = new Intent(this, SmackService.class);
            this.stopService(intent);
        }
    }

    private static boolean verifyJabberID(String jid){
        try {
            String parts[] = jid.split("@");
            if (parts.length != 2 || parts[0].length() == 0 || parts[1].length() == 0){
                return false;
            }
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        save();
    }
}