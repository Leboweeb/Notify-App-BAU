package com.example.bettermoodle;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.islandparadise14.mintable.MinTimeTableView;
import com.islandparadise14.mintable.model.ScheduleEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class ScheduleFragment extends Fragment {

    private ScheduleUtils scheduleUtils;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button refreshButton = view.findViewById(R.id.refreshButton);
        Button refreshColors = view.findViewById(R.id.refreshColors);
        LocalApi localApi = new LocalApi(requireContext());
        this.scheduleUtils = new ScheduleUtils(requireContext());
        refreshButton.setOnClickListener(v -> {
            this.scheduleUtils.refreshSchedule(true, false);
        });
        refreshColors.setOnClickListener(v -> {
            this.scheduleUtils.refreshSchedule(false, true);
        });
        MinTimeTableView table = view.findViewById(R.id.weekView);
        table.initTable(List.of("Mon", "Tue", "Wen", "Thu", "Fri").toArray(new String[5]));
        localApi.postResponseToListener(localApi::getSchedule, s -> {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Course>>() {
            }.getType();
            return gson.fromJson(s, listType);
        }, Services.SCHEDULE.getCacheKey()).observe(getViewLifecycleOwner(), courses -> {
            ArrayList<ScheduleEntity> events = this.scheduleUtils.populateEvents(courses);
            table.updateSchedules(events);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }


}