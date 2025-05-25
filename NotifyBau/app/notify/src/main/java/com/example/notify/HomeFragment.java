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

    //grab info from Iconnect and put it in the respective fields:
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentManager childFm = getChildFragmentManager();
        TabLayout tabLayout = requireView().findViewById(R.id.homefragment_tabs);
        ViewPager2 viewPager = requireView().findViewById(R.id.homefragment_pager);
        HomepageAdapter adapter = new HomepageAdapter(childFm,this.getLifecycle());
        adapter.addFragment(new StudentInfoFragment(), StudentInfoFragment.class.getSimpleName());
        adapter.addFragment(new FinancialInfoFragment(), FinancialInfoFragment.class.getSimpleName());
        adapter.addFragment(new GradesFragment(), GradesFragment.class.getSimpleName());
        viewPager.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        viewPager.setCurrentItem(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                }
        );
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
        SwipeRefreshLayout swipeRefreshLayout = requireView().findViewById(R.id.swipe_fragment);
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
}