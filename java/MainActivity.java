package com.jikexueyuan.jike_chat;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;


public class MainActivity extends ActionBarActivity {

    BaseAdapter adapter = null;
    List<String> list = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    Toast.makeText(MainActivity.this, "Error1", Toast.LENGTH_LONG).show();
                }
            });

            RongIM.setUserInfoProvider(new RongIM.UserInfoProvider() {
                @Override
                public UserInfo getUserInfo(String s) {
                    UserInfo userInfo = new UserInfo(s, s, Uri.parse("http://cdnq.duitang.com/uploads/item/201408/28/20140828224750_cuECw.jpeg"));
                    return userInfo;
                }
            }, true);

        }
        else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        }

        final ListView listView = (ListView)findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String target = (String) listView.getAdapter().getItem(position);
                RongIM.getInstance().startPrivateChat(MainActivity.this, target, null);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFriendList();
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
        else if(id == R.id.refresh) {
            refreshFriendList();
        }
        else if(id == R.id.conversationList) {
            RongIM.getInstance().startConversationList(MainActivity.this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshFriendList()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("username", App.username);
        client.post("http://192.168.0.157:81/chat/getFriendList.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                String response = new String(bytes);
                Log.e("debug", response);
                try {
                    JSONArray array = new JSONArray(response);
                    list.clear();
                    for(int j = 0; j < array.length(); j++) {
                        list.add((String)array.get(j));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(MainActivity.this, "refreshFriendList failed, due to internet error", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
