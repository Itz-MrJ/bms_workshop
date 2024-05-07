package com.example.auth_bms;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class SignUp extends AppCompatActivity {
    private EditText name,username,pwd;
    private Button b1;
    private URL url;
    private HttpURLConnection urlConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        pwd = findViewById(R.id.password);
        b1 = findViewById(R.id.sign);
        ImageView back = findViewById(R.id.imageView);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n,u,p;
                n = String.valueOf(name.getText());
                u = String.valueOf(username.getText());
                p = String.valueOf(pwd.getText());
                if(n.isEmpty() || u.isEmpty() || p.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Neither of the fields can be empty.", Toast.LENGTH_LONG).show();
                    return;
                }
                JSONObject js = new JSONObject();
                try {
                    js.put("name", String.valueOf(name.getText()));
                    js.put("username", String.valueOf(username.getText()));
                    js.put("password", String.valueOf(pwd.getText()));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String json = js.toString();
                new HTTP(json).execute();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private class HTTP extends AsyncTask{
        String json,res;
        public HTTP(String json){
            this.json = json;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                URL url = new URL("http://192.168.0.130:5000/api/signup");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(json.getBytes());
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    res = response.toString();
                } else {
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
            if(Objects.equals(res, "err")){
                Toast.makeText(getApplicationContext(), "Error",Toast.LENGTH_LONG).show();
                return;
            }
            try {
                JSONObject js = new JSONObject(res);
                String code = js.get("code").toString();
                if(Objects.equals(code, "A")) {
                    Toast.makeText(getApplicationContext(), "✅Successfully created user!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SignUp.this, MainActivity.class));
                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(), "⚠ User already exists", Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error",Toast.LENGTH_LONG).show();
                return;
            }

        }
    }

}