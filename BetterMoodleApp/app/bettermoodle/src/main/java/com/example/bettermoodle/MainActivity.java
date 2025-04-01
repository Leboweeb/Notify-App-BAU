package com.example.bettermoodle;

import static com.example.bettermoodle.UtilsKt.getPrefs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;

public class MainActivity extends AppCompatActivity {
    EditText userid;
    EditText password;
    EditText ipaddress;
    Button loginbutton;

    ProgressBar progressBar;

    public void logStackTrace(String Tag, @NonNull Exception e) {
        Log.e(Tag, String.valueOf(e.getCause()));
    }

    public void showToast(final String toast) {
        runOnUiThread(() -> Toast.makeText(this, toast, Toast.LENGTH_SHORT).show());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);
        userid = findViewById(R.id.userid);
        password = findViewById(R.id.password);
        loginbutton = findViewById(R.id.loginbutton);
        EncryptedSharedPreferences prefs = getPrefs(getApplicationContext());
        assert prefs != null;
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
            Intent intent = new Intent(MainActivity.this, OptionPage2.class);
            startActivity(intent);
//            JsonInterface jsonInterface = new JsonInterface(this, "");
//            // set the bar to visible before request
//            progressBar.setVisibility(View.VISIBLE);
//            CompletableFuture<Response> responseFuture = jsonInterface.connectToApi();
            // Threads die of natural causes when they return
            // lol
//            new Thread(() -> {
//                try {
//                    Response response = responseFuture.get();
//                    runOnUiThread(() -> {
//                        if (response.isSuccessful()) {
//                            startActivity(intent);
//                        } else {
//                            this.showToast("Invalid Credentials!");
//                        }
//                    });
//
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                } catch (ExecutionException e) {
//                    runOnUiThread(() -> {
//                        Toast.makeText(MainActivity.this, "Failed to connect to moodle API!", Toast.LENGTH_SHORT).show();
//                        logStackTrace("HTTP Tag", e);
//                        progressBar.setVisibility(View.GONE);
//                    });
//                }
//            }).start()
        });


    }


}
