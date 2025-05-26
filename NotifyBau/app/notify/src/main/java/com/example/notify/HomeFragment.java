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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;


public class HomeFragment extends Fragment {
    // Method to update user information
    private void updateUserInfo(View view) {
        TextView userName = view.findViewById(R.id.student_name);
        TextView userId = view.findViewById(R.id.student_id);
        TextView programValue = view.findViewById(R.id.program_value);
        TextView cgpa = view.findViewById(R.id.CGPA_value);
        TextView sgpa = view.findViewById(R.id.SGPA_value);
        TextView creditsValue = view.findViewById(R.id.Credits_value);
        ImageView userAvatar = view.findViewById(R.id.def_user_photo);
        LocalApi localApi = new LocalApi(requireContext());
        localApi.postResponseToListener(
                localApi::getXFilesInfo, (xfilesinfoString) -> {
                    Gson gson = new Gson();
                    return gson.fromJson(xfilesinfoString, XFilesInfo.class);
                }, "iconnect/xfiles"
        ).observe(getViewLifecycleOwner(), xFilesInfo -> {
            userName.setText(xFilesInfo.getStudentName());
            userId.setText(xFilesInfo.getStudentID());
            userAvatar.setImageBitmap(UtilsKt.b64ToBitmap(xFilesInfo.getStudentImage()));
            programValue.setText(xFilesInfo.getProgram());
            cgpa.setText(xFilesInfo.getCgpa());
            sgpa.setText(xFilesInfo.getSgpa());
            creditsValue.setText(xFilesInfo.getCreditsCompleted());
        });
    }
    
    private void updateFinancialInfo() {
        
    }
    
    private void updateGrades() {
        
    }


    private  void setupRefresh(View view) {
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_fragment);
        swipeRefreshLayout.setDistanceToTriggerSync(240);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            FragmentActivity activity = requireActivity();
            Fragment currentFragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.container);
            assert currentFragment != null;
            // before reconstructing view, clear cache key for the current fragment
            UtilsKt.clearFragmentCache(currentFragment, requireContext());
            // then refresh fragment frame layout
            FragmentManager fm = activity.getSupportFragmentManager();
            fm.beginTransaction().detach(currentFragment).commit();
            fm.beginTransaction().attach(currentFragment).commit();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupTabs(View view) {
        FragmentManager childFm = getChildFragmentManager();
        TabLayout tabLayout = view.findViewById(R.id.homefragment_tabs);
        ViewPager2 viewPager = view.findViewById(R.id.homefragment_pager);
        viewPager.setNestedScrollingEnabled(true);
        HomepageAdapter adapter = new HomepageAdapter(childFm,this.getLifecycle());
        adapter.addFragment(new StudentInfoFragment(), "Student Info");
        adapter.addFragment(new FinancialInfoFragment(), "Financial Info");
        adapter.addFragment(new GradesFragment(), "Grades");
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getTabTitle(position))
        ).attach();
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
        setupRefresh(view);
        setupTabs(view);
    }
}