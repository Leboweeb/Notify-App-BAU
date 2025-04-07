package com.example.bettermoodle;

import static com.example.bettermoodle.UtilsKt.getPrefs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;

public class MainActivity extends AppCompatActivity {
    EditText userid;
    EditText password;
    EditText ipaddress;
    Button loginbutton;

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);
        userid = findViewById(R.id.userid);
        password = findViewById(R.id.password);
        loginbutton = findViewById(R.id.loginbutton);
        EncryptedSharedPreferences prefs = getPrefs(getApplicationContext());
        String[] keys = {"username", "password"};
        EditText[] texts = {userid, password};
        // replace user and password fields, or empty string.
        for (int i = 0; i < keys.length; i++) {
            String value = prefs.getString(keys[i], "");
            texts[i].setText(value);
        }
        loginbutton.setOnClickListener(view -> {
            // store information for future logins
            for (int i = 0; i < keys.length; i++) {
                prefs.edit().putString(keys[i], texts[i].getText().toString()).apply();
            }
            if (texts[0].getText().toString().isEmpty() || texts[1].getText().toString().isEmpty())
                UtilsKt.showToastMessage(this, "Cannot login with empty credentials!");
            else {
                Intent intent = new Intent(MainActivity.this, OptionPage2.class);
                startActivity(intent);
            }
        });


    }


}
