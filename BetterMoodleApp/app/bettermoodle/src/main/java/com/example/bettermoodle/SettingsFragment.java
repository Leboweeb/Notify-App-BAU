package com.example.bettermoodle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;


public class SettingsFragment extends Fragment {


    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_PUSH_NOTIFICATIONS = "push_notifications";
    private static final String KEY_EMAIL_NOTIFICATIONS = "email_notifications";

    private SharedPreferences sharedPreferences;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the logout button
        Button btnLogout;
        SwitchMaterial nightModeSwitch = new SwitchMaterial(this.requireContext());

        btnLogout = view.findViewById(R.id.btnLogout);

        // Set click listener for logout button
        btnLogout.setOnClickListener(v -> logout());
        setupNightModeSwitch(view);
    }


    private void setupNightModeSwitch(View view) {
        SwitchMaterial nightModeSwitch = view.findViewById(R.id.night_mode_switch);

        // Set initial state based on saved preference
        boolean isNightMode = sharedPreferences.getBoolean(KEY_NIGHT_MODE, false);
        nightModeSwitch.setChecked(isNightMode);

        nightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            sharedPreferences.edit().putBoolean(KEY_NIGHT_MODE, isChecked).apply();

            // Apply theme
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // Activity will be recreated to apply theme
        });
    }

    private void setupFontSizeSpinner(View view) {
        Spinner fontSizeSpinner = view.findViewById(R.id.spinner_font_size);

        // Font size options
        String[] fontSizes = {"Small", "Medium", "Large", "Extra Large"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                fontSizes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSizeSpinner.setAdapter(adapter);

        // Set initial selection based on saved preference
        int savedFontSizeIndex = sharedPreferences.getInt(KEY_FONT_SIZE, 1); // Default to Medium
        fontSizeSpinner.setSelection(savedFontSizeIndex);

        fontSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Save selected font size
                sharedPreferences.edit().putInt(KEY_FONT_SIZE, position).apply();

                // Apply font size changes
                applyFontSize(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupLanguageSpinner(View view) {
        Spinner languageSpinner = view.findViewById(R.id.spinner_language);

        // Language options
        String[] languages = {"English", "Spanish", "French", "German", "Chinese", "Japanese"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                languages
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Set initial selection based on saved preference
        int savedLanguageIndex = sharedPreferences.getInt(KEY_LANGUAGE, 0); // Default to English
        languageSpinner.setSelection(savedLanguageIndex);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Save selected language
                sharedPreferences.edit().putInt(KEY_LANGUAGE, position).apply();

                // Apply language change (in a real app, this would change the app's locale)
                String selectedLanguage = languages[position];
                Toast.makeText(requireContext(), "Language set to " + selectedLanguage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupNotificationSwitches(View view) {
        SwitchMaterial pushNotificationsSwitch = view.findViewById(R.id.switch_push_notifications);
        SwitchMaterial emailNotificationsSwitch = view.findViewById(R.id.switch_email_notifications);

        // Set initial states
        boolean pushEnabled = sharedPreferences.getBoolean(KEY_PUSH_NOTIFICATIONS, true);
        boolean emailEnabled = sharedPreferences.getBoolean(KEY_EMAIL_NOTIFICATIONS, true);

        pushNotificationsSwitch.setChecked(pushEnabled);
        emailNotificationsSwitch.setChecked(emailEnabled);

        // Set listeners
        pushNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, isChecked).apply();
            // Here you would enable/disable push notifications in a real app
        });

        emailNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_EMAIL_NOTIFICATIONS, isChecked).apply();
            // Here you would enable/disable email notifications in a real app
        });
    }

    private void applyFontSize(int fontSizeIndex) {
        // In a real app, you would change the app's font scale factor
        // This is just a placeholder to show toast feedback
        String[] sizeNames = {"Small", "Medium", "Large", "Extra Large"};
        Toast.makeText(requireContext(), "Font size set to " + sizeNames[fontSizeIndex], Toast.LENGTH_SHORT).show();
    }


    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }


    private void logout() {

        UtilsKt.clearPrefs(requireContext());
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        startActivity(intent);


        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

    }


}
