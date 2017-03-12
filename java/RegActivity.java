package com.jikexueyuan.jike_chat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import static com.jikexueyuan.jike_chat.App.username;

public class RegActivity extends AppCompatActivity {

    public String JSONTokener(String in){
        //consume an option byte order mark (BOM) if it exists
        if (in != null && in.startsWith("\ufeff")){
            in = in.substring(1);
        }
        return in;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        setTitle("注册");

        Button btn_reg = (Button)findViewById(R.id.btn_reg);
        final EditText et_username = (EditText)findViewById(R.id.et_username);
        final EditText et_password = (EditText)findViewById(R.id.et_password);

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = et_username.getText().toString();
                String password = et_password.getText().toString();

                if(username.equals("") || password.equals("")) {
                    Toast.makeText(RegActivity.this, "用户名或密码不能为空", Toast.LENGTH_LONG).show();
                }
                else {
                    AsyncHttpClient client = new AsyncHttpClient();
                    RequestParams params = new RequestParams();
                    params.add("username", username);
                    params.add("password", password);
                    client.post("http://192.168.0.157:81/chat/reg.php", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int i, Header[] headers, byte[] bytes) {
                            String response = new String(bytes);
                            Log.e("debug", response);
                            JSONObject object = null;
                            try {
                                object =  new JSONObject(response);
                                String status = object.getString("status");
                                if (status.equals("exists")) {
                                    Toast.makeText(RegActivity.this, "用户名已存在，请更换", Toast.LENGTH_LONG).show();
                                } else if(status.equals("error")) {
                                    Toast.makeText(RegActivity.this, "出现错误，请稍后重试", Toast.LENGTH_LONG).show();

                                }
                                else if(status.equals("success")){
                                    String token = object.getString("token");
                                     App.token = token;
                                    App.username = username;
                                    App.isLogin = true;
                                    Intent intent = new Intent(RegActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    RegActivity.this.finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                            Toast.makeText(RegActivity.this, "网络错误，请稍后重试", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reg, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
