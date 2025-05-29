package com.example.notify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;


public class SettingsFragment extends Fragment {


    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_PUSH_NOTIFICATIONS = "push_notifications";
    private static final String KEY_REFRESH_INTERVAL = "refresh_interval";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RangeSlider slider = view.findViewById(R.id.slider_refresh_interval);
        TextInputEditText email = view.findViewById(R.id.login);
        TextInputEditText password = view.findViewById(R.id.password);
        EncryptedSharedPreferences prefs = UtilsKt.getPrefs(requireContext());
        if (prefs.contains("username"))
            email.setText(prefs.getString("username",""));
        if (prefs.contains("password"))
            password.setText(prefs.getString("password",""));
        Button btnLogout;
        btnLogout = view.findViewById(R.id.btnLogout);
        slider.addOnChangeListener(
                (slider1, value, fromUser) -> {
                    if (fromUser)
                        prefs.edit().putFloat(KEY_REFRESH_INTERVAL,value).apply();
                }
        );
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {

        UtilsKt.clearPrefs(requireContext());
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        startActivity(intent);


        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
