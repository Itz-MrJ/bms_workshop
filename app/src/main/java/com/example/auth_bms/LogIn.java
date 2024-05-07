package com.example.auth_bms;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class LogIn extends AppCompatActivity {
    Button log;
    EditText username,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        log = findViewById(R.id.log);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String us,pwd;
                us = String.valueOf(username.getText());
                pwd = String.valueOf(password.getText());
                if(us.isEmpty() || pwd.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Neither of the fields should be empty!", Toast.LENGTH_LONG).show();
                    return;
                }

                JSONObject js = new JSONObject();
                try {
                    js.put("username", us);
                    js.put("password", pwd);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String json = js.toString();
                new HTTP(json).execute();
            }
        });
    }
    public class HTTP extends AsyncTask{
        String json;
        String res;
        public HTTP(String json){
            this.json = json;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                URL url = new URL("http://192.168.0.130:5000/api/login");
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                OutputStream out = con.getOutputStream();
                out.write(json.getBytes());
                out.flush();
                out.close();

                int respCode = con.getResponseCode();
                if(respCode == HttpURLConnection.HTTP_OK){
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = br.readLine()) != null)response.append(line);
                    br.close();
                    res = response.toString();
                }else{
                    res = "err";
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(Objects.equals(res,"err")){
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                JSONObject js = new JSONObject(res);
                String code = js.get("code").toString();
                if(Objects.equals(code, "A")){
                    Toast.makeText(getApplicationContext(), "✅ Successfully logged in!", Toast.LENGTH_LONG).show();
                    Intent ini = new Intent(LogIn.this, Welcome.class);
                    ini.putExtra("name", js.get("name").toString());
                    startActivity(ini);
                    finish();
                }else if(Objects.equals(code, "B")){
                    Toast.makeText(getApplicationContext(),"🚫 Invalid credentials entered",Toast.LENGTH_LONG).show();
                }else if(Objects.equals(code, "Z")){
                    Toast.makeText(getApplicationContext(),"⚠ No user exists",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
        }
    }
}