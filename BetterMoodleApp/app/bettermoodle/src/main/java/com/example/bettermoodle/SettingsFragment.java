package com.example.bettermoodle;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;




public class SettingsFragment extends Fragment {


    public SettingsFragment() {
            // Required empty public constructor
        }

        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_settings, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Initialize the logout button
            Button btnLogout;
            btnLogout = view.findViewById(R.id.btnLogout);

            // Set click listener for logout button
            btnLogout.setOnClickListener(v -> logout());
        }

        private void logout() {

            UtilsKt.clearPrefs(requireContext());
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            startActivity(intent);


                Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        }
}
