package com.example.notify;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;


public class HomeFragment extends Fragment {
    // Method to update user information


    private void setupTabs(View view) {
        FragmentManager childFm = getChildFragmentManager();
        TabLayout tabLayout = view.findViewById(R.id.homefragment_tabs);
        ViewPager2 viewPager = view.findViewById(R.id.homefragment_pager);
        viewPager.setNestedScrollingEnabled(true);
        HomepageAdapter adapter = new HomepageAdapter(childFm, this.getLifecycle());
        adapter.addFragment(new StudentInfoFragment(), "Student Info");
        adapter.addFragment(new FinancialInfoFragment(), "Financial Info");
        adapter.addFragment(new GradesFragment(), "Grades");
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getTabTitle(position))
        ).attach();
    }

    public void refreshHome() {
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTabs(view);
    }
}