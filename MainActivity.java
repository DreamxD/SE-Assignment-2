package com.jikexueyuan.jike_chat;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        if(App.isLogin) {
            RongIM.connect(App.token, new RongIMClient.ConnectCallback() {
                @Override
                public void onTokenIncorrect() {
                    Toast.makeText(MainActivity.this, "Token incorrect", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(String s) {
                    Toast.makeText(MainActivity.this, "Login Success " + s, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
                }
            });

        }
        else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        }


        String token = "fMXnyAvR2EtjiPmglIK8NtiHORASPTrSh3bY77l4cE6kueHr4l9P0ozLEVQg3es8Arguc2rVddmh6ieLCiPanA==";
        RongIM.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                Toast.makeText(MainActivity.this, "Token incorrect", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(String s) {
                Toast.makeText(MainActivity.this, "Login Success " + s, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
            }
        });

        Button btn_startConversation = (Button)findViewById(R.id.button);
        btn_startConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(RongIM.getInstance() != null) {
                    RongIM.getInstance().startPrivateChat(MainActivity.this, "1", "hello");
                }
            }
        });*/

        Button btn_reg = (Button)findViewById(R.id.button);
        btn_reg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, RegActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_friend) {
            Intent intent = new Intent(MainActivity.this, AddFriendActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
