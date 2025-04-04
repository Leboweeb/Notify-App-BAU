package com.example.bettermoodle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class NotificationsFragment extends Fragment implements MenuProvider {

    ListView listView;
    SearchView searchView;
    LocalApi localApi;
    List<Notification> notificationList;
    private ArrayAdapter<String> arrayAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    private void updateNotifications(View view){
        listView = requireView().findViewById(R.id.listView);
        searchView = requireView().findViewById(R.id.SearchView);
        localApi.postResponseToListener(localApi::getMoodleNotifications, (notificationsJson) -> {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Notification>>() {
            }.getType();
            return gson.fromJson(notificationsJson, listType);
        }, "moodle/notifications").observe(getViewLifecycleOwner(), notificationList -> {
            this.notificationList = notificationList;
            String[] notificationsStrings = notificationList.stream().map(Notification::toString).toArray(String[]::new);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(), R.layout.list_custometext, notificationsStrings);
            this.arrayAdapter = arrayAdapter;
            listView.setAdapter(arrayAdapter);
        });
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Intent intent = new Intent(getActivity(), IndividualAnnouncement.class);
            String notificationMessage = (String) listView.getItemAtPosition(position);
            intent.putExtra("message", notificationMessage);
            startActivity(intent);
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.localApi = new LocalApi(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        // this.jsonInterface = new JsonInterface();
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    //List of Notification Code:
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        updateNotifications(view);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateNotifications(view);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    //Search Bar Code:
    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.notifcations_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.Search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                arrayAdapter.getFilter().filter(newText);
                return true;
            }
        });

    }

    //Function that takes you to the individual notification
    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }


}